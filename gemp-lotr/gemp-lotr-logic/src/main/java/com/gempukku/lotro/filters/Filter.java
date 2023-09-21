package com.gempukku.lotro.filters;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.game.DefaultGame;

public interface Filter extends Filterable {
    boolean accepts(DefaultGame game, LotroPhysicalCard physicalCard);
}
