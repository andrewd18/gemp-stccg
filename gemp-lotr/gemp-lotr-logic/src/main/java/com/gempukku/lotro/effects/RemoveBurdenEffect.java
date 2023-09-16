package com.gempukku.lotro.effects;

import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.rules.GameUtils;
import com.gempukku.lotro.effects.results.RemoveBurdenResult;

public class RemoveBurdenEffect extends AbstractEffect {
    private final String _performingPlayerId;
    private final LotroPhysicalCard _source;
    private final int _count;

    public RemoveBurdenEffect(String performingPlayerId, LotroPhysicalCard source) {
        this(performingPlayerId, source, 1);
    }

    public RemoveBurdenEffect(String performingPlayerId, LotroPhysicalCard source, int count) {
        _performingPlayerId = performingPlayerId;
        _source = source;
        _count = count;
    }

    public LotroPhysicalCard getSource() {
        return _source;
    }

    @Override
    public String getText(DefaultGame game) {
        return "Remove " + _count + " burden" + ((_count > 1) ? "s" : "");
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return game.getModifiersQuerying().canRemoveBurden(game, _source)
                && game.getGameState().getBurdens() >= _count;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        if (game.getModifiersQuerying().canRemoveBurden(game, _source)) {
            int toRemove = Math.min(_count, game.getGameState().getBurdens());
            if (toRemove > 0) {
                game.getGameState().sendMessage(_performingPlayerId + " removed " + GameUtils.formatNumber(toRemove, _count) + " burden" + ((toRemove > 1) ? "s" : "") + " with " + GameUtils.getCardLink(_source));
                game.getGameState().removeBurdens(toRemove);
                for (int i = 0; i < toRemove; i++)
                    game.getActionsEnvironment().emitEffectResult(new RemoveBurdenResult(_performingPlayerId, _source));
                return new FullEffectResult(true);
            }
        }

        return new FullEffectResult(false);
    }
}
