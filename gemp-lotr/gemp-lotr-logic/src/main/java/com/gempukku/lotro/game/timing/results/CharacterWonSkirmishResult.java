package com.gempukku.lotro.game.timing.results;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.effects.EffectResult;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CharacterWonSkirmishResult extends EffectResult {
    private final PhysicalCard _winner;
    private final Set<PhysicalCard> _involving;
    private final SkirmishType _type;

    public enum SkirmishType {
        OVERWHELM, NORMAL
    }

    public CharacterWonSkirmishResult(SkirmishType type, PhysicalCard winner, PhysicalCard involving) {
        super(EffectResult.Type.CHARACTER_WON_SKIRMISH);
        _type = type;
        _winner = winner;
        if (involving == null)
            _involving = Collections.emptySet();
        else
            _involving = Collections.singleton(involving);
    }

    public CharacterWonSkirmishResult(SkirmishType type, PhysicalCard winner, Set<PhysicalCard> involving) {
        super(EffectResult.Type.CHARACTER_WON_SKIRMISH);
        _type = type;
        _winner = winner;
        _involving = new HashSet<>(involving);
    }

    public Set<PhysicalCard> getInvolving() {
        return _involving;
    }

    public SkirmishType getSkirmishType() {
        return _type;
    }

    public PhysicalCard getWinner() {
        return _winner;
    }
}
