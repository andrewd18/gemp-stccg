package com.gempukku.stccg.effects.defaulteffect.unrespondable;

import com.gempukku.stccg.actions.ActionProxy;
import com.gempukku.stccg.effects.defaulteffect.UnrespondableEffect;
import com.gempukku.stccg.game.DefaultGame;

public class AddUntilEndOfTurnActionProxyEffect extends UnrespondableEffect {
    private final ActionProxy _actionProxy;
    private final DefaultGame _game;

    public AddUntilEndOfTurnActionProxyEffect(DefaultGame game, ActionProxy actionProxy) {
        _actionProxy = actionProxy;
        _game = game;
    }

    @Override
    public void doPlayEffect() {
        _game.getActionsEnvironment().addUntilEndOfTurnActionProxy(_actionProxy);
    }
}