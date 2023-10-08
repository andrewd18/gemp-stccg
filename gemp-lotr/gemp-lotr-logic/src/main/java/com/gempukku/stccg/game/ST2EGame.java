package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardDeck;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.gamestate.GameStateListener;
import com.gempukku.stccg.gamestate.TribblesGameState;
import com.gempukku.stccg.gamestate.UserFeedback;
import com.gempukku.stccg.processes.TurnProcedure;
import com.gempukku.stccg.rules.RuleSet;

import java.util.Map;

public class ST2EGame extends DefaultGame {
    private final GameState _gameState;
    private final TurnProcedure<ST2EGame> _turnProcedure;

    public ST2EGame(GameFormat format, Map<String, CardDeck> decks, UserFeedback userFeedback,
                    final CardBlueprintLibrary library) {
        super(format, decks, userFeedback, library);

        new RuleSet(_actionsEnvironment, _modifiersLogic).applyRuleSet();

            // TODO: Will likely need its own game state class
        _gameState = new TribblesGameState(_allPlayers, decks, library, _format);
        _turnProcedure = new TurnProcedure<>(this, _allPlayers, userFeedback, _actionsEnvironment,
                _gameState::init);
    }


    @Override
    public GameState getGameState() {
        return _gameState;
    }
    public TurnProcedure<ST2EGame> getTurnProcedure() { return _turnProcedure; }
    @Override
    public void addGameStateListener(String playerId, GameStateListener gameStateListener) {
        getGameState().addGameStateListener(playerId, gameStateListener, _turnProcedure.getGameStats());
    }
    

    public boolean checkPlayRequirements(PhysicalCard card) {
//        _gameState.sendMessage("Calling game.checkPlayRequirements for card " + card.getBlueprint().getTitle());

        // Check if card's own play requirements are met
        if (card.getBlueprint().playRequirementsNotMet(this, card))
            return false;
        // Check if the card's playability has been modified in the current game state
        return !_modifiersLogic.canNotPlayCard(this, card.getOwner(), card);

    }

}
