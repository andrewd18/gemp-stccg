package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class LimitEvaluator implements Evaluator {
    private final Evaluator _limit;
    private final Evaluator _value;

    public LimitEvaluator(Evaluator valueEvaluator, Evaluator limitEvaluator) {
        _value = valueEvaluator;
        _limit = limitEvaluator;
    }

    @Override
    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
        return Math.min( _limit.evaluateExpression(game, cardAffected), _value.evaluateExpression(game, cardAffected));
    }
}
