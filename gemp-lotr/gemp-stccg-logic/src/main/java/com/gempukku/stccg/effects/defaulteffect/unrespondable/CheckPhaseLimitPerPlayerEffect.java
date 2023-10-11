package com.gempukku.stccg.effects.defaulteffect.unrespondable;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.defaulteffect.UnrespondableEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.Action;

public class CheckPhaseLimitPerPlayerEffect extends UnrespondableEffect {
    private final Action _action;
    private final PhysicalCard _card;
    private final String _limitPrefix;
    private final String _playerId;
    private final int _limit;
    private final Phase _phase;
    private final Effect _limitedEffect;
    private final DefaultGame _game;

    public CheckPhaseLimitPerPlayerEffect(DefaultGame game, Action action, PhysicalCard card, String limitPrefix,
                                          String playerId, int limit, Phase phase, Effect limitedEffect) {
        _card = card;
        _limitPrefix = limitPrefix;
        _playerId = playerId;
        _limit = limit;
        _phase = phase;
        _limitedEffect = limitedEffect;
        _action = action;
        _game = game;
    }

    @Override
    public void doPlayEffect() {
        Phase phase = _phase;
        if (phase == null)
            phase = _game.getGameState().getCurrentPhase();

        int incrementedBy = _game.getModifiersQuerying().getUntilEndOfPhaseLimitCounter(_card, _playerId + "-" + _limitPrefix, phase).incrementToLimit(_limit, 1);
        if (incrementedBy > 0) {
            SubAction subAction = new SubAction(_action);
            subAction.appendEffect(
                    _limitedEffect);
            _game.getActionsEnvironment().addActionToStack(subAction);
        }
    }
}
