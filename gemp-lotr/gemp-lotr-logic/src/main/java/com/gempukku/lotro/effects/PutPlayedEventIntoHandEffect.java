package com.gempukku.lotro.effects;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.rules.GameUtils;

import java.util.Collections;

public class PutPlayedEventIntoHandEffect extends AbstractEffect {
    private final PhysicalCard card;

    public PutPlayedEventIntoHandEffect(PhysicalCard card) {
        this.card = card;
    }

    @Override
    public String getText(DefaultGame game) {
        return "Put " + GameUtils.getFullName(card) + " into hand";
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        Zone zone = card.getZone();
        return zone == Zone.VOID || zone == Zone.VOID_FROM_HAND;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        if (isPlayableInFull(game)) {
            game.getGameState().sendMessage(card.getOwner() + " puts " + GameUtils.getCardLink(card) + " into hand");
            game.getGameState().removeCardsFromZone(card.getOwner(), Collections.singletonList(card));
            game.getGameState().addCardToZone(game, card, Zone.HAND);
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}