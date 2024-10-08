package com.gempukku.stccg.condition;

import com.gempukku.stccg.game.DefaultGame;

public class AndCondition implements Condition {
    private final Condition[] _conditions;

    public AndCondition(DefaultGame game, Condition... conditions) {
        _conditions = conditions;
    }

    @Override
    public boolean isFulfilled() {
        for (Condition condition : _conditions) {
            if (condition != null && !condition.isFulfilled())
                return false;
        }

        return true;
    }
}
