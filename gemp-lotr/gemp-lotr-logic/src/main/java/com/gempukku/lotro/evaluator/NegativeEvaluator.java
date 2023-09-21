package com.gempukku.lotro.evaluator;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.game.DefaultGame;

public class NegativeEvaluator implements Evaluator {
    private final Evaluator _source;

    public NegativeEvaluator(Evaluator source) {
        _source = source;
    }

    @Override
    public int evaluateExpression(DefaultGame game, LotroPhysicalCard self) {
        return -_source.evaluateExpression(game, self);
    }
}
