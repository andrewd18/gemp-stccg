package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.ValueSource;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.PutCardFromZoneIntoHandEffect;
import com.gempukku.stccg.effects.ShuffleDeckEffect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class PutCardsFromDeckIntoHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "count", "filter", "shuffle", "reveal");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "choose(any)");
        final boolean shuffle = FieldUtils.getBoolean(effectObject.get("shuffle"), "shuffle", true);
        final boolean reveal = FieldUtils.getBoolean(effectObject.get("reveal"), "reveal", true);

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCardsInDeck(filter, valueSource, "_temp", "you", "Choose cards from deck", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected List<? extends Effect> createEffects(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cards = actionContext.getCardsFromMemory("_temp");
                        List<Effect> result = new LinkedList<>();
                        for (PhysicalCard card : cards) {
                            result.add(
                                    new PutCardFromZoneIntoHandEffect(card, Zone.DRAW_DECK, reveal));
                        }

                        return result;
                    }
                });
        if (shuffle)
            result.addEffectAppender(
                    new DelayedAppender() {
                @Override
                protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                    return new ShuffleDeckEffect(actionContext.getPerformingPlayer());
                }
            });

        return result;

    }
}
