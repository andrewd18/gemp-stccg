package com.gempukku.lotro.rules;

import com.gempukku.lotro.actions.*;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.ST1EGame;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ST1EPlayCardInPhaseRule {
    private final DefaultActionsEnvironment actionsEnvironment;

    public ST1EPlayCardInPhaseRule(DefaultActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy<ST1EGame>() {
                    @Override
                    public List<Action> getPhaseActions(String playerId, ST1EGame game) {
                        final Phase phase = game.getGameState().getCurrentPhase();
                        if (phase == Phase.SEED_DOORWAY) {
                            List<Action> result = new LinkedList<>();
                            for (PhysicalCard card : Filters.filter(game.getGameState().getHand(playerId), game)) {
                                if (game.checkPlayRequirements(card)) {
                                    result.add(new PlayPermanentForFreeAction(card, Zone.TABLE));
                                }
                            }
                            return result;
                        } else if (phase == Phase.SEED_MISSION && game.getGameState().getHand(playerId).size() > 0) {
                            if (Objects.equals(playerId, game.getGameState().getCurrentPlayerId())) {
                                List<Action> actionList = new LinkedList<>();
                                actionList.add(new PlayMissionAction(game.getGameState().getHand(playerId).get(0)));
                                return actionList;
                            }
//                        else if (phase == Phase.SEED_DOORWAY && game.getGameState().get)
                        }
                        return null;
                    }
                }
        );
    }
}