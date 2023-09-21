package com.gempukku.lotro.effects;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.rules.GameUtils;

import java.util.Collections;

public class StackCardFromDiscardEffect extends AbstractEffect {
    private final LotroPhysicalCard _card;
    private final LotroPhysicalCard _stackOn;

    public StackCardFromDiscardEffect(LotroPhysicalCard card, LotroPhysicalCard stackOn) {
        _card = card;
        _stackOn = stackOn;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return _card.getZone() == Zone.DISCARD && _stackOn.getZone().isInPlay();
    }

    @Override
    public String getText(DefaultGame game) {
        return "Stack " + GameUtils.getFullName(_card) + " from discard on " + GameUtils.getFullName(_stackOn);
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        if (isPlayableInFull(game)) {
            game.getGameState().sendMessage(_card.getOwner() + " stacks " + GameUtils.getCardLink(_card) + " from discard on " + GameUtils.getCardLink(_stackOn));
            game.getGameState().removeCardsFromZone(_card.getOwner(), Collections.singleton(_card));
            game.getGameState().stackCard(game, _card, _stackOn);
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
