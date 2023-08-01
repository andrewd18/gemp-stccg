package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.rules.GameUtils;
import com.gempukku.lotro.actions.lotronly.CostToEffectAction;
import com.gempukku.lotro.actions.lotronly.SubAction;
import com.gempukku.lotro.decisions.YesNoDecision;
import com.gempukku.lotro.effects.PlayoutDecisionEffect;
import com.gempukku.lotro.effects.StackActionEffect;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.UnrespondableEffect;
import org.json.simple.JSONObject;

public class PreventableAppenderProducer implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "text", "player", "effect", "cost");

        final String text = FieldUtils.getString(effectObject.get("text"), "text");
        final String player = FieldUtils.getString(effectObject.get("player"), "player");
        JSONObject[] effectArray = FieldUtils.getObjectArray(effectObject.get("effect"), "effect");
        JSONObject[] costArray = FieldUtils.getObjectArray(effectObject.get("cost"), "cost");

        if (text == null)
            throw new InvalidCardDefinitionException("Text is required for preventable effect");
        if (player == null)
            throw new InvalidCardDefinitionException("Player is required for preventable effect");

        final PlayerSource preventingPlayerSource = PlayerResolver.resolvePlayer(player, environment);
        final EffectAppender[] effectAppenders = environment.getEffectAppenderFactory().getEffectAppenders(effectArray, environment);
        final EffectAppender[] costAppenders = environment.getEffectAppenderFactory().getEffectAppenders(costArray, environment);

        return new DelayedAppender<>() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                if (areCostsPlayable(actionContext)) {
                    final String preventingPlayer = preventingPlayerSource.getPlayer(actionContext);

                    String textToUse = GameUtils.SubstituteText(text, actionContext);

                    SubAction subAction = new SubAction(action);
                    subAction.appendEffect(
                            new PlayoutDecisionEffect(preventingPlayer,
                                    new YesNoDecision(textToUse) {
                                        @Override
                                        protected void yes() {
                                            DelegateActionContext delegate = new DelegateActionContext(actionContext,
                                                    preventingPlayer, actionContext.getGame(), actionContext.getSource(), actionContext.getEffectResult(),
                                                    actionContext.getEffect());
                                            for (EffectAppender costAppender : costAppenders)
                                                costAppender.appendEffect(false, subAction, delegate);

                                            subAction.appendEffect(
                                                    new UnrespondableEffect() {
                                                        @Override
                                                        protected void doPlayEffect(DefaultGame game) {
                                                            // If the prevention was not carried out, need to do the original action anyway
                                                            if (!subAction.wasCarriedOut()) {
                                                                for (EffectAppender effectAppender : effectAppenders)
                                                                    effectAppender.appendEffect(false, subAction, actionContext);
                                                            }
                                                        }

                                                        @Override
                                                        public boolean wasCarriedOut() {
                                                            // Cheating a bit, we need to check, if the preventing effect was carried out,
                                                            // but have no way of doing this, as we can do that through subAction only,
                                                            // and this checking effect should be ALWAYS considered fine, even before it
                                                            // was done
                                                            return true;
                                                        }
                                                    });
                                        }

                                        @Override
                                        protected void no() {
                                            for (EffectAppender effectAppender : effectAppenders)
                                                effectAppender.appendEffect(false, subAction, actionContext);
                                        }
                                    }));
                    return new StackActionEffect(subAction);
                } else {
                    SubAction subAction = new SubAction(action);
                    for (EffectAppender effectAppender : effectAppenders)
                        effectAppender.appendEffect(false, subAction, actionContext);
                    return new StackActionEffect(subAction);
                }
            }

            private boolean areCostsPlayable(DefaultActionContext<DefaultGame> actionContext) {
                for (EffectAppender costAppender : costAppenders) {
                    if (!costAppender.isPlayableInFull(actionContext))
                        return false;
                }
                return true;
            }

            @Override
            public boolean isPlayableInFull(DefaultActionContext<DefaultGame> actionContext) {
                for (EffectAppender effectAppender : effectAppenders) {
                    if (!effectAppender.isPlayableInFull(actionContext))
                        return false;
                }

                return true;
            }
        };
    }
}
