package com.gempukku.stccg.results;

import com.gempukku.stccg.common.filterable.Phase;

public class StartOfPhaseResult extends EffectResult {
    private final Phase _phase;
    private String _playerId;

    public StartOfPhaseResult(Phase phase) {
        super(EffectResult.Type.START_OF_PHASE);
        _phase = phase;
    }

    public StartOfPhaseResult(Phase phase, String playerId) {
        super(EffectResult.Type.START_OF_PHASE);
        _phase = phase;
        _playerId = playerId;
    }

    public String getPlayerId() {
        return _playerId;
    }

    public Phase getPhase() {
        return _phase;
    }
}
