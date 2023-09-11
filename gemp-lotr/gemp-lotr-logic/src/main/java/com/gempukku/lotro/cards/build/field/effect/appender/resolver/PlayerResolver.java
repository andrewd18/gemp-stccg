package com.gempukku.lotro.cards.build.field.effect.appender.resolver;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.PlayerSource;
import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.rules.lotronly.LotroGameUtils;

import java.util.Locale;

public class PlayerResolver {
    public static PlayerSource resolvePlayer(String type) throws InvalidCardDefinitionException {

        if (type.equalsIgnoreCase("you"))
            return ActionContext::getPerformingPlayer;
        if (type.equalsIgnoreCase("owner"))
            return (actionContext) -> actionContext.getSource().getOwner();
/*        if (type.equalsIgnoreCase("all"))
            return (actionContext) -> "all"; */
        else if (type.equalsIgnoreCase("shadowPlayer") || type.equalsIgnoreCase("shadow")
                || type.equalsIgnoreCase("s"))
            return (actionContext) -> LotroGameUtils.getFirstShadowPlayer(actionContext.getGame());
        else if (type.equalsIgnoreCase("fp") || type.equalsIgnoreCase("freeps")
                || type.equalsIgnoreCase("free peoples") || type.equalsIgnoreCase("free people"))
            return ((actionContext) -> actionContext.getGame().getGameState().getCurrentPlayerId());
        else if (type.toLowerCase(Locale.ROOT).startsWith("ownerfrommemory(") && type.endsWith(")")) {
            String memory = type.substring(type.indexOf("(") + 1, type.lastIndexOf(")"));
            return (actionContext) -> {
                final LotroPhysicalCard cardFromMemory = actionContext.getCardFromMemory(memory);
                if (cardFromMemory != null)
                    return cardFromMemory.getOwner();
                else
                    // Sensible default
                    return actionContext.getPerformingPlayer();
            };
        }
        else if (type.toLowerCase().startsWith("frommemory(") && type.endsWith(")")) {
            String memory = type.substring(type.indexOf("(") + 1, type.lastIndexOf(")"));
            return (actionContext) -> actionContext.getValueFromMemory(memory);
        }
        throw new InvalidCardDefinitionException("Unable to resolve player resolver of type: " + type);
    }
}
