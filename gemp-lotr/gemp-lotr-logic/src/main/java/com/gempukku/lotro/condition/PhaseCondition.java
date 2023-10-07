package com.gempukku.lotro.condition;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.DefaultGame;

public class PhaseCondition implements Condition {
    private final Phase _phase;

    public PhaseCondition(Phase phase) {
        _phase = phase;
    }

    @Override
    public boolean isFulfilled(DefaultGame game) {
        return _phase == null || game.getGameState().getCurrentPhase() == _phase;
    }
}
