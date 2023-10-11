package com.gempukku.stccg.effects.choose;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.effects.defaulteffect.PutCardFromPlayOnTopOfDeckEffect;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.Action;

public class ChooseAndPutCardFromPlayOnTopOfDeckEffect extends ChooseActiveCardEffect {
    private final Action _action;
    private CostToEffectAction _resultSubAction;

    public ChooseAndPutCardFromPlayOnTopOfDeckEffect(DefaultGame game, Action action, String playerId, Filterable... filters) {
        super(game, action.getActionSource(), playerId, "Choose a card to put on top of deck", filters);
        _action = action;
    }

    @Override
    protected void cardSelected(DefaultGame game, PhysicalCard card) {
        _resultSubAction = new SubAction(_action);
        _resultSubAction.appendEffect(new PutCardFromPlayOnTopOfDeckEffect(game, card));
        game.getActionsEnvironment().addActionToStack(_resultSubAction);
    }

    @Override
    public boolean wasCarriedOut() {
        return super.wasCarriedOut() && _resultSubAction != null && _resultSubAction.wasCarriedOut();
    }
}
