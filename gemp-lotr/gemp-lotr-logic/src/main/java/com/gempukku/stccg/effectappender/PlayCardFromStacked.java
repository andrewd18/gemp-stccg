package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.rules.PlayUtils;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.effects.StackActionEffect;
import com.gempukku.stccg.evaluator.ConstantEvaluator;
import com.gempukku.stccg.effects.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;

public class PlayCardFromStacked implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter", "on", "removedTwilight", "nocheck");

        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "choose(any)");
        final String onFilter = FieldUtils.getString(effectObject.get("on"), "on");
        final int removedTwilight = FieldUtils.getInteger(effectObject.get("removedTwilight"), "removedTwilight", 0);
        final boolean noCheck = FieldUtils.getBoolean(effectObject.get("nocheck"), "nocheck", false);

        ValueSource countSource = new ConstantEvaluator(1);
        if(noCheck)
        {
            //This range will cause choice checks to succeed even if no valid choices are found (which is how draw deck
            // searching is supposed to work RAW).  However, we don't want this to be the default, else dual-choice cards
            // that play "from draw deck or discard pile" would allow empty sources to be chosen, which is NPE.
            countSource = ValueResolver.resolveEvaluator("0-1", 1, environment);
        }

        final FilterableSource onFilterableSource = (onFilter != null) ? environment.getFilterFactory().generateFilter(onFilter, environment) : null;

        MultiEffectAppender result = new MultiEffectAppender();
        result.setPlayabilityCheckedForEffect(true);

        result.addEffectAppender(
                CardResolver.resolveStackedCards(filter,
                        actionContext -> Filters.playable(actionContext.getGame()),
                        actionContext -> Filters.playable(actionContext.getGame(), removedTwilight, 0, false, false, true),
                        countSource, onFilterableSource, "_temp", "you", "Choose card to play", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cardsToPlay = actionContext.getCardsFromMemory("_temp");
                        if (cardsToPlay.size() == 1) {
                            final CostToEffectAction playCardAction = PlayUtils.getPlayCardAction(actionContext.getGame(), cardsToPlay.iterator().next(), 0, Filters.any, false);
                            return new StackActionEffect(playCardAction);
                        } else {
                            return null;
                        }
                    }
                });

        return result;
    }
}
