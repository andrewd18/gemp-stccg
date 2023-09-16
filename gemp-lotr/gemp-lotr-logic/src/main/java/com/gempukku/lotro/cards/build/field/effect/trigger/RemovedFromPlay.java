package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.DefaultActionContext;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.effects.results.DiscardCardsFromPlayResult;
import com.gempukku.lotro.effects.results.ForEachKilledResult;
import com.gempukku.lotro.effects.results.ReturnCardsToHandResult;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.game.TriggerConditions;
import org.json.simple.JSONObject;

public class RemovedFromPlay implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter", "memorize");

        final String filter = FieldUtils.getString(value.get("filter"), "filter", "any");
        final String memorize = FieldUtils.getString(value.get("memorize"), "memorize");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return new TriggerChecker<>() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(DefaultActionContext<DefaultGame> actionContext) {
                final Filterable filterable = filterableSource.getFilterable(actionContext);
                final boolean killResult = TriggerConditions.forEachKilled(actionContext.getGame(), actionContext.getEffectResult(), filterable);
                if (killResult && memorize != null) {
                    final LotroPhysicalCard killedCard = ((ForEachKilledResult) actionContext.getEffectResult()).getKilledCard();
                    actionContext.setCardMemory(memorize, killedCard);
                }

                final boolean discardResult = TriggerConditions.forEachDiscardedFromPlay(actionContext.getGame(), actionContext.getEffectResult(), filterable);
                if (discardResult && memorize != null) {
                    final LotroPhysicalCard discardedCard = ((DiscardCardsFromPlayResult) actionContext.getEffectResult()).getDiscardedCard();
                    actionContext.setCardMemory(memorize, discardedCard);
                }

                final boolean returnedResult = TriggerConditions.forEachReturnedToHand(actionContext.getGame(), actionContext.getEffectResult(), filterable);
                if (returnedResult && memorize != null) {
                    final LotroPhysicalCard returnedCard = ((ReturnCardsToHandResult) actionContext.getEffectResult()).getReturnedCard();
                    actionContext.setCardMemory(memorize, returnedCard);
                }
                return killResult || discardResult || returnedResult;
            }
        };
    }
}
