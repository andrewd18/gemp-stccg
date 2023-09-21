package com.gempukku.lotro.effects.choose;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.effects.PutCardFromPlayOnTopOfDeckEffect;
import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.actions.SubAction;
import com.gempukku.lotro.actions.Action;

public class ChooseAndPutCardFromPlayOnTopOfDeckEffect extends ChooseActiveCardEffect {
    private final Action _action;
    private CostToEffectAction _resultSubAction;

    public ChooseAndPutCardFromPlayOnTopOfDeckEffect(Action action, String playerId, Filterable... filters) {
        super(action.getActionSource(), playerId, "Choose a card to put on top of deck", filters);
        _action = action;
    }

    @Override
    protected void cardSelected(DefaultGame game, LotroPhysicalCard card) {
        _resultSubAction = new SubAction(_action);
        _resultSubAction.appendEffect(new PutCardFromPlayOnTopOfDeckEffect(card));
        game.getActionsEnvironment().addActionToStack(_resultSubAction);
    }

    @Override
    public boolean wasCarriedOut() {
        return super.wasCarriedOut() && _resultSubAction != null && _resultSubAction.wasCarriedOut();
    }
}
