package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.game.actions.CostToEffectAction;
import com.gempukku.lotro.game.effects.DrawCardsEffect;
import com.gempukku.lotro.game.modifiers.evaluator.Evaluator;
import com.gempukku.lotro.game.timing.Effect;
import org.json.simple.JSONObject;

public class DrawCards implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "count", "player");

        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player, environment);
        final ValueSource count = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);

        return new DelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final String drawPlayer = playerSource.getPlayer(actionContext);
                final Evaluator evaluator = count.getEvaluator(null);
                final int cardCount = evaluator.evaluateExpression(actionContext.getGame(), null);
                return actionContext.getGame().getGameState().getDeck(drawPlayer).size() >= cardCount;
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final String drawPlayer = playerSource.getPlayer(actionContext);
                final Evaluator evaluator = count.getEvaluator(actionContext);
                final int cardsDrawn = evaluator.evaluateExpression(actionContext.getGame(), null);
                return new DrawCardsEffect(action, drawPlayer, cardsDrawn);
            }
        };
    }

}
