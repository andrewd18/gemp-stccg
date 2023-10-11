package com.gempukku.stccg.effects.defaulteffect.unrespondable;

import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.effects.defaulteffect.UnrespondableEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.Modifier;

public class AddUntilStartOfPhaseModifierEffect extends UnrespondableEffect {
    private final Modifier _modifier;
    private final Phase _phase;
    private final DefaultGame _game;

    public AddUntilStartOfPhaseModifierEffect(DefaultGame game, Modifier modifier, Phase phase) {
        _modifier = modifier;
        _phase = phase;
        _game = game;
    }

    @Override
    public void doPlayEffect() {
        _game.getModifiersEnvironment().addUntilStartOfPhaseModifier(_modifier, _phase);
    }
}
