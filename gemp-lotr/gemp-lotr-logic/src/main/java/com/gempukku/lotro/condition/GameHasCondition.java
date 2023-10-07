package com.gempukku.lotro.condition;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;

public class GameHasCondition implements Condition {
    private final Filterable[] _filter;
    private final int _count;

    public GameHasCondition(Filterable... filter) {
        this(1, Filters.and(filter));
    }

    public GameHasCondition(int count, Filterable... filter) {
        _filter = filter;
        _count = count;
    }

    @Override
    public boolean isFulfilled(DefaultGame game) {
        return Filters.countActive(game, _filter)>=_count;
    }
}
