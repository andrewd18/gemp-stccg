package com.gempukku.lotro.modifiers;

import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.actions.Action;
import com.gempukku.lotro.modifiers.condition.Condition;

public class PlayersCantUseCardSpecialAbilitiesModifier extends AbstractModifier {

    private final Filter _sourceFilters;

    public PlayersCantUseCardSpecialAbilitiesModifier(LotroPhysicalCard source, Filterable... sourceFilters) {
        this(source, null, sourceFilters);
    }

    public PlayersCantUseCardSpecialAbilitiesModifier(LotroPhysicalCard source, Condition condition, Filterable... sourceFilters) {
        super(source, null, null, condition, ModifierEffect.ACTION_MODIFIER);
        _sourceFilters = Filters.and(sourceFilters);
    }

    @Override
    public boolean canPlayAction(DefaultGame game, String performingPlayer, Action action) {
        return action.getType() != Action.Type.SPECIAL_ABILITY
                || !_sourceFilters.accepts(game, action.getActionSource());
    }
}
