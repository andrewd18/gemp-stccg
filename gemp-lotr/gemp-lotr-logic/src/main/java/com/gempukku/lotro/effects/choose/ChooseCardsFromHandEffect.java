package com.gempukku.lotro.effects.choose;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.decisions.CardsSelectionDecision;
import com.gempukku.lotro.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.effects.AbstractEffect;

import java.util.Collection;
import java.util.Set;

public abstract class ChooseCardsFromHandEffect extends AbstractEffect {
    private final String _playerId;
    private final int _minimum;
    private final int _maximum;
    private final Filter _filter;

    public ChooseCardsFromHandEffect(String playerId, int minimum, int maximum, Filterable... filters) {
        _playerId = playerId;
        _minimum = minimum;
        _maximum = maximum;
        _filter = Filters.and(filters);
    }

    @Override
    public String getText(DefaultGame game) {
        return "Choose cards from hand";
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return Filters.filter(game.getGameState().getHand(_playerId), game, _filter).size() >= _minimum;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(final DefaultGame game) {
        final Collection<LotroPhysicalCard> selectableCards = Filters.filter(game.getGameState().getHand(_playerId), game, _filter);
        int maximum = Math.min(_maximum, selectableCards.size());

        boolean success = selectableCards.size() >= _minimum;

        int minimum = Math.min(_minimum, selectableCards.size());

        if (minimum == selectableCards.size())
            cardsSelected(game, selectableCards);
        else {
            game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new CardsSelectionDecision(1, getText(game), selectableCards, minimum, maximum) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Set<LotroPhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                            cardsSelected(game, selectedCards);
                        }
                    }
            );
        }

        return new FullEffectResult(success);
    }

    protected abstract void cardsSelected(DefaultGame game, Collection<LotroPhysicalCard> selectedCards);
}
