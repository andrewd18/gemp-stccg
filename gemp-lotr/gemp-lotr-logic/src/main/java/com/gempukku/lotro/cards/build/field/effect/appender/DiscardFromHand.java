package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.game.actions.lotronly.CostToEffectAction;
import com.gempukku.lotro.game.effects.DiscardCardsFromHandEffect;
import com.gempukku.lotro.game.effects.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;

public class DiscardFromHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "forced", "count", "filter", "memorize", "hand", "player");

        final String hand = FieldUtils.getString(effectObject.get("hand"), "hand", "you");
        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final boolean forced = FieldUtils.getBoolean(effectObject.get("forced"), "forced");
        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "choose(any)");
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");

        final PlayerSource handSource = PlayerResolver.resolvePlayer(hand, environment);
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player, environment);
        final ValueSource countSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCardsInHand(filter, countSource, memorize, player, hand, "Choose cards from hand to discard", true, environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends LotroPhysicalCard> cardsToDiscard = actionContext.getCardsFromMemory(memorize);
                        return new DiscardCardsFromHandEffect(actionContext.getSource(), handSource.getPlayer(actionContext), cardsToDiscard, forced);
                    }

                    @Override
                    public boolean isPlayableInFull(ActionContext actionContext) {
                        final DefaultGame game = actionContext.getGame();

                        final String handPlayer = handSource.getPlayer(actionContext);
                        final String choosingPlayer = playerSource.getPlayer(actionContext);
                        if (!handPlayer.equals(choosingPlayer)
                                && !game.getModifiersQuerying().canLookOrRevealCardsInHand(game, handPlayer, choosingPlayer))
                            return false;

                        return (!forced || game.getModifiersQuerying().canDiscardCardsFromHand(game, handPlayer, actionContext.getSource()));
                    }
                });

        return result;
    }

}
