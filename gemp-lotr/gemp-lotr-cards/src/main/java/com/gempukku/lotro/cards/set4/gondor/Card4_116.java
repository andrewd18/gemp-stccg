package com.gempukku.lotro.cards.set4.gondor;

import com.gempukku.lotro.cards.AbstractCompanion;
import com.gempukku.lotro.cards.modifiers.StrengthModifier;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.Condition;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.ModifiersQuerying;
import com.gempukku.lotro.logic.modifiers.TwilightCostModifier;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: The Two Towers
 * Side: Free
 * Culture: Gondor
 * Twilight Cost: 3
 * Type: Companion • Man
 * Strength: 7
 * Vitality: 3
 * Resistance: 6
 * Signet: Aragorn
 * Game Text: Ring-bound. Ranger. The twilight cost of each other [GONDOR] Man in your starting fellowship is -1.
 * While skirmishing a roaming minion, Faramir is strength +2.
 */
public class Card4_116 extends AbstractCompanion {
    public Card4_116() {
        super(3, 7, 3, Culture.GONDOR, Race.MAN, Signet.ARAGORN, "Faramir", true);
        addKeyword(Keyword.RING_BOUND);
        addKeyword(Keyword.RANGER);
    }

    @Override
    public List<? extends Modifier> getAlwaysOnModifiers(LotroGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
        modifiers.add(
                new TwilightCostModifier(self, Filters.and(Filters.not(Filters.sameCard(self)), Filters.culture(Culture.GONDOR), Filters.race(Race.MAN)),
                        new Condition() {
                            @Override
                            public boolean isFullfilled(GameState gameState, ModifiersQuerying modifiersQuerying) {
                                return gameState.getCurrentPhase() == Phase.GAME_SETUP;
                            }
                        }, -1));
        modifiers.add(
                new StrengthModifier(self, Filters.and(Filters.sameCard(self), Filters.inSkirmishAgainst(Filters.and(Filters.type(CardType.MINION), Filters.keyword(Keyword.ROAMING)))), 2));
        return modifiers;
    }
}
