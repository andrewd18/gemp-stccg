package com.gempukku.stccg.effects;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;

public class StackTopCardsFromDeckEffect extends AbstractEffect {
    private final String _playerId;
    private final int _count;
    private final PhysicalCard _target;

    public StackTopCardsFromDeckEffect(String playerId, int count, PhysicalCard target) {
        _playerId = playerId;
        _count = count;
        _target = target;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return _target.getZone().isInPlay() && game.getGameState().getDrawDeck(_playerId).size() >= _count;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        if (_target.getZone().isInPlay()) {
            int stacked = 0;
            for (int i = 0; i < _count; i++) {
                final PhysicalCard card = game.getGameState().removeCardFromEndOfPile(_playerId, Zone.DRAW_DECK, EndOfPile.TOP);
                if (card != null) {
                    game.getGameState().stackCard(game, card, _target);
                    stacked++;
                }
            }
            return new FullEffectResult(stacked == _count);
        }
        return new FullEffectResult(false);
    }
}
