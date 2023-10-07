package com.gempukku.lotro.requirement;

import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.FilterableSource;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.condition.CardPlayedInCurrentPhaseCondition;
import org.json.simple.JSONObject;

public class PlayedCardThisPhase implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter");

        final String filter = FieldUtils.getString(object.get("filter"), "filter");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return (actionContext) -> {
            final Filterable filterable = filterableSource.getFilterable(actionContext);
            return new CardPlayedInCurrentPhaseCondition(filterable).isFulfilled(actionContext.getGame());
        };
    }
}
