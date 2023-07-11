package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.GameUtils;
import com.gempukku.lotro.game.actions.CostToEffectAction;
import com.gempukku.lotro.game.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.game.decisions.IntegerAwaitingDecision;
import com.gempukku.lotro.game.effects.PlayoutDecisionEffect;
import com.gempukku.lotro.game.timing.Effect;
import org.json.simple.JSONObject;

public class ChooseHowManyToSpot implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter", "memorize", "text");

        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter");
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");
        final String text = FieldUtils.getString(effectObject.get("text"), "text", "Choose how many to spot");

        if (memorize == null)
            throw new InvalidCardDefinitionException("ChooseHowManyToSpot requires a field to memorize the value");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final Filterable filterable = filterableSource.getFilterable(actionContext);
                final int count = Filters.countSpottable(actionContext.getGame(), filterable);
                return new PlayoutDecisionEffect(actionContext.getPerformingPlayer(),
                        new IntegerAwaitingDecision(1, GameUtils.SubstituteText(text, actionContext), 0, count, count) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                final int spotCount = getValidatedResult(result);
                                actionContext.setValueToMemory(memorize, String.valueOf(spotCount));
                            }
                        });
            }
        };
    }
}
