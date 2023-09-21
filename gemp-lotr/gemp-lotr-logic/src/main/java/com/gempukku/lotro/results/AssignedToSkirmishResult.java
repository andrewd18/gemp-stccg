package com.gempukku.lotro.results;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.effects.EffectResult;

public class AssignedToSkirmishResult extends EffectResult {
    private final LotroPhysicalCard _assigned;
    private final String _playerId;

    public AssignedToSkirmishResult(LotroPhysicalCard assigned, String playerId) {
        super(Type.ASSIGNED_TO_SKIRMISH);
        _assigned = assigned;
        _playerId = playerId;
    }

    public LotroPhysicalCard getAssignedCard() {
        return _assigned;
    }

    public String getPlayerId() {
        return _playerId;
    }
}
