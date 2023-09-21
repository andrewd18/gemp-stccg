package com.gempukku.lotro.modifiers.lotronly;

import com.gempukku.lotro.cards.*;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.modifiers.ModifierSourceProducer;
import com.gempukku.lotro.modifiers.RequirementCondition;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.requirement.Requirement;
import org.json.simple.JSONObject;


public class CantBeAssignedToSkirmishAgainst implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "requires", "fpCharacter", "minion", "side");

        final String filter = FieldUtils.getString(object.get("fpCharacter"), "fpCharacter");
        final String against = FieldUtils.getString(object.get("minion"), "minion");
        final Side side = FieldUtils.getEnum(Side.class, object.get("side"), "side");
        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");

        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);
        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final FilterableSource againstSource = environment.getFilterFactory().generateFilter(against, environment);

        return actionContext -> {
            final Filterable filterable = filterableSource.getFilterable(actionContext);
            final Filterable againstFilterable = againstSource.getFilterable(actionContext);
            return new CantBeAssignedAgainstModifier(actionContext.getSource(), side,
                    filterable, new RequirementCondition(requirements, actionContext), againstFilterable);
        };
    }

}
