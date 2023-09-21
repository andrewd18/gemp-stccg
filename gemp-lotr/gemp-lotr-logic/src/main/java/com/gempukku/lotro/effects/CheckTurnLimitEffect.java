package com.gempukku.lotro.effects;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.actions.SubAction;
import com.gempukku.lotro.actions.Action;

public class CheckTurnLimitEffect extends UnrespondableEffect {
    private final Action _action;
    private final LotroPhysicalCard _card;
    private final int _limit;
    private final Effect _limitedEffect;

    public CheckTurnLimitEffect(Action action, LotroPhysicalCard card, int limit, Effect limitedEffect) {
        _card = card;
        _limit = limit;
        _limitedEffect = limitedEffect;
        _action = action;
    }

    @Override
    public void doPlayEffect(DefaultGame game) {
        int incrementedBy = game.getModifiersQuerying().getUntilEndOfTurnLimitCounter(_card).incrementToLimit(_limit, 1);
        if (incrementedBy > 0) {
            SubAction subAction = new SubAction(_action);
            subAction.appendEffect(
                    _limitedEffect);
            game.getActionsEnvironment().addActionToStack(subAction);
        }
    }
}
