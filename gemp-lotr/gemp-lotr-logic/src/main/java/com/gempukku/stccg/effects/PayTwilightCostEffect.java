package com.gempukku.stccg.effects;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class PayTwilightCostEffect extends AbstractEffect {
    private final PhysicalCard _physicalCard;
    private final int _twilightModifier;
    private final boolean _ignoreRoamingPenalty;

    public PayTwilightCostEffect(PhysicalCard physicalCard) {
        this(physicalCard, 0);
    }

    public PayTwilightCostEffect(PhysicalCard physicalCard, int twilightModifier) {
        this(physicalCard, twilightModifier, false);
    }

    public PayTwilightCostEffect(PhysicalCard physicalCard, int twilightModifier, boolean ignoreRoamingPenalty) {
        _physicalCard = physicalCard;
        _twilightModifier = twilightModifier;
        _ignoreRoamingPenalty = ignoreRoamingPenalty;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        int twilightCost = game.getModifiersQuerying().getTwilightCost(game, _physicalCard, null, _twilightModifier, _ignoreRoamingPenalty);

        String currentPlayerId = game.getGameState().getCurrentPlayerId();
        if (!currentPlayerId.equals(_physicalCard.getOwner())) {
            int twilightPool = game.getGameState().getTwilightPool();
            return twilightPool >= twilightCost;
        }
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        int twilightCost = game.getModifiersQuerying().getTwilightCost(game, _physicalCard, null, _twilightModifier, _ignoreRoamingPenalty);

        String currentPlayerId = game.getGameState().getCurrentPlayerId();
        if (currentPlayerId.equals(_physicalCard.getOwner())) {
            game.getGameState().addTwilight(twilightCost);
            if (twilightCost > 0)
                game.getGameState().sendMessage(_physicalCard.getOwner() + " adds " + twilightCost + " to twilight pool");
            return new FullEffectResult(true);
        } else {
            boolean success = game.getGameState().getTwilightPool() >= twilightCost;
            twilightCost = Math.min(twilightCost, game.getGameState().getTwilightPool());
            if (twilightCost > 0) {
                game.getGameState().removeTwilight(twilightCost);
                game.getGameState().sendMessage(_physicalCard.getOwner() + " removes " + twilightCost + " from twilight pool");
            }
            return new FullEffectResult(success);
        }
    }
}
