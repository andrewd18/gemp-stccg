package com.gempukku.lotro.evaluator;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.game.DefaultGame;

public class ForEachTwilightEvaluator implements Evaluator {
    @Override
    public int evaluateExpression(DefaultGame game, LotroPhysicalCard self) {
        return game.getGameState().getTwilightPool();
    }
}