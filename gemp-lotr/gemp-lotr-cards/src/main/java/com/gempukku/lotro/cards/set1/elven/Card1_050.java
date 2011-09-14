package com.gempukku.lotro.cards.set1.elven;

import com.gempukku.lotro.cards.AbstractCompanion;
import com.gempukku.lotro.cards.PlayConditions;
import com.gempukku.lotro.cards.effects.AddUntilEndOfPhaseModifierEffect;
import com.gempukku.lotro.cards.effects.ExertCharacterEffect;
import com.gempukku.lotro.cards.modifiers.DoesNotAddToArcheryTotalModifier;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.DefaultCostToEffectAction;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.effects.WoundCharacterEffect;
import com.gempukku.lotro.logic.timing.Action;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Fellowship of the Ring
 * Side: Free
 * Culture: Elven
 * Twilight Cost: 2
 * Type: Companion • Elf
 * Strength: 6
 * Vitality: 3
 * Resistance: 6
 * Signet: Frodo
 * Game Text: Archer. Archery: Exert Legolas to wound a minion; Legolas does not add to the fellowship archery total.
 */
public class Card1_050 extends AbstractCompanion {
    public Card1_050() {
        super(2, 6, 3, Culture.ELVEN, Race.ELF, Signet.FRODO, "Legolas", true);
        addKeyword(Keyword.ARCHER);
    }

    @Override
    protected List<? extends Action> getExtraInPlayPhaseActions(final String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game.getGameState(), Phase.ARCHERY, self)
                && PlayConditions.canExert(game.getGameState(), game.getModifiersQuerying(), self)) {
            final DefaultCostToEffectAction action = new DefaultCostToEffectAction(self, Keyword.ARCHERY, "Exert Legolas to wound a minion");
            action.addCost(new ExertCharacterEffect(playerId, self));
            action.addCost(
                    new AddUntilEndOfPhaseModifierEffect(
                            new DoesNotAddToArcheryTotalModifier(self, Filters.sameCard(self)), Phase.ARCHERY));
            action.addEffect(
                    new ChooseActiveCardEffect(playerId, "Choose a minion", Filters.type(CardType.MINION)) {
                        @Override
                        protected void cardSelected(PhysicalCard minion) {
                            action.addEffect(new WoundCharacterEffect(playerId, minion));
                        }
                    });

            return Collections.singletonList(action);
        }
        return null;
    }
}
