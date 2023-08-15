package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.DefaultActionContext;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.PlayerSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.game.TriggerConditions;
import org.json.simple.JSONObject;

public class Reconciles implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "player");

        final String player = FieldUtils.getString(value.get("player"), "player");

        PlayerSource playerSource = (player != null) ? PlayerResolver.resolvePlayer(player) : null;

        return new TriggerChecker() {
            @Override
            public boolean accepts(DefaultActionContext<DefaultGame> actionContext) {
                if (playerSource != null)
                    return TriggerConditions.reconciles(actionContext.getGame(), actionContext.getEffectResult(),
                            playerSource.getPlayer(actionContext));
                else
                    return TriggerConditions.reconciles(actionContext.getGame(), actionContext.getEffectResult(), null);
            }

            @Override
            public boolean isBefore() {
                return false;
            }
        };
    }
}
