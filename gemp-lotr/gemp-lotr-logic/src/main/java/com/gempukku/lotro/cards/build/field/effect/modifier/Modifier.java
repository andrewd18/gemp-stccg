package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.ModifierSource;
import com.gempukku.lotro.cards.build.field.EffectProcessor;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import org.json.simple.JSONObject;

public class Modifier implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "modifier");

        JSONObject jsonObject = (JSONObject) value.get("modifier");
        final ModifierSource modifier = environment.getModifierSourceFactory().getModifier(jsonObject, environment);

        blueprint.appendInPlayModifier(modifier);
    }
}
