package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.modifiers.CantDiscardFromPlayModifier;
import org.json.simple.JSONObject;

public class CantBeDiscarded implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "requires", "by");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final String filter = FieldUtils.getString(object.get("filter"), "filter");
        final String byFilter = FieldUtils.getString(object.get("by"), "by", "any");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final FilterableSource byFilterableSource = environment.getFilterFactory().generateFilter(byFilter, environment);
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return (actionContext) -> new CantDiscardFromPlayModifier(actionContext.getSource(),
                "Can't be discarded",
                new RequirementCondition(requirements, actionContext),
                filterableSource.getFilterable(actionContext),
                byFilterableSource.getFilterable(actionContext));
    }
}
