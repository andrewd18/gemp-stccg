package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.actions.lotronly.CostToEffectAction;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.DefaultActionContext;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.PlayerSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.RevealHandEffect;
import com.gempukku.lotro.game.DefaultGame;
import org.json.simple.JSONObject;

import java.util.Collection;

public class RevealHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "hand", "memorize");

        final String player = FieldUtils.getString(effectObject.get("hand"), "hand", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");

        return new DelayedAppender<>() {
            @Override
            public boolean isPlayableInFull(DefaultActionContext<DefaultGame> actionContext) {
                final DefaultGame game = actionContext.getGame();
                final String revealingPlayer = playerSource.getPlayer(actionContext);
                return game.getModifiersQuerying().canLookOrRevealCardsInHand(game, revealingPlayer, actionContext.getPerformingPlayer());
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                final String revealingPlayer = playerSource.getPlayer(actionContext);
                return new RevealHandEffect(actionContext.getSource(), actionContext.getPerformingPlayer(), revealingPlayer) {
                    @Override
                    protected void cardsRevealed(Collection<? extends LotroPhysicalCard> cards) {
                        if (memorize != null)
                            actionContext.setCardMemory(memorize, cards);
                    }
                };
            }
        };
    }

}
