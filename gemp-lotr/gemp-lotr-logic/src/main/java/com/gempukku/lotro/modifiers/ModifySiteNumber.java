package com.gempukku.lotro.modifiers;

import com.gempukku.lotro.cards.*;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.ValueResolver;
import com.gempukku.lotro.modifiers.lotronly.MinionSiteNumberModifier;
import com.gempukku.lotro.evaluator.Evaluator;
import com.gempukku.lotro.requirement.Requirement;
import org.json.simple.JSONObject;

public class ModifySiteNumber implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "requires", "amount");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final String filter = FieldUtils.getString(object.get("filter"), "filter");
        final ValueSource amount = ValueResolver.resolveEvaluator(object.get("amount"), environment);

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return actionContext -> {
            final Evaluator evaluator = amount.getEvaluator(actionContext);
            return new MinionSiteNumberModifier(actionContext.getSource(),
                    filterableSource.getFilterable(actionContext),
                    new RequirementCondition(requirements, actionContext), evaluator);
        };
    }
}
