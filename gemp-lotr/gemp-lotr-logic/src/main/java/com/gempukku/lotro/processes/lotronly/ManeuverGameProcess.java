package com.gempukku.lotro.processes.lotronly;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.processes.EndOfPhaseGameProcess;
import com.gempukku.lotro.processes.PlayersPlayPhaseActionsInOrderGameProcess;
import com.gempukku.lotro.processes.StartOfPhaseGameProcess;
import com.gempukku.lotro.processes.GameProcess;

public class ManeuverGameProcess implements GameProcess {
    private GameProcess _followingGameProcess;

    @Override
    public void process(DefaultGame game) {
        if (Filters.countActive(game, CardType.MINION)==0
                || game.getModifiersQuerying().shouldSkipPhase(game, Phase.MANEUVER, null))
            _followingGameProcess = new ArcheryGameProcess();
        else
            _followingGameProcess = new StartOfPhaseGameProcess(Phase.MANEUVER,
                    new PlayersPlayPhaseActionsInOrderGameProcess(game.getGameState().getPlayerOrder().getCounterClockwisePlayOrder(game.getGameState().getCurrentPlayerId(), true), 0,
                            new EndOfPhaseGameProcess(Phase.MANEUVER,
                                    new ArcheryGameProcess())));
    }

    @Override
    public GameProcess getNextProcess() {
        return _followingGameProcess;
    }
}
