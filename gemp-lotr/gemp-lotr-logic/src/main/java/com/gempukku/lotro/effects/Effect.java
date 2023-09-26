package com.gempukku.lotro.effects;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.game.DefaultGame;

public interface Effect {
    enum Type {
        BEFORE_EXERT, BEFORE_DISCARD_FROM_PLAY,
        BEFORE_ADD_TWILIGHT, BEFORE_KILLED, BEFORE_HEALED,
        BEFORE_SKIRMISH_RESOLVED,
        BEFORE_THREAT_WOUNDS,
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
    String getText(DefaultGame game);

    /**
     * Returns the type of the effect. This should list the type of effect it represents
     * if the effect is a recognizable by the game.
     *
     * @return
     */
    Effect.Type getType();

    /**
     * Checks wheather this effect can be played in full. This is required to check
     * for example for cards that give a choice of effects to carry out and one
     * that can be played in full has to be chosen.
     *
     * @param game
     * @return
     */
    boolean isPlayableInFull(DefaultGame game);

    /**
     * Plays the effect and emits the results.
     *
     * @param game
     * @return
     */
    void playEffect(DefaultGame game);

    /**
     * Returns if the effect was carried out (not prevented) in full. This is required
     * for checking if effect that player can prevent by paying some cost should be
     * played anyway. If it was prevented, the original event has to be played.
     *
     * @return
     */
    boolean wasCarriedOut();

    default LotroPhysicalCard getSource() {
        return null;
    }

    default String getPerformingPlayer() {
        return null;
    }
}
