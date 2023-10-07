package com.gempukku.lotro.actions;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.game.DefaultGame;

public class SubAction extends AbstractCostToEffectAction {
    private final Action _action;

    public SubAction(Action action) {
        _action = action;
    }

    @Override
    public ActionType getActionType() {
        return _action.getActionType();
    }

    @Override
    public PhysicalCard getActionAttachedToCard() {
        return _action.getActionAttachedToCard();
    }

    @Override
    public PhysicalCard getActionSource() {
        return _action.getActionSource();
    }

    @Override
    public String getPerformingPlayer() {
        return _action.getPerformingPlayer();
    }

    @Override
    public void setPerformingPlayer(String playerId) {
        _action.setPerformingPlayer(playerId);
    }

    @Override
    public String getText() {
        return _action.getText();
    }

    @Override
    public Effect nextEffect(DefaultGame game) {
        if (isCostFailed()) {
            return null;
        } else {
            Effect cost = getNextCost();
            if (cost != null)
                return cost;

            return getNextEffect();
        }
    }
}
