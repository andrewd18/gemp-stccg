package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.actions.lotronly.CostToEffectAction;
import com.gempukku.lotro.effects.PutCardFromStackedIntoHandEffect;
import com.gempukku.lotro.effects.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class PutStackedCardsIntoHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "count", "filter", "on");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "choose(any)");
        final String on = FieldUtils.getString(effectObject.get("on"), "on", "any");
        final FilterableSource onFilterSource = environment.getFilterFactory().generateFilter(on, environment);

        MultiEffectAppender result = new MultiEffectAppender();
        result.addEffectAppender(
                CardResolver.resolveStackedCards(filter, valueSource, onFilterSource, "_temp", "you", "Choose cards to take into hand", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected List<? extends Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends LotroPhysicalCard> cardsToPutToHand = actionContext.getCardsFromMemory("_temp");
                        List<Effect> result = new LinkedList<>();
                        for (LotroPhysicalCard physicalCard : cardsToPutToHand)
                            result.add(new PutCardFromStackedIntoHandEffect(physicalCard));

                        return result;
                    }
                });

        return result;
    }

}
