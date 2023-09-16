package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.ModifierSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.PossessionClass;
import com.gempukku.lotro.modifiers.lotronly.PossessionClassSpotModifier;
import org.json.simple.JSONObject;

public class ItemClassSpot implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "class");

        final PossessionClass spotClass = FieldUtils.getEnum(PossessionClass.class, object.get("class"), "class");

        return actionContext -> new PossessionClassSpotModifier(actionContext.getSource(), spotClass);
    }
}
