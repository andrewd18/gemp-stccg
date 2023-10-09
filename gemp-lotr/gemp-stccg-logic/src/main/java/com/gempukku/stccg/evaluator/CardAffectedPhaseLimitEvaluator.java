package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.LimitCounter;

import java.util.HashMap;
import java.util.Map;

public class CardAffectedPhaseLimitEvaluator implements Evaluator {
    private final Map<Integer, Integer> _evaluatedForCard = new HashMap<>();

    private final String prefix;
    private final Evaluator evaluator;

    private final PhysicalCard source;
    private final Phase phase;
    private final int limit;

    public CardAffectedPhaseLimitEvaluator(PhysicalCard source, Phase phase, int limit, String prefix, Evaluator evaluator) {
        this.source = source;
        this.phase = phase;
        this.limit = limit;
        this.prefix = prefix;
        this.evaluator = evaluator;
    }

    private int evaluateOnce(DefaultGame game, PhysicalCard cardAffected) {
        LimitCounter limitCounter = game.getModifiersQuerying().getUntilEndOfPhaseLimitCounter(source, prefix + cardAffected.getCardId() + "_", phase);
        int internalResult = evaluator.evaluateExpression(game, cardAffected);
        return limitCounter.incrementToLimit(limit, internalResult);
    }

    @Override
    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
        Integer value = _evaluatedForCard.get(cardAffected.getCardId());
        if (value == null) {
            value = evaluateOnce(game, cardAffected);
            _evaluatedForCard.put(cardAffected.getCardId(), value);
        }
        return value;
    }
}
