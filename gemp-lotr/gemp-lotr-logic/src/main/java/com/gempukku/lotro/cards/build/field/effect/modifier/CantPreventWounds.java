package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.game.modifiers.Modifier;
import com.gempukku.lotro.game.modifiers.ModifierFlag;
import com.gempukku.lotro.game.modifiers.SpecialFlagModifier;
import org.json.simple.JSONObject;

public class CantPreventWounds implements ModifierSourceProducer {

    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object,"requires");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return new ModifierSource() {
            @Override
            public Modifier getModifier(ActionContext actionContext) {
                return new SpecialFlagModifier(actionContext.getSource(),
                        new RequirementCondition(requirements, actionContext), ModifierFlag.CANT_PREVENT_WOUNDS);
            }
        };
    }
}
