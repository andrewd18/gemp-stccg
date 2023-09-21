package com.gempukku.lotro.actions;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.effects.Effect;

public class SubAction extends AbstractCostToEffectAction {
    private final Action _action;

    public SubAction(Action action) {
        _action = action;
    }

    @Override
    public Type getType() {
        return _action.getType();
    }

    @Override
    public LotroPhysicalCard getActionAttachedToCard() {
        return _action.getActionAttachedToCard();
    }

    @Override
    public LotroPhysicalCard getActionSource() {
        return _action.getActionSource();
    }

    @Override
    public Phase getActionTimeword() {
        return _action.getActionTimeword();
    }

    @Override
    public String getPerformingPlayer() {
        return _action.getPerformingPlayer();
    }

    @Override
    public void setActionTimeword(Phase phase) {
        _action.setActionTimeword(phase);
    }

    @Override
    public void setPerformingPlayer(String playerId) {
        _action.setPerformingPlayer(playerId);
    }

    @Override
    public String getText(DefaultGame game) {
        return _action.getText(game);
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
