package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import org.json.simple.JSONObject;

public class Transferred implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter", "to");

        final String filter = FieldUtils.getString(value.get("filter"), "filter", "any");
        final String toFilter = FieldUtils.getString(value.get("to"), "to", "any");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final FilterableSource toFilterableSource = environment.getFilterFactory().generateFilter(toFilter, environment);

        return new TriggerChecker() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(ActionContext actionContext) {
                return TriggerConditions.transferredCard(actionContext.getGame(),
                        actionContext.getEffectResult(),
                        filterableSource.getFilterable(actionContext),
                        null,
                        toFilterableSource.getFilterable(actionContext));
            }
        };
    }
}
