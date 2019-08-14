package com.gempukku.lotro.cards.set7.site;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractSite;
import com.gempukku.lotro.logic.effects.CheckPhaseLimitEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndPlayCardFromDeckEffect;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Return of the King
 * Type: Site
 * Site: 1K
 * Game Text: Fellowship: Spot a Dwarf to play a [DWARVEN] condition from your draw deck (limit once per turn).
 */
public class Card7_329 extends AbstractSite {
    public Card7_329() {
        super("Dunharrow Plateau", SitesBlock.KING, 1, 0, Direction.LEFT);
    }

    @Override
    public List<? extends Action> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseSiteDuringPhase(game, Phase.FELLOWSHIP, self)
                && PlayConditions.canSpot(game, Race.DWARF)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendEffect(
                    new CheckPhaseLimitEffect(action, self, 1, Phase.FELLOWSHIP,
                            new ChooseAndPlayCardFromDeckEffect(playerId, Culture.DWARVEN, CardType.CONDITION)));
            return Collections.singletonList(action);
        }
        return null;
    }
}
