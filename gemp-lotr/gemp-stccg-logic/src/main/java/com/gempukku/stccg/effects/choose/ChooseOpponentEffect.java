package com.gempukku.stccg.effects.choose;

import com.gempukku.stccg.adventure.InvalidSoloAdventureException;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;

import java.util.LinkedList;
import java.util.List;

public abstract class ChooseOpponentEffect extends ChoosePlayerEffect {

    public ChooseOpponentEffect(ActionContext actionContext) {
        super(actionContext);
    }

    public static String[] getOpponents(DefaultGame game, String playerId) {
        if (game.isSolo())
            throw new InvalidSoloAdventureException("Opponent requested");
        List<String> shadowPlayers = new LinkedList<>(game.getGameState().getPlayerOrder().getAllPlayers());
        shadowPlayers.remove(playerId);
        return shadowPlayers.toArray(new String[0]);
    }

    @Override
    public void doPlayEffect() {
        String[] opponents = getOpponents(_game, _playerId);
        if (opponents.length == 1)
            playerChosen(opponents[0]);
        else
            _game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new MultipleChoiceAwaitingDecision(1, "Choose an opponent", opponents) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            playerChosen(result);
                        }
                    });
    }
}
