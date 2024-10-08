package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public class CardTransferredResult extends EffectResult {
    private final PhysicalCard _transferredCard;
    private final PhysicalCard _transferredFrom;
    private final PhysicalCard _transferredTo;

    public CardTransferredResult(PhysicalCard transferredCard, PhysicalCard transferredFrom, PhysicalCard transferredTo) {
        super(EffectResult.Type.CARD_TRANSFERRED, transferredCard);
        _transferredCard = transferredCard;
        _transferredFrom = transferredFrom;
        _transferredTo = transferredTo;
    }

    public PhysicalCard getTransferredCard() {
        return _transferredCard;
    }

    public PhysicalCard getTransferredFrom() {
        return _transferredFrom;
    }

    public PhysicalCard getTransferredTo() {
        return _transferredTo;
    }
}
