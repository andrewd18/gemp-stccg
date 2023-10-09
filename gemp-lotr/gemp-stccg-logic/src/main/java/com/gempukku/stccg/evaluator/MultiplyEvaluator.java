package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class MultiplyEvaluator implements Evaluator {
    private final Evaluator _source;
    private final Evaluator _multiplier;

    public MultiplyEvaluator(Evaluator multiplier, Evaluator source) {
        _multiplier = multiplier;
        _source = source;
    }

    public MultiplyEvaluator(int multiplier, Evaluator source) {
        _multiplier = new ConstantEvaluator(multiplier);
        _source = source;
    }

    @Override
    public int evaluateExpression(DefaultGame game, PhysicalCard self) {
        return _multiplier.evaluateExpression(game, self) * _source.evaluateExpression(game, self);
    }
}
