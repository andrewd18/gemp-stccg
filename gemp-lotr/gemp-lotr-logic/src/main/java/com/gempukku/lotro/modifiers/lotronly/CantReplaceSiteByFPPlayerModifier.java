package com.gempukku.lotro.modifiers.lotronly;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.modifiers.AbstractModifier;
import com.gempukku.lotro.condition.Condition;
import com.gempukku.lotro.modifiers.ModifierEffect;

public class CantReplaceSiteByFPPlayerModifier extends AbstractModifier {
    public CantReplaceSiteByFPPlayerModifier(PhysicalCard source, Condition condition, Filterable affectFilter) {
        super(source, "Can't be replaced by Free Peoples player", affectFilter, condition, ModifierEffect.REPLACE_SITE_MODIFIER);
    }

    @Override
    public boolean isSiteReplaceable(DefaultGame game, String playerId) {
        return !playerId.equals(game.getGameState().getCurrentPlayerId());
    }
}
