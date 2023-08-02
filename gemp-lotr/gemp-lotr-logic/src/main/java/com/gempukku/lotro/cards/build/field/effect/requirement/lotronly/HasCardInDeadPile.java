package com.gempukku.lotro.cards.build.field.effect.requirement.lotronly;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.requirement.RequirementProducer;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.rules.lotronly.LotroGameUtils;
import org.json.simple.JSONObject;

public class HasCardInDeadPile implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "count", "filter");

        final int count = FieldUtils.getInteger(object.get("count"), "count", 1);
        final String filter = FieldUtils.getString(object.get("filter"), "filter");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        return (actionContext) -> {
            final Filterable filterable = filterableSource.getFilterable(actionContext);
            final DefaultGame game = actionContext.getGame();

            // You can "spot" cards in dead pile, only for current player
            return Filters.filter(game.getGameState().getDeadPile(LotroGameUtils.getFreePeoplePlayer(game)), game, filterable).size() >= count;
        };
    }
}
