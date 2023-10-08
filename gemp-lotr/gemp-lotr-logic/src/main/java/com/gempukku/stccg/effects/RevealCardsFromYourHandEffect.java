package com.gempukku.stccg.effects;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;
import com.gempukku.stccg.game.PlayOrder;
import com.gempukku.stccg.results.RevealCardFromHandResult;

import java.util.Collection;
import java.util.Collections;

public class RevealCardsFromYourHandEffect extends AbstractEffect {
    private final PhysicalCard _source;
    private final String _handPlayerId;
    private final Collection<? extends PhysicalCard> _cards;

    public RevealCardsFromYourHandEffect(PhysicalCard source, String handPlayerId, PhysicalCard card) {
        this(source, handPlayerId, Collections.singleton(card));
    }

    public RevealCardsFromYourHandEffect(PhysicalCard source, String handPlayerId, Collection<? extends PhysicalCard> cards) {
        _source = source;
        _handPlayerId = handPlayerId;
        _cards = cards;
    }

    @Override
    public String getText(DefaultGame game) {
        return "Reveal cards from hand";
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        for (PhysicalCard card : _cards) {
            if (card.getZone() != Zone.HAND)
                return false;
        }

        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        game.getGameState().sendMessage(GameUtils.getCardLink(_source) + " revealed " + _handPlayerId + " cards in hand - " + getAppendedNames(_cards));

        final PlayOrder playerOrder = game.getGameState().getPlayerOrder().getCounterClockwisePlayOrder(_handPlayerId, false);
        // Skip hand owner
        playerOrder.getNextPlayer();

        String nextPlayer;
        while ((nextPlayer = playerOrder.getNextPlayer()) != null) {
            game.getUserFeedback().sendAwaitingDecision(nextPlayer,
                    new ArbitraryCardsSelectionDecision(1, _handPlayerId + " revealed card(s) in hand", _cards, Collections.emptySet(), 0, 0) {
                        @Override
                        public void decisionMade(String result) {
                        }
                    });
        }

        for (PhysicalCard card : _cards) {
            game.getActionsEnvironment().emitEffectResult(new RevealCardFromHandResult(_source, _handPlayerId, card));
        }

        return new FullEffectResult(true);
    }
}
