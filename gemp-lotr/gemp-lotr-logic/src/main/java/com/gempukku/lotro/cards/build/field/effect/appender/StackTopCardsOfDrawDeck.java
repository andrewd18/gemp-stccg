package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.PlayerSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.actions.lotronly.CostToEffectAction;
import com.gempukku.lotro.game.effects.StackTopCardsFromDeckEffect;
import com.gempukku.lotro.game.effects.DoNothingEffect;
import com.gempukku.lotro.game.effects.Effect;
import org.json.simple.JSONObject;

public class StackTopCardsOfDrawDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "deck", "where", "count");

        final String deck = FieldUtils.getString(effectObject.get("deck"), "deck", "you");
        final String where = FieldUtils.getString(effectObject.get("where"), "where");
        final int count = FieldUtils.getInteger(effectObject.get("count"), "count", 1);

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(deck, environment);

        MultiEffectAppender result = new MultiEffectAppender();
        String cardMemory = "_temp";

        result.addEffectAppender(
                CardResolver.resolveCard(where, cardMemory, "you", "Choose card to stack on", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final PhysicalCard card = actionContext.getCardFromMemory(cardMemory);
                        if (card != null) {
                            final String deckId = playerSource.getPlayer(actionContext);

                            return new StackTopCardsFromDeckEffect(actionContext.getSource(), deckId, count, card);
                        } else
                            return new DoNothingEffect();
                    }
                });

        return result;
    }

}
