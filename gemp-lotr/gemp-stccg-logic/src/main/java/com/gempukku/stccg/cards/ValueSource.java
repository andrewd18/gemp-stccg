package com.gempukku.stccg.cards;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.evaluator.Evaluator;

public interface ValueSource {
    Evaluator getEvaluator(ActionContext actionContext);

    default int getMinimum(ActionContext actionContext) {
        return getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
    }

    default int getMaximum(ActionContext actionContext) {
        return getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
    }

    default int evaluateExpression(ActionContext actionContext) {
        return evaluateExpression(actionContext, actionContext.getSource());
    }

    default int evaluateExpression(ActionContext actionContext, PhysicalCard cardAffected) {
        return getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), cardAffected);
    }

}