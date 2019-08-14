package com.gempukku.lotro.cards.set9.elven;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.effects.*;
import com.gempukku.lotro.logic.timing.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Reflections
 * Side: Free
 * Culture: Elven
 * Twilight Cost: 1
 * Type: Possession • Support Area
 * Game Text: To play, spot a Dwarf. When you play this possession, add 2 [ELVEN] tokens here. Fellowship: Discard
 * this possession or remove an [ELVEN] token from here to reveal the top card of your draw deck. If it is a [DWARVEN]
 * or [ELVEN] card, you may take it into hand.
 */
public class Card9_022 extends AbstractPermanent {
    public Card9_022() {
        super(Side.FREE_PEOPLE, 1, CardType.POSSESSION, Culture.ELVEN, "Strands of Elven Hair", null, true);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Race.DWARF);
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.played(game, effectResult, self)) {
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            action.appendEffect(
                    new AddTokenEffect(self, self, Token.ELVEN, 2));
            return Collections.singletonList(action);
        }
        return null;
    }

    @Override
    public List<? extends Action> getPhaseActionsInPlay(final String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.FELLOWSHIP, self)
                && (PlayConditions.canSelfDiscard(self, game) || PlayConditions.canRemoveTokens(game, self, Token.ELVEN))) {
            final ActivateCardAction action = new ActivateCardAction(self);
            List<Effect> possibleCosts = new LinkedList<Effect>();
            possibleCosts.add(
                    new RemoveTokenEffect(self, self, Token.ELVEN) {
                        @Override
                        public String getText(LotroGame game) {
                            return "Remove ELVEN token from here";
                        }
                    });
            possibleCosts.add(
                    new SelfDiscardEffect(self) {
                        @Override
                        public String getText(LotroGame game) {
                            return "Discard this possession";
                        }
                    });
            action.appendCost(
                    new ChoiceEffect(action, playerId, possibleCosts));
            action.appendEffect(
                    new RevealTopCardsOfDrawDeckEffect(self, playerId, 1) {
                        @Override
                        protected void cardsRevealed(List<PhysicalCard> revealedCards) {
                            for (PhysicalCard card : revealedCards) {
                                Culture culture = card.getBlueprint().getCulture();
                                if (culture == Culture.DWARVEN || culture == Culture.ELVEN)
                                    action.appendEffect(
                                            new OptionalEffect(action, playerId,
                                                    new PutCardFromDeckIntoHandEffect(card)));
                            }
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }
}
