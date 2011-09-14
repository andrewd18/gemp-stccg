package com.gempukku.lotro.cards.set1.site;

import com.gempukku.lotro.cards.AbstractSite;
import com.gempukku.lotro.cards.effects.ExertCharacterEffect;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: The Fellowship of the Ring
 * Twilight Cost: 1
 * Type: Site
 * Site: 2
 * Game Text: Marsh. Each time a Hobbit moves to Midgewater Marshes, that Hobbit must exert.
 */
public class Card1_332 extends AbstractSite {
    public Card1_332() {
        super("Midgewater Marshes", 2, 1, Direction.LEFT);
        addKeyword(Keyword.MARSH);
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (effectResult.getType() == EffectResult.Type.WHEN_MOVE_TO
                && game.getGameState().getCurrentSite() == self) {
            List<RequiredTriggerAction> actions = new LinkedList<RequiredTriggerAction>();
            List<PhysicalCard> hobbits = Filters.filterActive(game.getGameState(), game.getModifiersQuerying(), Filters.race(Race.HOBBIT), Filters.type(CardType.COMPANION));
            for (PhysicalCard hobbit : hobbits) {
                RequiredTriggerAction action = new RequiredTriggerAction(self, null, "Each Hobbit must exert");
                action.addEffect(new ExertCharacterEffect(hobbit.getOwner(), hobbit));
                actions.add(action);
            }
            return actions;
        }
        return null;
    }
}
