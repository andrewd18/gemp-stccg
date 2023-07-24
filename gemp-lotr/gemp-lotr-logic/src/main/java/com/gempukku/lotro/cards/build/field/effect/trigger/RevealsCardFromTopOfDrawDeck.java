package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.game.timing.TriggerConditions;
import com.gempukku.lotro.game.timing.results.RevealCardFromTopOfDeckResult;
import org.json.simple.JSONObject;

public class RevealsCardFromTopOfDrawDeck implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter");

        final String filter = FieldUtils.getString(value.get("filter"), "filter", "any");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return new TriggerChecker() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(ActionContext actionContext) {
                if (TriggerConditions.revealedCardsFromTopOfDeck(actionContext.getEffectResult(), actionContext.getPerformingPlayer())) {
                    RevealCardFromTopOfDeckResult revealCardFromTopOfDeckResult = (RevealCardFromTopOfDeckResult) actionContext.getEffectResult();
                    final Filterable filterable = filterableSource.getFilterable(actionContext);
                    final LotroPhysicalCard revealedCard = revealCardFromTopOfDeckResult.getRevealedCard();
                    return Filters.and(filterable).accepts(actionContext.getGame(), revealedCard);
                }
                return false;
            }
        };
    }
}
