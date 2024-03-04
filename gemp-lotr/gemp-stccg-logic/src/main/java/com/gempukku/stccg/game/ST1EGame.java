package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardDeck;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.gamestate.GameStateListener;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.gamestate.UserFeedback;
import com.gempukku.stccg.processes.st1e.ST1EGameProcess;
import com.gempukku.stccg.processes.st1e.ST1EPlayerOrderProcess;
import com.gempukku.stccg.processes.TurnProcedure;
import com.gempukku.stccg.rules.AffiliationAttackRestrictions;
import com.gempukku.stccg.rules.ST1ERuleSet;

import java.util.Map;

public class ST1EGame extends DefaultGame {
    private ST1EGameState _gameState;
    private TurnProcedure _turnProcedure;
    private final ST1EGame _thisGame;
    private AffiliationAttackRestrictions _affiliationAttackRestrictions;

    public ST1EGame(GameFormat format, Map<String, CardDeck> decks, UserFeedback userFeedback,
                    final CardBlueprintLibrary library) {
        super(format, decks, userFeedback, library);
        _thisGame = this;

        _gameState = new ST1EGameState(_allPlayerIds, decks, library, _format, this);
        new ST1ERuleSet(_actionsEnvironment, _modifiersLogic).applyRuleSet();

        _gameState.createPhysicalCards();
        _turnProcedure = new TurnProcedure(this, userFeedback, _actionsEnvironment
        ) {
            @Override
            protected ST1EGameProcess setFirstGameProcess() {
                return new ST1EPlayerOrderProcess(_allPlayerIds, _gameState::init, _thisGame);
            }
        };
    }

    @Override
    public ST1EGameState getGameState() {
        return _gameState;
    }

    public TurnProcedure getTurnProcedure() { return _turnProcedure; }

    protected void restoreSnapshot() {
        if (_snapshotToRestore != null) {
            if (!(_snapshotToRestore.getGameState() instanceof ST1EGameState))
                throw new RuntimeException("Tried to restore a snapshot with an invalid gamestate");
            else {
                _gameState = (ST1EGameState) _snapshotToRestore.getGameState();
                _modifiersLogic = _snapshotToRestore.getModifiersLogic();
                _actionsEnvironment = _snapshotToRestore.getActionsEnvironment();
                _turnProcedure = _snapshotToRestore.getTurnProcedure();
                getGameState().sendMessage("Reverted to previous game state");
                _snapshotToRestore = null;
                getGameState().sendStateToAllListeners();
            }
        }
    }

    @Override
    public void addGameStateListener(String playerId, GameStateListener gameStateListener) {
        getGameState().addGameStateListener(playerId, gameStateListener);
    }

    public void setAffiliationAttackRestrictions(AffiliationAttackRestrictions restrictions) {
        _affiliationAttackRestrictions = restrictions;
    }

    public AffiliationAttackRestrictions getAffiliationAttackRestrictions() { return _affiliationAttackRestrictions; }

}
