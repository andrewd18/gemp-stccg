package com.gempukku.lotro.rules.lotronly;

import com.gempukku.lotro.cards.LotroCardBlueprint;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.actions.AttachPermanentAction;
import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.actions.PlayEventAction;
import com.gempukku.lotro.actions.PlayPermanentAction;
import com.gempukku.lotro.rules.PlayUtils;
import com.gempukku.lotro.rules.RuleUtils;
import com.gempukku.lotro.game.PlayConditions;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

public class LotroPlayUtils extends PlayUtils {
    private static Zone getPlayToZone(LotroPhysicalCard card) {
        final CardType cardType = card.getBlueprint().getCardType();
        return switch (cardType) {
            case COMPANION -> Zone.FREE_CHARACTERS;
            case MINION -> Zone.SHADOW_CHARACTERS;
            default -> Zone.SUPPORT;
        };
    }

    public static final Map<Phase, Keyword> PhaseKeywordMap = ImmutableMap.copyOf(new HashMap<>() {{
        put(Phase.FELLOWSHIP, Keyword.FELLOWSHIP);
        put(Phase.SHADOW, Keyword.SHADOW);
        put(Phase.MANEUVER, Keyword.MANEUVER);
        put(Phase.ARCHERY, Keyword.ARCHERY);
        put(Phase.ASSIGNMENT, Keyword.ASSIGNMENT);
        put(Phase.SKIRMISH, Keyword.SKIRMISH);
        put(Phase.REGROUP, Keyword.REGROUP);
    }});

    private static Filter getFullAttachValidTargetFilter(final DefaultGame game, final LotroPhysicalCard card, int twilightModifier, int withTwilightRemoved) {
        return Filters.and(RuleUtils.getFullValidTargetFilter(card.getOwner(), game, card),
                (Filter) (game12, physicalCard) -> game12.getModifiersQuerying().canHavePlayedOn(game12, card, physicalCard),
                (Filter) (game1, physicalCard) -> {
                    if (card.getBlueprint().getSide() == Side.SHADOW) {
                        final int twilightCostOnTarget = game1.getModifiersQuerying().getTwilightCost(game1, card, physicalCard, twilightModifier, false);
                        int potentialDiscount = game1.getModifiersQuerying().getPotentialDiscount(game1, card);
                        return twilightCostOnTarget - potentialDiscount <= game1.getGameState().getTwilightPool() - withTwilightRemoved;
                    } else {
                        return true;
                    }
                });
    }


    public static CostToEffectAction getPlayCardAction(DefaultGame game, LotroPhysicalCard card, int twilightModifier, Filterable additionalAttachmentFilter, boolean ignoreRoamingPenalty) {
        final LotroCardBlueprint blueprint = card.getBlueprint();

        if (blueprint.getCardType() != CardType.EVENT) {
            final Filterable validTargetFilter = blueprint.getValidTargetFilter(card.getOwner(), game, card);
            if (validTargetFilter == null) {
                PlayPermanentAction action = new PlayPermanentAction(card, getPlayToZone(card), twilightModifier, ignoreRoamingPenalty);

                game.getModifiersQuerying().appendExtraCosts(game, action, card);
                game.getModifiersQuerying().appendPotentialDiscounts(game, action, card);

                return action;
            } else {
                final AttachPermanentAction action = new AttachPermanentAction(game, card, Filters.and(getFullAttachValidTargetFilter(game, card, twilightModifier, 0), additionalAttachmentFilter), twilightModifier);

                game.getModifiersQuerying().appendPotentialDiscounts(game, action, card);
                game.getModifiersQuerying().appendExtraCosts(game, action, card);

                return action;
            }
        } else {
            final PlayEventAction action = blueprint.getPlayEventCardAction(card.getOwner(), game, card);

            game.getModifiersQuerying().appendPotentialDiscounts(game, action, card);
            game.getModifiersQuerying().appendExtraCosts(game, action, card);

            return action;
        }
    }

    public static boolean checkPlayRequirements(DefaultGame game, LotroPhysicalCard card, Filterable additionalAttachmentFilter, int withTwilightRemoved, int twilightModifier, boolean ignoreRoamingPenalty, boolean ignoreCheckingDeadPile) {
        return checkPlayRequirements(game, card, additionalAttachmentFilter, withTwilightRemoved, twilightModifier, ignoreRoamingPenalty, ignoreCheckingDeadPile, false);
    }

    public static boolean checkPlayRequirements(DefaultGame game, LotroPhysicalCard card, Filterable additionalAttachmentFilter, int withTwilightRemoved, int twilightModifier, boolean ignoreRoamingPenalty, boolean ignoreCheckingDeadPile, boolean ignoreResponseEvents) {
        final LotroCardBlueprint blueprint = card.getBlueprint();

        // Check if card's own play requirements are met
        if (card.getBlueprint().playRequirementsNotMet(game, card))
            return false;

        twilightModifier -= game.getModifiersQuerying().getPotentialDiscount(game, card);

        // Check if there exists a legal target (if needed)
        final Filterable validTargetFilter = blueprint.getValidTargetFilter(card.getOwner(), game, card);
        Filterable finalTargetFilter = null;
        if (validTargetFilter != null) {
            finalTargetFilter = Filters.and(getFullAttachValidTargetFilter(game, card, twilightModifier, withTwilightRemoved), additionalAttachmentFilter);
            if (Filters.countActive(game, finalTargetFilter) == 0)
                return false;
        }

        // Check if player can play extra costs
        if (!game.getModifiersQuerying().canPayExtraCostsToPlay(game, card))
            return false;

        if (game.getModifiersQuerying().canNotPlayCard(game, card.getOwner(), card))
            return false;

        // Check uniqueness
        if (!PlayConditions.checkUniqueness(game, card, ignoreCheckingDeadPile))
            return false;

        if(blueprint.getCardType() == CardType.EVENT)
        {
            if(game.getModifiersQuerying().hasKeyword(game, card, Keyword.RESPONSE)) {
                if (ignoreResponseEvents)
                    return false;
            }
            else {
                final Keyword phaseKeyword = PhaseKeywordMap.get(game.getGameState().getCurrentPhase());
                if (phaseKeyword != null && !game.getModifiersQuerying().hasKeyword(game, card, phaseKeyword))
                    return false;
            }
        }

        return (blueprint.getSide() != Side.SHADOW || PlayConditions.canPayForShadowCard(game, card, finalTargetFilter, withTwilightRemoved, twilightModifier, ignoreRoamingPenalty));
    }
}
