package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.modifiers.AbstractModifier;
import com.gempukku.lotro.modifiers.ModifierEffect;
import com.gempukku.lotro.modifiers.evaluator.Evaluator;
import org.json.simple.JSONObject;

public class ModifyPlayOnCost implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "requires", "filter", "on", "amount");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final String filter = FieldUtils.getString(object.get("filter"), "filter");
        final String onFilter = FieldUtils.getString(object.get("on"), "on");

        final Requirement[] conditions = environment.getRequirementFactory().getRequirements(conditionArray, environment);
        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final FilterableSource onFilterableSource = environment.getFilterFactory().generateFilter(onFilter, environment);
        final ValueSource amountSource = ValueResolver.resolveEvaluator(object.get("amount"), environment);

        return actionContext -> {
            final Filterable filterable = filterableSource.getFilterable(actionContext);
            final Filterable onFilterable = onFilterableSource.getFilterable(actionContext);
            final RequirementCondition requirementCondition = new RequirementCondition(conditions, actionContext);
            final Evaluator evaluator = amountSource.getEvaluator(actionContext);
            return new AbstractModifier(actionContext.getSource(), "Cost to play on is modified", filterable,
                    requirementCondition, ModifierEffect.TWILIGHT_COST_MODIFIER) {
                @Override
                public int getTwilightCostModifier(DefaultGame game, LotroPhysicalCard physicalCard, LotroPhysicalCard target, boolean ignoreRoamingPenalty) {
                    if (target != null && Filters.and(onFilterable).accepts(game, target))
                        return evaluator.evaluateExpression(game, null);
                    return 0;
                }
            };
        };
    }
}
