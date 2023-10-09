package com.gempukku.stccg.effects;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;

import java.util.Collections;

public class StackCardFromHandEffect extends AbstractEffect {
    private final PhysicalCard _card;
    private final PhysicalCard _stackOn;

    public StackCardFromHandEffect(PhysicalCard card, PhysicalCard stackOn) {
        _card = card;
        _stackOn = stackOn;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        if (isPlayableInFull(game)) {
            game.getGameState().sendMessage(_card.getOwner() + " stacks " + GameUtils.getCardLink(_card) + " from hand on " + GameUtils.getCardLink(_stackOn));
            game.getGameState().removeCardsFromZone(_card.getOwner(), Collections.singleton(_card));
            game.getGameState().stackCard(game, _card, _stackOn);
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }

    @Override
    public String getText(DefaultGame game) {
        return "Stack " + GameUtils.getFullName(_card) + " from hand on " + GameUtils.getFullName(_stackOn);
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return _card.getZone() == Zone.HAND;
    }
}
