package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.effectappender.EffectAppender;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.requirement.Requirement;
import org.json.simple.JSONObject;

public class ExtraCostToPlay implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "requires", "cost", "filter");

        final FilterableSource filterableSource = environment.getFilterable(object);
        final Requirement[] requirements = environment.getRequirementsFromJSON(object);
        final EffectAppender[] effectAppenders = environment.getEffectAppendersFromJSON(object, "cost");

        return (actionContext) -> {
            final Filterable filterable = filterableSource.getFilterable(actionContext);
            final RequirementCondition condition = new RequirementCondition(requirements, actionContext);

            return new AbstractExtraPlayCostModifier(actionContext.getSource(), "Cost to play is modified", filterable, condition) {
                @Override
                public void appendExtraCosts(DefaultGame game, CostToEffectAction action, PhysicalCard card) {
                    for (EffectAppender effectAppender : effectAppenders)
                        effectAppender.appendEffect(true, action, actionContext);
                }

                @Override
                public boolean canPayExtraCostsToPlay(DefaultGame game, PhysicalCard card) {
                    for (EffectAppender effectAppender : effectAppenders) {
                        if (!effectAppender.isPlayableInFull(actionContext))
                            return false;
                    }

                    return true;
                }
            };
        };
    }
}
