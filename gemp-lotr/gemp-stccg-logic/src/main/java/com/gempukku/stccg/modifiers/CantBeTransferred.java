package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.requirement.Requirement;
import org.json.simple.JSONObject;

public class CantBeTransferred implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "filter", "requires");

        final FilterableSource filterableSource =
                environment.getFilterFactory().generateFilter(environment.getString(object.get("filter"), "filter"));
        final Requirement[] requirements =
                environment.getRequirementsFromJSON(object);

        return (actionContext) -> new CantBeTransferredModifier(actionContext.getSource(), filterableSource.getFilterable(actionContext), new RequirementCondition(requirements, actionContext));
    }
}
