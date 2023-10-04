package com.gempukku.lotro.modifiers.lotronly;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.modifiers.AbstractModifier;
import com.gempukku.lotro.condition.Condition;
import com.gempukku.lotro.modifiers.ModifierEffect;
import com.gempukku.lotro.condition.AndCondition;
import com.gempukku.lotro.condition.PhaseCondition;

import java.util.Collection;

public class CantTakeMoreThanXWoundsModifier extends AbstractModifier {
    private final int _count;

    public CantTakeMoreThanXWoundsModifier(PhysicalCard source, Phase phase, int count, Filterable... affectFilters) {
        this(source, phase, count, null, affectFilters);
    }

    public CantTakeMoreThanXWoundsModifier(PhysicalCard source, final Phase phase, int count, Condition condition, Filterable... affectFilters) {
        super(source, "Can't take more than " + count + " wound(s)", Filters.and(affectFilters),
                (condition == null ? new PhaseCondition(phase) : new AndCondition(new PhaseCondition(phase), condition)), ModifierEffect.WOUND_MODIFIER);
        _count = count;
    }

    @Override
    public boolean canTakeWounds(DefaultGame game, Collection<PhysicalCard> woundSources, PhysicalCard physicalCard, int woundsAlreadyTaken, int woundsToTake) {
        return woundsAlreadyTaken + woundsToTake <= _count;
    }
}
