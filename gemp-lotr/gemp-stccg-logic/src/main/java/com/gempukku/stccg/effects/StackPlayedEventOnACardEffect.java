package com.gempukku.stccg.effects;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;
import com.gempukku.stccg.actions.PlayEventAction;

import java.util.Collections;

public class StackPlayedEventOnACardEffect extends AbstractEffect {
    private final PlayEventAction _action;
    private final PhysicalCard _stackOn;

    public StackPlayedEventOnACardEffect(PlayEventAction action, PhysicalCard stackOn) {
        _action = action;
        _stackOn = stackOn;
    }

    @Override
    public String getText(DefaultGame game) {
        return "Stack " + GameUtils.getFullName(_action.getEventPlayed()) + " on "+GameUtils.getFullName(_stackOn);
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        Zone zone = _action.getEventPlayed().getZone();
        return _stackOn.getZone().isInPlay() && (zone == Zone.VOID || zone == Zone.VOID_FROM_HAND);
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        if (isPlayableInFull(game)) {
            PhysicalCard eventPlayed = _action.getEventPlayed();
            game.getGameState().sendMessage(_action.getPerformingPlayer() + " stacks " + GameUtils.getCardLink(eventPlayed) + " on " + GameUtils.getCardLink(_stackOn));
            game.getGameState().removeCardsFromZone(eventPlayed.getOwner(), Collections.singletonList(eventPlayed));
            game.getGameState().stackCard(game, eventPlayed, _stackOn);
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}