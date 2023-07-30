package com.gempukku.lotro.effects.choose;

import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.effects.ChooseActiveCardsEffect;
import com.gempukku.lotro.effects.DiscardCardsFromPlayEffect;
import com.gempukku.lotro.actions.lotronly.CostToEffectAction;
import com.gempukku.lotro.actions.lotronly.SubAction;
import com.gempukku.lotro.actions.Action;

import java.util.Collection;

public class ChooseAndDiscardCardsFromPlayEffect extends ChooseActiveCardsEffect {
    private final Action _action;
    private final String _playerId;
    private CostToEffectAction _resultSubAction;

    public ChooseAndDiscardCardsFromPlayEffect(Action action, String playerId, int minimum, int maximum, Filterable... filters) {
        super(action.getActionSource(), playerId, "Choose cards to discard", minimum, maximum, filters);
        _action = action;
        _playerId = playerId;
    }

    @Override
    protected Filter getExtraFilterForPlaying(DefaultGame game) {
        if (_action.getActionSource() == null)
            return Filters.any;
        return Filters.canBeDiscarded(_playerId, _action.getActionSource());
    }

    @Override
    protected void cardsSelected(DefaultGame game, Collection<LotroPhysicalCard> cards) {
        _resultSubAction = new SubAction(_action);
        _resultSubAction.appendEffect(new DiscardCardsFromPlayEffect(_playerId, _action.getActionSource(), Filters.in(cards)) {
            @Override
            protected void forEachDiscardedByEffectCallback(Collection<LotroPhysicalCard> discardedCards) {
                ChooseAndDiscardCardsFromPlayEffect.this.forEachDiscardedByEffectCallback(discardedCards);
            }
        });
        game.getActionsEnvironment().addActionToStack(_resultSubAction);
        cardsToBeDiscardedCallback(cards);
    }

    protected void cardsToBeDiscardedCallback(Collection<LotroPhysicalCard> cards) {

    }

    protected void forEachDiscardedByEffectCallback(Collection<LotroPhysicalCard> cards) {

    }

    @Override
    public boolean wasCarriedOut() {
        return super.wasCarriedOut() && _resultSubAction != null && _resultSubAction.wasCarriedOut();
    }
}
