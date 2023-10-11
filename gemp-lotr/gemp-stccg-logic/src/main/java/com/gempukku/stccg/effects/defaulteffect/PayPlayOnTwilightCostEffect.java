package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.game.DefaultGame;

public class PayPlayOnTwilightCostEffect extends DefaultEffect {
    private final PhysicalCard _physicalCard;
    private final PhysicalCard _target;
    private final int _twilightModifier;
    private final DefaultGame _game;

    public PayPlayOnTwilightCostEffect(DefaultGame game, PhysicalCard physicalCard, PhysicalCard target, int twilightModifier) {
        _physicalCard = physicalCard;
        _target = target;
        _twilightModifier = twilightModifier;
        _game = game;
    }

    @Override
    public boolean isPlayableInFull() {
        int twilightCost = _game.getModifiersQuerying().getTwilightCost(_game, _physicalCard, _target, _twilightModifier, false);

        String currentPlayerId = _game.getGameState().getCurrentPlayerId();
        if (!currentPlayerId.equals(_physicalCard.getOwner())) {
            int twilightPool = _game.getGameState().getTwilightPool();
            return twilightPool >= twilightCost;
        }
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        int twilightCost = _game.getModifiersQuerying().getTwilightCost(_game, _physicalCard, _target, _twilightModifier, false);

        String currentPlayerId = _game.getGameState().getCurrentPlayerId();
        if (currentPlayerId.equals(_physicalCard.getOwner())) {
            _game.getGameState().addTwilight(twilightCost);
            if (twilightCost > 0)
                _game.getGameState().sendMessage(_physicalCard.getOwner() + " adds " + twilightCost + " to twilight pool");
            return new FullEffectResult(true);
        } else {
            int twilightPool = _game.getGameState().getTwilightPool();
            boolean success = twilightPool >= twilightCost;
            twilightCost = Math.min(twilightPool, twilightCost);
            _game.getGameState().removeTwilight(twilightCost);
            if (twilightCost > 0)
                _game.getGameState().sendMessage(_physicalCard.getOwner() + " removes " + twilightCost + " from twilight pool");
            return new FullEffectResult(success);
        }
    }
}
