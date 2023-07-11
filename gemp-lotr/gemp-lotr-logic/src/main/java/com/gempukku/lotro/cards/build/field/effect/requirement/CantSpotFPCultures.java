package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.game.GameUtils;
import org.json.simple.JSONObject;

public class CantSpotFPCultures implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "amount");
        final int amount = FieldUtils.getInteger(object.get("amount"), "amount");

        return (actionContext) -> GameUtils.getSpottableFPCulturesCount(actionContext.getGame(), actionContext.getPerformingPlayer()) < amount;
    }
}
