package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.actions.lotronly.CostToEffectAction;
import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.modifiers.evaluator.Evaluator;
import org.json.simple.JSONObject;

public class PlaceTopCardOfDrawDeckOnTopOfPlayPile implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player", "count");

        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);
        final ValueSource count = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);

        return new DelayedAppender<>() {
            @Override
            public boolean isPlayableInFull(DefaultActionContext<DefaultGame> actionContext) {
                final String drawPlayer = playerSource.getPlayer(actionContext);
                final Evaluator evaluator = count.getEvaluator(null);
                final int cardCount = evaluator.evaluateExpression(actionContext.getGame(), null);
                return actionContext.getGame().getGameState().getDeck(drawPlayer).size() >= cardCount;
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                final String drawPlayer = playerSource.getPlayer(actionContext);
                final Evaluator evaluator = count.getEvaluator(actionContext);
                final int cardsDrawn = evaluator.evaluateExpression(actionContext.getGame(), null);
                return new PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect(drawPlayer, cardsDrawn);
            }
        };
    }

}