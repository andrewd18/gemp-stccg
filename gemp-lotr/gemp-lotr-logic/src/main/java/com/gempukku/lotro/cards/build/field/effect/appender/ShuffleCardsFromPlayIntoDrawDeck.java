package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.actions.CostToEffectAction;
import com.gempukku.lotro.game.effects.ShuffleCardsFromPlayIntoDeckEffect;
import com.gempukku.lotro.game.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;

public class ShuffleCardsFromPlayIntoDrawDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player", "filter", "count", "memorize");

        String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player, environment);
        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "choose(any)");
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCards(filter, valueSource, memorize, player, "Choose cards to shuffle into your deck", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cardsInPlay = actionContext.getCardsFromMemory(memorize);

                        return new ShuffleCardsFromPlayIntoDeckEffect(actionContext.getSource(), playerSource.getPlayer(actionContext), cardsInPlay);
                    }
                });

        return result;
    }

}
