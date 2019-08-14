package com.gempukku.lotro.cards.set40.moria;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.PlayUtils;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.effects.ChooseArbitraryCardsEffect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.ExtraFilters;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Title: Goblin Scavengers
 * Set: Second Edition
 * Side: Shadow
 * Culture: Moria
 * Twilight Cost: 3
 * Type: Minion - Goblin
 * Strength: 8
 * Vitality: 1
 * Home: 4
 * Card Number: 1RC170
 * Game Text: When you play this minion, you may play a weapon from your discard pile on a [MORIA] Goblin.
 */
public class Card40_170 extends AbstractMinion {
    public Card40_170() {
        super(3, 8, 1, 4, Race.GOBLIN, Culture.MORIA, "Goblin Scavengers");
    }

    @Override
    public List<OptionalTriggerAction> getOptionalAfterTriggers(final String playerId, final LotroGame game, EffectResult effectResult, final PhysicalCard self) {
        final Filter additionalAttachmentFilter = Filters.and(Filters.owner(self.getOwner()), Culture.MORIA, Race.GOBLIN);

        if (TriggerConditions.played(game, effectResult, self)
                && PlayConditions.canPlayFromDiscard(playerId, game, Filters.weapon, ExtraFilters.attachableTo(game, additionalAttachmentFilter))) {
            OptionalTriggerAction action = new OptionalTriggerAction(self);
            action.appendEffect(
                    new ChooseArbitraryCardsEffect(playerId, "Choose card to play", game.getGameState().getDiscard(playerId),
                            Filters.and(
                                    Filters.weapon,
                                    ExtraFilters.attachableTo(game, additionalAttachmentFilter)), 1, 1, true) {
                        @Override
                        protected void cardsSelected(LotroGame game, Collection<PhysicalCard> selectedCards) {
                            if (selectedCards.size() > 0) {
                                PhysicalCard selectedCard = selectedCards.iterator().next();
                                game.getActionsEnvironment().addActionToStack(PlayUtils.getPlayCardAction(game, selectedCard, 0, additionalAttachmentFilter, false));
                            }
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }}
