package com.gempukku.lotro.modifiers.lotronly;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.modifiers.AbstractModifier;
import com.gempukku.lotro.modifiers.ModifierEffect;
import com.gempukku.lotro.condition.Condition;

public class CantHealModifier extends AbstractModifier {
    public CantHealModifier(PhysicalCard source, Filterable... affectFilters) {
        this(source, null, affectFilters);
    }

    public CantHealModifier(PhysicalCard source, Condition condition, Filterable... affectFilters) {
        super(source, "Can't be healed", Filters.and(affectFilters), condition, ModifierEffect.WOUND_MODIFIER);
    }

    @Override
    public boolean canBeHealed(DefaultGame game, PhysicalCard card) {
        return false;
    }
}
