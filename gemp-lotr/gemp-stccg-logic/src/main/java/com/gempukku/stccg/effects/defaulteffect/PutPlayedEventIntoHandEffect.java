package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collections;

public class PutPlayedEventIntoHandEffect extends DefaultEffect {
    private final PhysicalCard card;
    private final DefaultGame _game;

    public PutPlayedEventIntoHandEffect(ActionContext actionContext) {
        this.card = actionContext.getSource();
        _game = actionContext.getGame();
    }

    @Override
    public String getText() {
        return "Put " + card.getFullName() + " into hand";
    }

    @Override
    public boolean isPlayableInFull() {
        Zone zone = card.getZone();
        return zone == Zone.VOID || zone == Zone.VOID_FROM_HAND;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (isPlayableInFull()) {
            _game.getGameState().sendMessage(card.getOwnerName() + " puts " + card.getCardLink() + " into hand");
            _game.getGameState().removeCardsFromZone(card.getOwnerName(), Collections.singletonList(card));
            _game.getGameState().addCardToZone(card, Zone.HAND);
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}