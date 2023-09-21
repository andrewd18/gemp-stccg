package com.gempukku.lotro.effectprocessor;

import com.gempukku.lotro.cards.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.actions.DefaultActionSource;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.requirement.trigger.TriggerChecker;
import org.json.simple.JSONObject;

public class ResponseEventEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "trigger", "requires", "cost", "effect");

        final JSONObject[] triggerArray = FieldUtils.getObjectArray(value.get("trigger"), "trigger");

        for (JSONObject trigger : triggerArray) {
            final TriggerChecker triggerChecker = environment.getTriggerCheckerFactory().getTriggerChecker(trigger, environment);
            final boolean before = triggerChecker.isBefore();

            DefaultActionSource triggerActionSource = new DefaultActionSource();
            triggerActionSource.addPlayRequirement(triggerChecker);
            EffectUtils.processRequirementsCostsAndEffects(value, environment, triggerActionSource);

            if (before) {
                blueprint.appendOptionalInHandBeforeAction(triggerActionSource);
            } else {
                blueprint.appendOptionalInHandAfterAction(triggerActionSource);
            }
        }
    }
}
