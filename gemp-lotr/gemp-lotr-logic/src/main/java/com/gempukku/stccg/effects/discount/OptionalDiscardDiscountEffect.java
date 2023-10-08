package com.gempukku.stccg.effects.discount;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.Filterable;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.effects.OptionalEffect;
import com.gempukku.stccg.effects.choose.ChooseAndDiscardCardsFromPlayEffect;
import com.gempukku.stccg.effects.AbstractSubActionEffect;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.game.PlayConditions;

import java.util.Collection;

public class OptionalDiscardDiscountEffect extends AbstractSubActionEffect implements DiscountEffect {
    private final int _discount;
    private boolean _paid;
    private int _minimalDiscount;
    private final String _playerId;
    private final int _discardCount;
    private final Filterable[] _discardFilters;
    private final Action _action;

    public OptionalDiscardDiscountEffect(Action action, int discount, String playerId, int discardCount, Filterable... discardFilters) {
        _action = action;
        _discount = discount;
        _playerId = playerId;
        _discardCount = discardCount;
        _discardFilters = discardFilters;
    }

    @Override
    public int getDiscountPaidFor() {
        return _paid ? _discount : 0;
    }

    @Override
    public void setMinimalRequiredDiscount(int minimalDiscount) {
        _minimalDiscount = minimalDiscount;
    }

    @Override
    public String getText(DefaultGame game) {
        return "Discard cards to get a twilight discount";
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        if (PlayConditions.canDiscardFromPlay(_action.getActionSource(), game, _discardCount, _discardFilters))
            return _minimalDiscount <= _discount;
        return _minimalDiscount == 0;
    }

    @Override
    public int getMaximumPossibleDiscount(DefaultGame game) {
        return PlayConditions.canDiscardFromPlay(_action.getActionSource(), game, _discardCount, _discardFilters) ? _discount : 0;
    }

    @Override
    public void playEffect(DefaultGame game) {
        if (isPlayableInFull(game)) {
            SubAction subAction = new SubAction(_action);
            if (PlayConditions.canDiscardFromPlay(_action.getActionSource(), game, _discardCount, _discardFilters))
                subAction.appendEffect(
                        new OptionalEffect(subAction, _playerId,
                                new ChooseAndDiscardCardsFromPlayEffect(subAction, _playerId, _discardCount, _discardCount, _discardFilters) {
                                    @Override
                                    protected void cardsToBeDiscardedCallback(Collection<PhysicalCard> cards) {
                                        if (cards.size() == _discardCount) {
                                            _paid = true;
                                            discountPaidCallback(_discardCount);
                                        }
                                    }
                                }));
            processSubAction(game, subAction);
        }
    }

    protected void discountPaidCallback(int paid) {  }
}
