package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.defaulteffect.ReturnCardsToHandEffect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import org.json.simple.JSONObject;

import java.util.Collection;

public class ReturnToHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter", "count", "player");

        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter");
        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCards(filter,
                        actionContext -> (Filter) (game, physicalCard) -> game.getModifiersQuerying().canBeReturnedToHand(physicalCard, actionContext.getSource()),
                        valueSource, "_temp", player, "Choose cards to return to hand", environment));
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory("_temp");
                        return new ReturnCardsToHandEffect(actionContext.getGame(), actionContext.getSource(), Filters.in(cardsFromMemory));
                    }
                });

        return result;
    }

}
