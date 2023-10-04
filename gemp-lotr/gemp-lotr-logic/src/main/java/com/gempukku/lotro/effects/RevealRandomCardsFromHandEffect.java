package com.gempukku.lotro.effects;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.rules.GameUtils;
import com.gempukku.lotro.game.PlayOrder;
import com.gempukku.lotro.results.RevealCardFromHandResult;

import java.util.Collections;
import java.util.List;

public abstract class RevealRandomCardsFromHandEffect extends AbstractEffect {
    private final PhysicalCard _source;
    private final int _count;
    private final String _actingPlayer;
    private final String _playerHand;

    protected RevealRandomCardsFromHandEffect(String actingPlayer, String handOfPlayer, PhysicalCard source, int count) {
        _actingPlayer = actingPlayer;
        _playerHand = handOfPlayer;
        _source = source;
        _count = count;
    }

    @Override
    public String getText(DefaultGame game) {
        return "Reveal cards from hand";
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        if (_actingPlayer.equals(_playerHand) || game.getModifiersQuerying().canLookOrRevealCardsInHand(game, _playerHand, _actingPlayer)) {
            List<PhysicalCard> randomCards = GameUtils.getRandomCards(game.getGameState().getHand(_playerHand), _count);

            if (randomCards.size() > 0) {
                final PlayOrder playerOrder = game.getGameState().getPlayerOrder().getCounterClockwisePlayOrder(_source.getOwner(), false);

                String nextPlayer;
                while ((nextPlayer = playerOrder.getNextPlayer()) != null) {
                    game.getUserFeedback().sendAwaitingDecision(nextPlayer,
                            new ArbitraryCardsSelectionDecision(1, _playerHand+" revealed card(s) from hand at random", randomCards, Collections.emptySet(), 0, 0) {
                                @Override
                                public void decisionMade(String result) {
                                }
                            });
                }

                game.getGameState().sendMessage(GameUtils.getCardLink(_source) + " revealed cards from " + _playerHand + " hand at random - " + getAppendedNames(randomCards));
            }
            else {
                game.getGameState().sendMessage("No cards in " + _playerHand + " hand to reveal");
            }
            cardsRevealed(randomCards);
            for (PhysicalCard randomCard : randomCards)
                game.getActionsEnvironment().emitEffectResult(new RevealCardFromHandResult(_source, _playerHand, randomCard));

            return new FullEffectResult(randomCards.size() == _count);
        }
        return new FullEffectResult(false);
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        if (game.getGameState().getHand(_playerHand).size() < _count)
            return false;
        return _actingPlayer.equals(_playerHand) || game.getModifiersQuerying().canLookOrRevealCardsInHand(game, _playerHand, _actingPlayer);
    }

    protected abstract void cardsRevealed(List<PhysicalCard> revealedCards);
}
