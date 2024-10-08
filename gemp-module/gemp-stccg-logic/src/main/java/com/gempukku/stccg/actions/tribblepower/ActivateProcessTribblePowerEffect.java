package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.draw.DrawCardsEffect;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.actions.choose.ChooseAndPutCardsFromHandBeneathDrawDeckEffect;

import com.gempukku.stccg.filters.Filters;

public class ActivateProcessTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateProcessTribblePowerEffect(CostToEffectAction action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        SubAction subAction = _action.createSubAction();
        subAction.appendEffect(new DrawCardsEffect(_game, subAction, _activatingPlayer, 3));
        subAction.appendEffect(new ChooseAndPutCardsFromHandBeneathDrawDeckEffect(
                _game, subAction, _activatingPlayer, 2, false, Filters.any));
        return addActionAndReturnResult(_game, subAction);
    }
}