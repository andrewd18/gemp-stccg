package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.actions.SubAction;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.DefaultActionContext;
import com.gempukku.lotro.cards.DelegateActionContext;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.StackActionEffect;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.rules.GameUtils;
import org.json.simple.JSONObject;

public class ForEachPlayer implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "effect");

        final JSONObject[] effectArray = FieldUtils.getObjectArray(effectObject.get("effect"), "effect");
        final EffectAppender[] effectAppenders = environment.getEffectAppenderFactory().getEffectAppenders(effectArray, environment);

        return new DelayedAppender<>() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                SubAction subAction = new SubAction(action);
                for (String playerId : GameUtils.getAllPlayers(actionContext.getGame())) {
                    for (EffectAppender effectAppender : effectAppenders) {
                        DelegateActionContext playerActionContext = new DelegateActionContext(actionContext, playerId,
                                actionContext.getGame(), actionContext.getSource(), actionContext.getEffectResult(),
                                actionContext.getEffect());
                        effectAppender.appendEffect(cost, action, playerActionContext);
                    }
                }
                return new StackActionEffect(subAction);
            }

            @Override
            public boolean isPlayableInFull(DefaultActionContext<DefaultGame> actionContext) {
                for (String playerId : GameUtils.getAllPlayers(actionContext.getGame())) {
                    for (EffectAppender effectAppender : effectAppenders) {
                        DelegateActionContext playerActionContext = new DelegateActionContext(actionContext, playerId,
                                actionContext.getGame(), actionContext.getSource(), actionContext.getEffectResult(),
                                actionContext.getEffect());
                        if (!effectAppender.isPlayableInFull(playerActionContext))
                            return false;
                    }
                }

                return true;
            }
        };
    }
}
