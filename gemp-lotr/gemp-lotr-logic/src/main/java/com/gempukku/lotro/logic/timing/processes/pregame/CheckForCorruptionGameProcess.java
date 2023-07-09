package com.gempukku.lotro.logic.timing.processes.pregame;

import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.PlayOrder;
import com.gempukku.lotro.logic.timing.processes.GameProcess;

public class CheckForCorruptionGameProcess implements GameProcess {
    private GameProcess _nextProcess;
    private final String _firstPlayer;

    public CheckForCorruptionGameProcess(String firstPlayer) {
        _firstPlayer = firstPlayer;
    }

    @Override
    public void process(LotroGame game) {
        PlayOrder playOrder = game.getGameState().getPlayerOrder().getClockwisePlayOrder(_firstPlayer, false);

        while (true) {
            String nextPlayer = playOrder.getNextPlayer();
            if (nextPlayer == null)
                break;

            game.getGameState().startPlayerTurn(nextPlayer);
            game.getGameState().startAffectingCardsForCurrentPlayer(game);
            game.getGameState().stopAffectingCardsForCurrentPlayer();
        }

        _nextProcess = new PlayStartingFellowshipGameProcess(game.getGameState().getPlayerOrder().getClockwisePlayOrder(_firstPlayer, false), _firstPlayer);
    }

    @Override
    public GameProcess getNextProcess() {
        return _nextProcess;
    }
}
