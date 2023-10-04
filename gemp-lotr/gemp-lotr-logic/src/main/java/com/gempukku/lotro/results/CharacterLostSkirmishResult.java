package com.gempukku.lotro.results;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.effects.EffectResult;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CharacterLostSkirmishResult extends EffectResult {
    private final PhysicalCard _loser;
    private final Set<PhysicalCard> _involving;
    private final SkirmishType _type;

    public enum SkirmishType {
        OVERWHELM, NORMAL
    }

    public CharacterLostSkirmishResult(SkirmishType type, PhysicalCard loser, PhysicalCard involving) {
        super(Type.CHARACTER_LOST_SKIRMISH);
        _type = type;
        _loser = loser;
        if (involving == null)
            _involving = Collections.emptySet();
        else
            _involving = Collections.singleton(involving);
    }

    public CharacterLostSkirmishResult(SkirmishType type, PhysicalCard loser, Set<PhysicalCard> involving) {
        super(Type.CHARACTER_LOST_SKIRMISH);
        _type = type;
        _loser = loser;
        _involving = new HashSet<>(involving);
    }

    public Set<PhysicalCard> getInvolving() {
        return _involving;
    }

    public SkirmishType getSkirmishType() {
        return _type;
    }

    public PhysicalCard getLoser() {
        return _loser;
    }
}