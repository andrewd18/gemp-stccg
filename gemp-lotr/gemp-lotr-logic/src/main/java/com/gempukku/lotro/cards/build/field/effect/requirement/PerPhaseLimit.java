package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.game.timing.PlayConditions;
import org.json.simple.JSONObject;

public class PerPhaseLimit implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "limit", "perPlayer");

        final int limit = FieldUtils.getInteger(object.get("limit"), "limit", 1);
        final boolean perPlayer = FieldUtils.getBoolean(object.get("perPlayer"), "perPlayer", false);

        return (actionContext) -> {
            if (perPlayer)
                return PlayConditions.checkPhaseLimit(actionContext.getGame(), actionContext.getSource(), actionContext.getPerformingPlayer() + "_", limit);
            else
                return PlayConditions.checkPhaseLimit(actionContext.getGame(), actionContext.getSource(), limit);
        };
    }
}
