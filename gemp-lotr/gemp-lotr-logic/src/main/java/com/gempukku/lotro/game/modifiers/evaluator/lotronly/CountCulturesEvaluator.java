package com.gempukku.lotro.game.modifiers.evaluator.lotronly;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.game.modifiers.evaluator.Evaluator;

import java.util.HashSet;
import java.util.Set;

public class CountCulturesEvaluator implements Evaluator {
    private final Filterable[] _filters;
    private final int _multiplier;
    private final int _over;

    public CountCulturesEvaluator(Filterable... filters) {
        this(0, filters);
    }

    public CountCulturesEvaluator(int over, Filterable... filters) {
        this(over, 1, filters);
    }

    public CountCulturesEvaluator(int over, int multiplier, Filterable... filters) {
        _over = over;
        _multiplier = multiplier;
        _filters = filters;
    }

    @Override
    public int evaluateExpression(DefaultGame game, LotroPhysicalCard self) {
        Set<Culture> cultures = new HashSet<>();
        for (LotroPhysicalCard physicalCard : Filters.filterActive(game, _filters)) {
            Culture culture = physicalCard.getBlueprint().getCulture();
            if (culture != null)
                cultures.add(culture);
        }
        return _multiplier * Math.max(0, cultures.size() - _over);
    }
}
