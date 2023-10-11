package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.PlayOrder;
import com.gempukku.stccg.results.RevealCardFromHandResult;
import com.gempukku.stccg.rules.GameUtils;

import java.util.Collections;
import java.util.List;

public abstract class RevealRandomCardsFromHandEffect extends DefaultEffect {
    private final PhysicalCard _source;
    private final int _count;
    private final String _actingPlayer;
    private final String _playerHand;
    private final DefaultGame _game;

    protected RevealRandomCardsFromHandEffect(ActionContext actionContext, String handOfPlayer, int count) {
        _actingPlayer = actionContext.getPerformingPlayer();
        _playerHand = handOfPlayer;
        _source = actionContext.getSource();
        _count = count;
        _game = actionContext.getGame();
    }

    @Override
    public String getText() {
        return "Reveal cards from hand";
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (_actingPlayer.equals(_playerHand) || _game.getModifiersQuerying().canLookOrRevealCardsInHand(_game, _playerHand, _actingPlayer)) {
            List<PhysicalCard> randomCards = GameUtils.getRandomCards(_game.getGameState().getHand(_playerHand), _count);

            if (randomCards.size() > 0) {
                final PlayOrder playerOrder = _game.getGameState().getPlayerOrder().getCounterClockwisePlayOrder(_source.getOwner(), false);

                String nextPlayer;
                while ((nextPlayer = playerOrder.getNextPlayer()) != null) {
                    _game.getUserFeedback().sendAwaitingDecision(nextPlayer,
                            new ArbitraryCardsSelectionDecision(1, _playerHand+" revealed card(s) from hand at random", randomCards, Collections.emptySet(), 0, 0) {
                                @Override
                                public void decisionMade(String result) {
                                }
                            });
                }

                _game.getGameState().sendMessage(GameUtils.getCardLink(_source) + " revealed cards from " + _playerHand + " hand at random - " + GameUtils.getAppendedNames(randomCards));
            }
            else {
                _game.getGameState().sendMessage("No cards in " + _playerHand + " hand to reveal");
            }
            cardsRevealed(randomCards);
            for (PhysicalCard randomCard : randomCards)
                _game.getActionsEnvironment().emitEffectResult(new RevealCardFromHandResult(_source, _playerHand, randomCard));

            return new FullEffectResult(randomCards.size() == _count);
        }
        return new FullEffectResult(false);
    }

    @Override
    public boolean isPlayableInFull() {
        if (_game.getGameState().getHand(_playerHand).size() < _count)
            return false;
        return _actingPlayer.equals(_playerHand) || _game.getModifiersQuerying().canLookOrRevealCardsInHand(_game, _playerHand, _actingPlayer);
    }

    protected abstract void cardsRevealed(List<PhysicalCard> revealedCards);
}
