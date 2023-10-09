package com.gempukku.stccg.results;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.EffectResult;

public class DiscardCardFromDeckResult extends EffectResult {
    private final PhysicalCard _source;
    private final PhysicalCard _card;
    private final boolean _forced;

    public DiscardCardFromDeckResult(PhysicalCard source, PhysicalCard card, boolean forced) {
        super(Type.FOR_EACH_DISCARDED_FROM_DECK);
        _source = source;
        _card = card;
        _forced = forced;
    }

    public PhysicalCard getSource() {
        return _source;
    }

    public boolean isForced() {
        return _forced;
    }


    public PhysicalCard getDiscardedCard() {
        return _card;
    }
}