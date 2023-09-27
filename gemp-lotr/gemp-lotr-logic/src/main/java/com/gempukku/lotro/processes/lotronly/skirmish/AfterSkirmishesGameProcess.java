package com.gempukku.lotro.processes.lotronly.skirmish;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.gamestate.GameState;
import com.gempukku.lotro.processes.GameProcess;
import com.gempukku.lotro.processes.lotronly.AssignmentGameProcess;
import com.gempukku.lotro.processes.lotronly.RegroupGameProcess;

public class AfterSkirmishesGameProcess implements GameProcess {
    private GameProcess _followingGameProcess;

    @Override
    public void process(DefaultGame game) {
        GameState gameState = game.getGameState();
        if (gameState.isExtraSkirmishes()) {
            gameState.setExtraSkirmishes(false);
            _followingGameProcess = new RegroupGameProcess();
        } else if (!gameState.isFierceSkirmishes() && Filters.countActive(game, CardType.MINION, Keyword.FIERCE)>0) {
            gameState.sendMessage("Fierce skirmishes.");
            _followingGameProcess = new AssignmentGameProcess();
        } else {
            _followingGameProcess = new EndSkirmishesGameProcess();
        }
    }

    @Override
    public GameProcess getNextProcess() {
        return _followingGameProcess;
    }
}
