package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.modifiers.ArcheryTotalModifier;
import com.gempukku.lotro.game.modifiers.Modifier;
import org.json.simple.JSONObject;

public class ArcheryTotal implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "amount", "requires", "side");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final int amount = FieldUtils.getInteger(object.get("amount"), "amount");
        final Side side = FieldUtils.getEnum(Side.class, object.get("side"), "side");

        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return new ModifierSource() {
                    @Override
                    public Modifier getModifier(ActionContext actionContext) {
                        return new ArcheryTotalModifier(actionContext.getSource(), side,
                                new RequirementCondition(requirements, actionContext),
                                amount);
                    }
        };
    }
}
