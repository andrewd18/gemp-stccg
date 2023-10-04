package com.gempukku.lotro.effectprocessor;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.DefaultActionContext;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.actions.DefaultActionSource;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.AbstractEffectAppender;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.IncrementPhaseLimitEffect;
import com.gempukku.lotro.effects.IncrementTurnLimitEffect;
import com.gempukku.lotro.game.PlayConditions;
import org.json.simple.JSONObject;

public class ActivatedInDiscardEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "phase", "requires", "cost", "effect", "limitPerPhase", "limitPerTurn", "text");

        final String text = FieldUtils.getString(value.get("text"), "text");
        final String[] phaseArray = FieldUtils.getStringArray(value.get("phase"), "phase");
        final int limitPerPhase = FieldUtils.getInteger(value.get("limitPerPhase"), "limitPerPhase", 0);
        final int limitPerTurn = FieldUtils.getInteger(value.get("limitPerTurn"), "limitPerTurn", 0);

        if (phaseArray.length == 0)
            throw new InvalidCardDefinitionException("Unable to find phase for an activated effect");

        for (String phaseString : phaseArray) {
            final Phase phase = Phase.valueOf(phaseString.toUpperCase());

            DefaultActionSource actionSource = new DefaultActionSource();
            actionSource.setText(text);
            if (limitPerPhase > 0) {
                actionSource.addPlayRequirement(
                        (actionContext) -> PlayConditions.checkPhaseLimit(actionContext.getGame(), actionContext.getSource(), phase, limitPerPhase));
                actionSource.addCost(
                        new AbstractEffectAppender() {
                            @Override
                            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                                return new IncrementPhaseLimitEffect(actionContext.getSource(), phase, limitPerPhase);
                            }
                        });
            }
            if (limitPerTurn > 0) {
                actionSource.addPlayRequirement(
                        (actionContext) -> PlayConditions.checkTurnLimit(actionContext.getGame(), actionContext.getSource(), limitPerTurn));
                actionSource.addCost(
                        new AbstractEffectAppender() {
                            @Override
                            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                                return new IncrementTurnLimitEffect(actionContext.getSource(), limitPerTurn);
                            }
                        });
            }
            actionSource.addPlayRequirement(
                    (actionContext) -> PlayConditions.isPhase(actionContext.getGame(), phase));
            EffectUtils.processRequirementsCostsAndEffects(value, environment, actionSource);

            blueprint.appendInDiscardPhaseAction(actionSource);
        }
    }
}