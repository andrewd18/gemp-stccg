package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.DefaultActionContext;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.PlayerSource;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.PlayerResolver;
import com.gempukku.lotro.decisions.YesNoDecision;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.PlayoutDecisionEffect;
import com.gempukku.lotro.rules.GameUtils;
import org.json.simple.JSONObject;

public class ChooseYesOrNo implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "text", "player", "memorize", "yes", "no");

        final String text = FieldUtils.getString(effectObject.get("text"), "text");
        if (text == null)
            throw new InvalidCardDefinitionException("Text is required for Yes or No decision");
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");
        final String yesAnswer = FieldUtils.getString(effectObject.get("yes"), "yes", "yes");
        final String noAnswer = FieldUtils.getString(effectObject.get("no"), "no", "no");
        PlayerSource playerSource = PlayerResolver.resolvePlayer(FieldUtils.getString(effectObject.get("player"), "player", "you"));

        return new DelayedAppender<>() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                return new PlayoutDecisionEffect(playerSource.getPlayer(actionContext),
                        new YesNoDecision(GameUtils.SubstituteText(text, actionContext)) {
                            @Override
                            protected void yes() {
                                actionContext.setValueToMemory(memorize, GameUtils.SubstituteText(yesAnswer, actionContext));
                            }

                            @Override
                            protected void no() {
                                actionContext.setValueToMemory(memorize, GameUtils.SubstituteText(noAnswer, actionContext));
                            }
                        });
            }
        };
    }
}
