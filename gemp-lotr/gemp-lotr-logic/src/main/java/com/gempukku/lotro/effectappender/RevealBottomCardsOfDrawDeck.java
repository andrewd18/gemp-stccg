package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.*;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.PlayerResolver;
import com.gempukku.lotro.effectappender.resolver.ValueResolver;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.RevealBottomCardsOfDrawDeckEffect;
import com.gempukku.lotro.game.DefaultGame;
import org.json.simple.JSONObject;

import java.util.List;

public class RevealBottomCardsOfDrawDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "deck", "count", "memorize");

        final String deck = FieldUtils.getString(effectObject.get("deck"), "deck", "you");
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(deck);

        return new DelayedAppender<>() {
            @Override
            public boolean isPlayableInFull(DefaultActionContext<DefaultGame> actionContext) {
                final String deckId = playerSource.getPlayer(actionContext);
                final int count = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);

                return actionContext.getGame().getGameState().getDeck(deckId).size() >= count;
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action,
                                          DefaultActionContext<DefaultGame> actionContext) {
                final String deckId = playerSource.getPlayer(actionContext);
                final int count = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);

                return new RevealBottomCardsOfDrawDeckEffect(actionContext.getSource(), deckId, count) {
                    @Override
                    protected void cardsRevealed(List<LotroPhysicalCard> revealedCards) {
                        if (memorize != null)
                            actionContext.setCardMemory(memorize, revealedCards);
                    }
                };
            }
        };
    }
}
