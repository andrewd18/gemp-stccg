package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.actions.ActionSource;
import com.gempukku.stccg.actions.ActivateCardAction;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.DefaultActionSource;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.effectappender.AbstractEffectAppender;
import com.gempukku.stccg.effectprocessor.EffectUtils;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.IncrementPhaseLimitEffect;
import com.gempukku.stccg.effects.IncrementTurnLimitEffect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.PlayConditions;
import org.json.simple.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class AddActivated implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "phase", "requires", "cost", "effect", "limitPerPhase", "limitPerTurn", "text");

        final String filter = FieldUtils.getString(object.get("filter"), "filter");
        final String text = FieldUtils.getString(object.get("text"), "text");
        final String[] phaseArray = FieldUtils.getStringArray(object.get("phase"), "phase");
        final int limitPerPhase = FieldUtils.getInteger(object.get("limitPerPhase"), "limitPerPhase", 0);
        final int limitPerTurn = FieldUtils.getInteger(object.get("limitPerTurn"), "limitPerTurn", 0);
        final FilterableSource<DefaultGame> filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        if (phaseArray.length == 0)
            throw new InvalidCardDefinitionException("Unable to find phase for an activated effect");

        List<DefaultActionSource> actionSources = new LinkedList<>();

        for (String phaseString : phaseArray) {
            final Phase phase = Phase.valueOf(phaseString.toUpperCase());

            DefaultActionSource actionSource = new DefaultActionSource();
            actionSource.setText(text);
            if (limitPerPhase > 0) {
                actionSource.addPlayRequirement(
                        actionContext -> PlayConditions.checkPhaseLimit(
                                actionContext.getGame(), actionContext.getSource(), phase, limitPerPhase
                        ));
                actionSource.addCost(
                        new AbstractEffectAppender<>() {
                            @Override
                            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext<DefaultGame> actionContext) {
                                return new IncrementPhaseLimitEffect(actionContext.getSource(), phase, limitPerPhase);
                            }
                        });
            }
            if (limitPerTurn > 0) {
                actionSource.addPlayRequirement(
                        (actionContext) -> PlayConditions.checkTurnLimit(actionContext.getGame(), actionContext.getSource(), limitPerTurn));
                actionSource.addCost(
                        new AbstractEffectAppender<>() {
                            @Override
                            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext<DefaultGame> actionContext) {
                                return new IncrementTurnLimitEffect(actionContext.getSource(), limitPerTurn);
                            }
                        });
            }
            actionSource.addPlayRequirement(
                    (actionContext) -> PlayConditions.isPhase(actionContext.getGame(), phase));
            EffectUtils.processRequirementsCostsAndEffects(object, environment, actionSource);

            actionSources.add(actionSource);
        }

        return actionContext -> new AddActionToCardModifier(actionContext.getSource(), null, filterableSource.getFilterable(actionContext)) {
            @Override
            public List<? extends ActivateCardAction> getExtraPhaseAction(DefaultGame game, PhysicalCard card) {
                LinkedList<ActivateCardAction> result = new LinkedList<>();
                for (ActionSource inPlayPhaseAction : actionSources) {
                    DefaultActionContext actionContext = new DefaultActionContext(card.getOwner(), game, card, null, null);
                    if (inPlayPhaseAction.isValid(actionContext)) {
                        ActivateCardAction action = new ActivateCardAction(card);
                        inPlayPhaseAction.createAction(action, actionContext);
                        result.add(action);
                    }
                }

                return result;
            }

            @Override
            protected ActivateCardAction createExtraPhaseAction(DefaultGame game, PhysicalCard matchingCard) {
                return null;
            }
        };
    }
}
