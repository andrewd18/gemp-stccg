package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.game.GameUtils;
import com.gempukku.lotro.game.actions.CostToEffectAction;
import com.gempukku.lotro.game.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.game.decisions.IntegerAwaitingDecision;
import com.gempukku.lotro.game.effects.PlayoutDecisionEffect;
import com.gempukku.lotro.game.timing.Effect;
import org.json.simple.JSONObject;

public class ChooseANumber implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player", "text", "from", "to", "memorize");

        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final String displayText = FieldUtils.getString(effectObject.get("text"), "text", "Choose a number");
        final ValueSource fromSource = ValueResolver.resolveEvaluator(effectObject.get("from"), 0, environment);
        final ValueSource toSource = ValueResolver.resolveEvaluator(effectObject.get("to"), 1, environment);

        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player, environment);

        if (memorize == null)
            throw new InvalidCardDefinitionException("ChooseANumber requires a field to memorize the value");

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                return new PlayoutDecisionEffect(actionContext.getPerformingPlayer(),
                    new IntegerAwaitingDecision(1, GameUtils.SubstituteText(displayText, actionContext),
                        fromSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null),
                        toSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null))
                {
                    @Override
                    public void decisionMade(String result) throws DecisionResultInvalidException {
                        actionContext.setValueToMemory(memorize, String.valueOf(getValidatedResult(result)));
                    }
                });
            }
        };
    }
}