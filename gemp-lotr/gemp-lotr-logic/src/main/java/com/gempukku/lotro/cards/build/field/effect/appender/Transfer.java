package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.actions.lotronly.CostToEffectAction;
import com.gempukku.lotro.game.effects.TransferPermanentEffect;
import com.gempukku.lotro.game.effects.Effect;
import com.gempukku.lotro.game.rules.RuleUtils;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Transfer implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter", "where", "checkTarget", "memorizeTransferred", "memorizeTarget");

        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter");
        final String where = FieldUtils.getString(effectObject.get("where"), "where");
        final boolean checkTarget = FieldUtils.getBoolean(effectObject.get("checkTarget"), "checkTarget", false);
        final String memorizeTransferred = FieldUtils.getString(effectObject.get("memorizeTransferred"), "memorizeTransferred", "_temp1");
        final String memorizeTarget = FieldUtils.getString(effectObject.get("memorizeTarget"), "memorizeTarget", "_temp2");

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCard(filter, memorizeTransferred, "you", "Choose card to transfer", environment));
        result.addEffectAppender(
                CardResolver.resolveCards(where,
                        actionContext -> (Filter) (game, physicalCard) -> {
                            final Collection<? extends PhysicalCard> transferCard = actionContext.getCardsFromMemory(memorizeTransferred);
                            if (transferCard.isEmpty())
                                return false;
                            final PhysicalCard transferredCard = transferCard.iterator().next();
                            // Can't be transferred to card it's already attached to
                            if (transferredCard.getAttachedTo() == physicalCard)
                                return false;
                            // Optionally check target against original target filter
                            if (checkTarget && !RuleUtils.getFullValidTargetFilter(transferredCard.getOwner(), game, transferredCard).accepts(game, physicalCard))
                                return false;

                            return actionContext.getGame().getModifiersQuerying().canHaveTransferredOn(game, transferredCard, physicalCard);
                        }, actionContext -> Filters.any,
                        ValueResolver.resolveEvaluator(1, environment), memorizeTarget, "you", "Choose cards to transfer to", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected List<? extends Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> transferCard = actionContext.getCardsFromMemory(memorizeTransferred);
                        if (transferCard.isEmpty())
                            return null;

                        final Collection<? extends PhysicalCard> transferredToCard = actionContext.getCardsFromMemory(memorizeTarget);
                        if (transferredToCard.isEmpty())
                            return null;

                        return Collections.singletonList(new TransferPermanentEffect(transferCard.iterator().next(), transferredToCard.iterator().next()));
                    }
                });

        return result;
    }
}
