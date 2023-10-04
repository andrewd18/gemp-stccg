package com.gempukku.lotro.effects;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.rules.GameUtils;
import com.gempukku.lotro.game.PlayOrder;
import com.gempukku.lotro.results.RevealCardFromTopOfDeckResult;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class RevealTopCardsOfDrawDeckEffect extends AbstractEffect {
    private final PhysicalCard _source;
    private final String _playerId;
    private final int _count;

    public RevealTopCardsOfDrawDeckEffect(PhysicalCard source, String playerId, int count) {
        _source = source;
        _playerId = playerId;
        _count = count;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return game.getGameState().getDrawDeck(_playerId).size() >= _count;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        List<? extends PhysicalCard> deck = game.getGameState().getDrawDeck(_playerId);
        int count = Math.min(deck.size(), _count);
        LinkedList<PhysicalCard> topCards = new LinkedList<>(deck.subList(0, count));
        if (topCards.size() > 0) {
            final PlayOrder playerOrder = game.getGameState().getPlayerOrder().getCounterClockwisePlayOrder(_source.getOwner(), false);

            String nextPlayer;
            while ((nextPlayer = playerOrder.getNextPlayer()) != null) {
                game.getUserFeedback().sendAwaitingDecision(nextPlayer,
                        new ArbitraryCardsSelectionDecision(1, _playerId + " revealed card(s) from top of deck", topCards, Collections.emptySet(), 0, 0) {
                            @Override
                            public void decisionMade(String result) {
                            }
                        });
            }

            game.getGameState().sendMessage(GameUtils.getCardLink(_source) + " revealed cards from top of " + _playerId + " deck - " + getAppendedNames(topCards));
            for (PhysicalCard topCard : topCards) {
                game.getActionsEnvironment().emitEffectResult(
                        new RevealCardFromTopOfDeckResult(_playerId, topCard));
            }
        }
        cardsRevealed(topCards);
        return new FullEffectResult(topCards.size() == _count);
    }

    protected abstract void cardsRevealed(List<PhysicalCard> revealedCards);
}
