package com.gempukku.lotro.effects;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.rules.GameUtils;
import com.gempukku.lotro.results.DrawCardOrPutIntoHandResult;

import java.util.Collections;

public class PutCardFromDeckIntoHandOrDiscardEffect extends AbstractEffect {
    private final LotroPhysicalCard _physicalCard;
    private final boolean _reveal;

    public PutCardFromDeckIntoHandOrDiscardEffect(LotroPhysicalCard physicalCard, boolean reveal) {
        _physicalCard = physicalCard;
        _reveal = reveal;
    }

    public LotroPhysicalCard getCard() {
        return _physicalCard;
    }

    @Override
    public String getText(DefaultGame game) {
        return "Put card from deck into hand";
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return _physicalCard.getZone() == Zone.DECK;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        if (_physicalCard.getZone() == Zone.DECK) {
            var gameState = game.getGameState();
            if ((game.getFormat().doesNotHaveRuleOfFour() || game.getModifiersQuerying().canDrawCardAndIncrementForRuleOfFour(game, _physicalCard.getOwner()))) {
                if(_reveal) {
                    gameState.sendMessage(_physicalCard.getOwner() + " puts " + GameUtils.getCardLink(_physicalCard) + " from deck into their hand");
                }
                else {
                    gameState.sendMessage(_physicalCard.getOwner() + " puts a card from deck into their hand");
                }
                gameState.removeCardsFromZone(_physicalCard.getOwner(), Collections.singleton(_physicalCard));
                gameState.addCardToZone(game, _physicalCard, Zone.HAND);
                game.getActionsEnvironment().emitEffectResult(new DrawCardOrPutIntoHandResult(_physicalCard.getOwner()));
                return new FullEffectResult(true);
            } else {
                gameState.sendMessage(_physicalCard.getOwner() + " discards " + GameUtils.getCardLink(_physicalCard) + " from deck due to Rule of 4");
                gameState.removeCardsFromZone(_physicalCard.getOwner(), Collections.singleton(_physicalCard));
                gameState.addCardToZone(game, _physicalCard, Zone.DISCARD);
            }
        }
        return new FullEffectResult(false);
    }
}
