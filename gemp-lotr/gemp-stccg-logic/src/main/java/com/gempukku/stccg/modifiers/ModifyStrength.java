package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.requirement.Requirement;
import org.json.simple.JSONObject;

public class ModifyStrength implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "requires", "amount");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(object.get("amount"), environment);
        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final String filter = FieldUtils.getString(object.get("filter"), "filter");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return (actionContext) -> {
                    final Evaluator evaluator = valueSource.getEvaluator(actionContext);
                    return new StrengthModifier(actionContext.getSource(),
                            filterableSource.getFilterable(actionContext),
                            new RequirementCondition(requirements, actionContext), evaluator);
        };
    }
}
