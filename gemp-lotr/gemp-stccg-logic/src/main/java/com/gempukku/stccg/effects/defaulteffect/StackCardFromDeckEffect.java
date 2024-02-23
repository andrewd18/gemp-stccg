package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collections;

public class StackCardFromDeckEffect extends DefaultEffect {
    private final PhysicalCard _card;
    private final PhysicalCard _stackOn;
    private final DefaultGame _game;

    public StackCardFromDeckEffect(DefaultGame game, PhysicalCard card, PhysicalCard stackOn) {
        _card = card;
        _stackOn = stackOn;
        _game = game;
    }

    @Override
    public boolean isPlayableInFull() {
        return _card.getZone() == Zone.DRAW_DECK && _stackOn.getZone().isInPlay();
    }

    @Override
    public String getText() {
        return "Stack " + _card.getFullName() + " from deck on " + _stackOn.getFullName();
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        if (isPlayableInFull()) {
            _game.getGameState().sendMessage(_card.getOwnerName() + " stacks " + _card.getCardLink() + " from deck on " + _stackOn.getCardLink());
            _game.getGameState().removeCardsFromZone(_card.getOwnerName(), Collections.singleton(_card));
            _game.getGameState().stackCard(_card, _stackOn);
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
