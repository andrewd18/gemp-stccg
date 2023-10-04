package com.gempukku.lotro.results;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.effects.KillEffect;
import com.gempukku.lotro.effects.EffectResult;

import java.util.Set;

public class KilledResult extends EffectResult {
    private final Set<PhysicalCard> _killedCards;
    private final KillEffect.Cause _cause;

    public KilledResult(Set<PhysicalCard> killedCards, KillEffect.Cause cause) {
        super(EffectResult.Type.ANY_NUMBER_KILLED);
        _killedCards = killedCards;
        _cause = cause;
    }

    public Set<PhysicalCard> getKilledCards() {
        return _killedCards;
    }

    public KillEffect.Cause getCause() {
        return _cause;
    }
}
