package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.timing.TriggerConditions;
import org.json.simple.JSONObject;

public class AboutToExert implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter");

        String filter = FieldUtils.getString(value.get("filter"), "filter");

        final FilterableSource affectedFilter = environment.getFilterFactory().generateFilter(filter, environment);

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                final Filterable affected = affectedFilter.getFilterable(actionContext);
                return TriggerConditions.isGettingExerted(actionContext.getEffect(), actionContext.getGame(), affected);
            }

            @Override
            public boolean isBefore() {
                return true;
            }
        };
    }
}
