package com.gempukku.lotro.cards.build.field.effect.appender.lotronly;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.DelayedAppender;
import com.gempukku.lotro.cards.build.field.effect.appender.MultiEffectAppender;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.TimeResolver;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.game.actions.lotronly.CostToEffectAction;
import com.gempukku.lotro.game.effects.AddUntilModifierEffect;
import com.gempukku.lotro.game.modifiers.OverwhelmedByMultiplierModifier;
import com.gempukku.lotro.game.modifiers.evaluator.ConstantEvaluator;
import com.gempukku.lotro.game.effects.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;

public class CantBeOverwhelmedMultiplier implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter", "until", "multiplier", "memorize");

        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter");
        final int multiplier = FieldUtils.getInteger(effectObject.get("multiplier"), "multiplier", 3);
        final TimeResolver.Time until = TimeResolver.resolveTime(effectObject.get("until"), "end(current)");
        final String memory = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCards(filter, new ConstantEvaluator(1), memory, "you", "Choose characters to apply overwhelm multiplier on", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends LotroPhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(memory);

                        return new AddUntilModifierEffect(
                                new OverwhelmedByMultiplierModifier(actionContext.getSource(), Filters.in(cardsFromMemory), multiplier), until);
                    }
                });

        return result;
    }

}
