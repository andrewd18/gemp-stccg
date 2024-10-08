package com.gempukku.stccg.actions.turn;


import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.Modifier;

public class AddUntilEndOfTurnModifierEffect extends UnrespondableEffect {
    private final Modifier _modifier;
    private final DefaultGame _game;

    public AddUntilEndOfTurnModifierEffect(DefaultGame game, Modifier modifier) {
        _modifier = modifier;
        _game = game;
    }

    @Override
    public void doPlayEffect() {
        _game.getModifiersEnvironment().addUntilEndOfTurnModifier(_modifier);
    }
}
