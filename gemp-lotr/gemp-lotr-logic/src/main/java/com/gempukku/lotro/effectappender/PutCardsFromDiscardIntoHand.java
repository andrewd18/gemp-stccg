package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.ValueSource;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.CardResolver;
import com.gempukku.lotro.effectappender.resolver.ValueResolver;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.PutCardFromDiscardIntoHandEffect;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class PutCardsFromDiscardIntoHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "count", "filter", "player", "discard", "memorize");

        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final String discard = FieldUtils.getString(effectObject.get("discard"), "disard", player);
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "choose(any)");
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCardsInDiscard(filter, valueSource, memorize, player, discard, "Choose cards from discard", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected List<? extends Effect> createEffects(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                        final Collection<? extends LotroPhysicalCard> cards = actionContext.getCardsFromMemory(memorize);
                        List<Effect> result = new LinkedList<>();
                        for (LotroPhysicalCard card : cards) {
                            result.add(
                                    new PutCardFromDiscardIntoHandEffect(card));
                        }

                        return result;
                    }
                });

        return result;

    }
}
