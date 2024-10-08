package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.actions.playcard.TribblesPlayCardAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.TribblesGame;

public class TribblesPhysicalCard extends PhysicalCard {
    private final TribblesGame _game;
    public TribblesPhysicalCard(TribblesGame game, int cardId, Player owner, CardBlueprint blueprint) {
        super(cardId, owner, blueprint);
        _game = game;
    }
    @Override
    public TribblesGame getGame() { return _game; }

    @Override
    public CostToEffectAction getPlayCardAction(boolean forFree) { return new TribblesPlayCardAction(this); }

    @Override
    public ActionContext createActionContext(String playerId, Effect effect, EffectResult effectResult) {
        return new TribblesActionContext(playerId, getGame(), this, effect, effectResult);
    }

    public boolean canPlayOutOfSequence() {
        if (_blueprint.getPlayOutOfSequenceConditions() == null) return false;
        return _blueprint.getPlayOutOfSequenceConditions().stream().anyMatch(
                requirement -> requirement.accepts(createActionContext()));
    }

    public boolean isNextInSequence() {
        final int cardValue = _blueprint.getTribbleValue();
        if (_game.getGameState().isChainBroken() && (cardValue == 1)) {
            return true;
        }
        return (cardValue == _game.getGameState().getNextTribbleInSequence());
    }

}