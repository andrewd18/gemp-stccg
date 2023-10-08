package com.gempukku.stccg.effects;

import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.Modifier;

public class AddUntilStartOfPhaseModifierEffect extends UnrespondableEffect {
    private final Modifier _modifier;
    private final Phase _phase;

    public AddUntilStartOfPhaseModifierEffect(Modifier modifier, Phase phase) {
        _modifier = modifier;
        _phase = phase;
    }

    @Override
    public void doPlayEffect(DefaultGame game) {
        game.getModifiersEnvironment().addUntilStartOfPhaseModifier(_modifier, _phase);
    }
}
