package com.gempukku.lotro.game.effects;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;

public interface Effect {
    public enum Type {
        BEFORE_WOUND, BEFORE_EXERT, BEFORE_ADD_BURDENS, BEFORE_DISCARD_FROM_PLAY,
        BEFORE_ADD_TWILIGHT, BEFORE_KILLED, BEFORE_HEALED,
        BEFORE_TAKE_CONTROL_OF_A_SITE,
        BEFORE_SKIRMISH_RESOLVED,
        BEFORE_THREAT_WOUNDS, BEFORE_TOIL,
        BEFORE_DRAW_CARD,
        BEFORE_MOVE_FROM, BEFORE_MOVE, BEFORE_MOVE_TO
    }

    /**
     * Returns the text tha represents this effect. This text might be displayed
     * to the user.
     *
     * @param game
     * @return
     */
    public String getText(DefaultGame game);

    /**
     * Returns the type of the effect. This should list the type of effect it represents
     * if the effect is a recognizable by the game.
     *
     * @return
     */
    public Effect.Type getType();

    /**
     * Checks wheather this effect can be played in full. This is required to check
     * for example for cards that give a choice of effects to carry out and one
     * that can be played in full has to be chosen.
     *
     * @param game
     * @return
     */
    public boolean isPlayableInFull(DefaultGame game);

    /**
     * Plays the effect and emits the results.
     *
     * @param game
     * @return
     */
    public void playEffect(DefaultGame game);

    /**
     * Returns if the effect was carried out (not prevented) in full. This is required
     * for checking if effect that player can prevent by paying some cost should be
     * played anyway. If it was prevented, the original event has to be played.
     *
     * @return
     */
    public boolean wasCarriedOut();

    default PhysicalCard getSource() {
        return null;
    }

    default String getPerformingPlayer() {
        return null;
    }
}
