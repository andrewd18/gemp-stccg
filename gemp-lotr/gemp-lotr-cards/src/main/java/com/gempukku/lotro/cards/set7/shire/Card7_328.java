package com.gempukku.lotro.cards.set7.shire;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractAttachable;
import com.gempukku.lotro.logic.effects.AssignmentEffect;
import com.gempukku.lotro.logic.effects.RemoveBurdenEffect;
import com.gempukku.lotro.logic.effects.SelfDiscardEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndDiscardCardsFromPlayEffect;
import com.gempukku.lotro.logic.modifiers.AddActionToCardModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Return of the King
 * Side: Free
 * Culture: Shire
 * Twilight Cost: 2
 * Type: Condition
 * Game Text: Bearer must be Merry or Pippin. Each minion gains this ability: 'Assignment: Assign this minion to bearer
 * of Slow-kindled Courage.' Regroup: Discard this condition to discard a minion and remove 2 burdens.
 */
public class Card7_328 extends AbstractAttachable {
    public Card7_328() {
        super(Side.FREE_PEOPLE, CardType.CONDITION, 2, Culture.SHIRE, null, "Slow-kindled Courage", null, true);
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Filters.or(Filters.name("Merry"), Filters.name("Pippin"));
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(final LotroGame game, final PhysicalCard self) {
        return Collections.singletonList(
                new AddActionToCardModifier(self, null, CardType.MINION) {
                    @Override
                    protected ActivateCardAction createExtraPhaseAction(LotroGame game, PhysicalCard matchingCard) {
                        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.ASSIGNMENT, matchingCard, 0)) {
                            ActivateCardAction action = new ActivateCardAction(matchingCard);
                            action.setText("Assign to " + GameUtils.getFullName(self.getAttachedTo()));
                            action.appendEffect(
                                    new AssignmentEffect(matchingCard.getOwner(), self.getAttachedTo(), matchingCard));
                            return action;
                        }
                        return null;
                    }
                });
    }

    @Override
    public List<? extends Action> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.REGROUP, self)
                && PlayConditions.canSelfDiscard(self, game)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new SelfDiscardEffect(self));
            action.appendEffect(
                    new ChooseAndDiscardCardsFromPlayEffect(action, playerId, 1, 1, CardType.MINION));
            action.appendEffect(
                    new RemoveBurdenEffect(playerId, self, 2));
            return Collections.singletonList(action);
        }
        return null;
    }
}
