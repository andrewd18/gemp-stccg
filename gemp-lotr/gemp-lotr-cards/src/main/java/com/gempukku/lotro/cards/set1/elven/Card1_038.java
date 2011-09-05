package com.gempukku.lotro.cards.set1.elven;

import com.gempukku.lotro.cards.AbstractEvent;
import com.gempukku.lotro.cards.actions.PlayEventAction;
import com.gempukku.lotro.cards.effects.AddUntilEndOfPhaseModifierEffect;
import com.gempukku.lotro.cards.effects.SpotEffect;
import com.gempukku.lotro.cards.modifiers.ArcheryTotalModifier;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.Action;

/**
 * Set: The Fellowship of the Ring
 * Side: Free
 * Culture: Elven
 * Twilight Cost: 0
 * Type: Event
 * Game Text: Archery: Spot an Elf archer companion to make the fellowship archery total +1.
 */
public class Card1_038 extends AbstractEvent {
    public Card1_038() {
        super(Side.FREE_PEOPLE, CardType.EVENT, Culture.ELVEN, "Double Shot", Phase.ARCHERY);
    }

    @Override
    public int getTwilightCost() {
        return 0;
    }

    @Override
    public boolean checkPlayRequirements(String playerId, LotroGame game, PhysicalCard self) {
        return Filters.canSpot(game.getGameState(), game.getModifiersQuerying(), Filters.keyword(Keyword.ELF), Filters.keyword(Keyword.ARCHER), Filters.type(CardType.COMPANION));
    }

    @Override
    public Action getPlayCardAction(String playerId, LotroGame game, PhysicalCard self) {
        PlayEventAction action = new PlayEventAction(self);
        action.addCost(new SpotEffect(Filters.and(Filters.keyword(Keyword.ELF), Filters.keyword(Keyword.ARCHER), Filters.type(CardType.COMPANION))));
        action.addEffect(new AddUntilEndOfPhaseModifierEffect(
                new ArcheryTotalModifier(self, Side.FREE_PEOPLE, 1), Phase.ARCHERY));

        return action;
    }
}
