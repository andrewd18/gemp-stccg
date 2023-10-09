package com.gempukku.stccg.effects;

import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;
import com.gempukku.stccg.actions.PlayEventAction;

import java.util.Collections;

public class RemovePlayedEventFromGameEffect extends AbstractEffect {
    private final PlayEventAction _action;

    public RemovePlayedEventFromGameEffect(PlayEventAction action) {
        _action = action;
    }

    @Override
    public String getText(DefaultGame game) {
        return "Remove " + GameUtils.getFullName(_action.getEventPlayed()) + " from the game";
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        Zone zone = _action.getEventPlayed().getZone();
        return zone == Zone.VOID || zone == Zone.VOID_FROM_HAND;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        if (isPlayableInFull(game)) {
            PhysicalCard eventPlayed = _action.getEventPlayed();
            game.getGameState().sendMessage(_action.getPerformingPlayer() + " removes " + GameUtils.getCardLink(eventPlayed) + " from the game");
            game.getGameState().removeCardsFromZone(eventPlayed.getOwner(), Collections.singletonList(eventPlayed));
            game.getGameState().addCardToZone(game, eventPlayed, Zone.REMOVED);
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}