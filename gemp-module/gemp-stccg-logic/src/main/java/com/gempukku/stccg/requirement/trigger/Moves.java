package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import org.json.simple.JSONObject;

public class Moves implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(value);

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                return TriggerConditions.moves(actionContext.getEffectResult());
            }

            @Override
            public boolean isBefore() {
                return false;
            }
        };
    }
}
