package com.gempukku.lotro.effects;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;

public class SpotEffect extends AbstractEffect {
    private final int _count;
    private final Filterable[] _filters;

    public SpotEffect(int count, Filterable... filters) {
        _count = count;
        _filters = filters;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return Filters.canSpot(game, _count, _filters);
    }

    @Override
    public String getText(DefaultGame game) {
        return "Spot cards";
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        if (isPlayableInFull(game)) {
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
