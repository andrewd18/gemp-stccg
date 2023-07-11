package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.ModifierSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Signet;
import com.gempukku.lotro.game.modifiers.AddSignetModifier;
import org.json.simple.JSONObject;

public class AddSignet implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "signet");

        final String filter = FieldUtils.getString(object.get("filter"), "filter");
        final Signet signet = FieldUtils.getEnum(Signet.class, object.get("signet"), "signet");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return actionContext ->
                new AddSignetModifier(actionContext.getSource(),
                        filterableSource.getFilterable(actionContext),
                        signet);
    }
}
