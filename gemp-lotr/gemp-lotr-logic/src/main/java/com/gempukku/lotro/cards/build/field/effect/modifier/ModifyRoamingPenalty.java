package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.modifiers.lotronly.RoamingPenaltyModifier;
import org.json.simple.JSONObject;

public class ModifyRoamingPenalty implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "requires", "amount");

        final int amount = FieldUtils.getInteger(object.get("amount"), "amount");
        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final String filter = FieldUtils.getString(object.get("filter"), "filter");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return actionContext -> new RoamingPenaltyModifier(actionContext.getSource(),
                filterableSource.getFilterable(actionContext),
                new RequirementCondition(requirements, actionContext), amount);
    }
}
