package com.gempukku.stccg.effects.choose;

import com.gempukku.stccg.common.Filterable;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.effects.RemoveCardsFromTheGameEffect;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.Action;

import java.util.Collection;

public class ChooseAndRemoveFromTheGameCardsInPlayEffect extends ChooseActiveCardsEffect {
    private final Action _action;
    private final String _playerId;
    private CostToEffectAction _resultSubAction;

    public ChooseAndRemoveFromTheGameCardsInPlayEffect(Action action, String playerId, int minimum, int maximum, Filterable... filters) {
        super(action.getActionSource(), playerId, "Choose cards to remove from play", minimum, maximum, filters);
        _action = action;
        _playerId = playerId;
    }

    @Override
    protected void cardsSelected(DefaultGame game, Collection<PhysicalCard> cards) {
        _resultSubAction = new SubAction(_action);
        _resultSubAction.appendEffect(new RemoveCardsFromTheGameEffect(_playerId, _action.getActionSource(), cards));
        game.getActionsEnvironment().addActionToStack(_resultSubAction);
    }

    @Override
    public boolean wasCarriedOut() {
        return super.wasCarriedOut() && _resultSubAction != null && _resultSubAction.wasCarriedOut();
    }
}
