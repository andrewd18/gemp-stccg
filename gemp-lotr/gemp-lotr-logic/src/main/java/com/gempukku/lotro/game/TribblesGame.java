package com.gempukku.lotro.game;

import com.gempukku.lotro.cards.CardBlueprintLibrary; // has some LotR stuff
import com.gempukku.lotro.cards.PhysicalCard; // has some LotR stuff
import com.gempukku.lotro.cards.CardDeck;
import com.gempukku.lotro.common.Phase; // has some LotR stuff
import com.gempukku.lotro.communication.GameStateListener; // has some LotR stuff
import com.gempukku.lotro.communication.UserFeedback;
import com.gempukku.lotro.game.actions.ActionStack;
import com.gempukku.lotro.game.actions.ActionsEnvironment;
import com.gempukku.lotro.game.actions.DefaultActionsEnvironment; // sort of has some LotR stuff (costs for effects)
import com.gempukku.lotro.game.adventure.Adventure; // LotR-specific
import com.gempukku.lotro.game.modifiers.ModifiersEnvironment; // has some LotR stuff
import com.gempukku.lotro.game.modifiers.ModifiersLogic; // has some LotR stuff
import com.gempukku.lotro.game.modifiers.ModifiersQuerying; // has some LotR stuff
import com.gempukku.lotro.game.state.tribbles.TribblesGameState;
import com.gempukku.lotro.game.state.GameStats; // relies on a couple LotR things (like "adventure deck")
import com.gempukku.lotro.game.timing.GameResultListener;
import com.gempukku.lotro.game.timing.PlayerOrder;
import com.gempukku.lotro.game.timing.PlayerOrderFeedback;
import com.gempukku.lotro.game.timing.processes.turn.tribbles.TribblesTurnProcedure;
import com.gempukku.lotro.game.rules.tribbles.TribblesRuleSet;
import org.apache.log4j.Logger;

import java.util.*;

public class TribblesGame implements DefaultGame {
    private static final Logger log = Logger.getLogger(TribblesGame.class);
    private final TribblesGameState _gameState;
    private final ModifiersLogic _modifiersLogic = new ModifiersLogic();
    private final DefaultActionsEnvironment _actionsEnvironment;
    private final UserFeedback _userFeedback;
    private final TribblesTurnProcedure _turnProcedure;
    private final ActionStack _actionStack;
    private boolean _cancelled;
    private boolean _finished;

    private final Adventure _adventure;
    private final LotroFormat _format;

    private final Set<String> _allPlayers;
    private final Map<String, Set<Phase>> _autoPassConfiguration = new HashMap<>();

    private String _winnerPlayerId;
    private final Map<String, String> _losers = new HashMap<>();

    private final Set<GameResultListener> _gameResultListeners = new HashSet<>();

    private final Set<String> _requestedCancel = new HashSet<>();
    private final CardBlueprintLibrary _library;

    public TribblesGame(LotroFormat format, Map<String, CardDeck> decks, UserFeedback userFeedback,
                        final CardBlueprintLibrary library) {
        _library = library;
        _adventure = format.getAdventure();
        _format = format;
        _actionStack = new ActionStack();

        _allPlayers = decks.keySet();

        _actionsEnvironment = new DefaultActionsEnvironment(this, _actionStack);

        final Map<String, List<String>> cards = new HashMap<>();
        for (String playerId : decks.keySet()) {
            List<String> deck = new LinkedList<>();

            CardDeck playerDeck = decks.get(playerId);
            deck.addAll(playerDeck.getDrawDeckCards());

            cards.put(playerId, deck);
        }

        _gameState = new TribblesGameState();

        _turnProcedure = new TribblesTurnProcedure(this, decks.keySet(), userFeedback, _actionStack,
                new PlayerOrderFeedback() {
                    @Override
                    public void setPlayerOrder(PlayerOrder playerOrder, String firstPlayer) {
                        final GameStats gameStats = _turnProcedure.getGameStats();
                        _gameState.init(playerOrder, firstPlayer, cards, library, format);
                    }
                });
        _userFeedback = userFeedback;

        TribblesRuleSet ruleSet = new TribblesRuleSet(this, _actionsEnvironment, _modifiersLogic);
        ruleSet.applyRuleSet();

        _adventure.applyAdventureRules(this, _actionsEnvironment, _modifiersLogic);
    }


    @Override
    public boolean shouldAutoPass(String playerId, Phase phase) {
        final Set<Phase> passablePhases = _autoPassConfiguration.get(playerId);
        if (passablePhases == null)
            return false;
        return passablePhases.contains(phase);
    }

    @Override
    public boolean isSolo() {
        return _allPlayers.size() == 1;
    }

    public void addGameResultListener(GameResultListener listener) {
        _gameResultListeners.add(listener);
    }

    public void removeGameResultListener(GameResultListener listener) {
        _gameResultListeners.remove(listener);
    }

    @Override
    public LotroFormat getFormat() {
        return _format;
    }

    public void startGame() {
        if (!_cancelled)
            _turnProcedure.carryOutPendingActionsUntilDecisionNeeded();
    }

    public void carryOutPendingActionsUntilDecisionNeeded() {
        if (!_cancelled)
            _turnProcedure.carryOutPendingActionsUntilDecisionNeeded();
    }

    @Override
    public String getWinnerPlayerId() {
        return _winnerPlayerId;
    }

    public boolean isFinished() {
        return _finished;
    }

    public void cancelGame() {
        if (!_finished) {
            _cancelled = true;

            if (_gameState != null) {
                _gameState.sendMessage("Game was cancelled due to an error, the error was logged and will be fixed soon.");
                _gameState.sendMessage("Please post the replay game link and description of what happened on the TLHH forum.");
            }

            for (GameResultListener gameResultListener : _gameResultListeners)
                gameResultListener.gameCancelled();

            _finished = true;
        }
    }

    public void cancelGameRequested() {
        if (!_finished) {
            _cancelled = true;

            if (_gameState != null)
                _gameState.sendMessage("Game was cancelled, as requested by all parties.");

            for (GameResultListener gameResultListener : _gameResultListeners)
                gameResultListener.gameCancelled();

            _finished = true;
        }
    }

    public boolean isCancelled() {
        return _cancelled;
    }

    @Override
    public void playerWon(String playerId, String reason) {
        if (!_finished) {
            // Any remaining players have lost
            Set<String> losers = new HashSet<>(_allPlayers);
            losers.removeAll(_losers.keySet());
            losers.remove(playerId);

            for (String loser : losers)
                _losers.put(loser, "Other player won");

            gameWon(playerId, reason);
        }
    }

    private void gameWon(String winner, String reason) {
        _winnerPlayerId = winner;

        if (_gameState != null)
            _gameState.sendMessage(_winnerPlayerId + " is the winner due to: " + reason);

        _gameState.finish();

        for (GameResultListener gameResultListener : _gameResultListeners)
            gameResultListener.gameFinished(_winnerPlayerId, reason, _losers);

        _finished = true;
    }

    @Override
    public void playerLost(String playerId, String reason) {
        if (!_finished) {
            if (_losers.get(playerId) == null) {
                _losers.put(playerId, reason);
                if (_gameState != null)
                    _gameState.sendMessage(playerId + " lost due to: " + reason);

                if (_losers.size() + 1 == _allPlayers.size()) {
                    List<String> allPlayers = new LinkedList<>(_allPlayers);
                    allPlayers.removeAll(_losers.keySet());
                    gameWon(allPlayers.get(0), "Last remaining player in game");
                }
            }
        }
    }

    public void requestCancel(String playerId) {
        _requestedCancel.add(playerId);
        if (_requestedCancel.size() == _allPlayers.size())
            cancelGameRequested();
    }

    @Override
    public TribblesGameState getGameState() {
        return _gameState;
    }

    @Override
    public CardBlueprintLibrary getLotroCardBlueprintLibrary() {
        return _library;
    }

    @Override
    public ActionsEnvironment getActionsEnvironment() {
        return _actionsEnvironment;
    }

    @Override
    public ModifiersEnvironment getModifiersEnvironment() {
        return _modifiersLogic;
    }

    @Override
    public ModifiersQuerying getModifiersQuerying() {
        return _modifiersLogic;
    }

    @Override
    public UserFeedback getUserFeedback() {
        return _userFeedback;
    }

    public void addGameStateListener(String playerId, GameStateListener gameStateListener) {
        _gameState.addGameStateListener(playerId, gameStateListener, _turnProcedure.getGameStats());
    }

    public void removeGameStateListener(GameStateListener gameStateListener) {
        _gameState.removeGameStateListener(gameStateListener);
    }

    public void setPlayerAutoPassSettings(String playerId, Set<Phase> phases) {
        _autoPassConfiguration.put(playerId, phases);
    }

    public boolean checkPlayRequirements(PhysicalCard card) {
//        _gameState.sendMessage("Calling game.checkPlayRequirements for card " + card.getBlueprint().getTitle());

        // Check if card's own play requirements are met
        if (!card.getBlueprint().checkPlayRequirements(this, card)) {
//            _gameState.sendMessage("card.checkPlayRequirements failed");
            return false;
        }

        // Check if the card's playability has been modified in the current game state
        if (!_modifiersLogic.canPlayCard(this, card.getOwner(), card)) {
//            _gameState.sendMessage("getModifiersQuerying.canPlayCard failed");
            return false;
        }

        // Otherwise, the play requirements are met if the card is next in the tribble sequence
        return isNextInSequence(card);
    }

    public boolean isNextInSequence(PhysicalCard card) {
        final int cardValue = card.getBlueprint().getTribbleValue();
        if (_gameState.isChainBroken() && (cardValue == 1)) {
            return true;
        }
/*        _gameState.sendMessage(card.getBlueprint().getTitle() + " tribble value = " + cardValue +
                "; nextTribble value = " + _gameState.getNextTribble()); */
        return (cardValue == _gameState.getNextTribble());
    }
}
