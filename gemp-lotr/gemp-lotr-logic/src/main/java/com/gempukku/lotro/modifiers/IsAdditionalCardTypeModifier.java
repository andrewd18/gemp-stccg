package com.gempukku.lotro.modifiers;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.condition.Condition;

public class IsAdditionalCardTypeModifier extends AbstractModifier {
    private final CardType _cardType;

    public IsAdditionalCardTypeModifier(PhysicalCard source, Filterable affectFilter, CardType cardType) {
        this(source, affectFilter, null, cardType);
    }

    public IsAdditionalCardTypeModifier(PhysicalCard source, Filterable affectFilter, Condition condition, CardType cardType) {
        super(source, "Has additional card type - " + cardType.toString(), affectFilter, condition, ModifierEffect.ADDITIONAL_CARD_TYPE);
        _cardType = cardType;
    }

    @Override
    public boolean isAdditionalCardTypeModifier(DefaultGame game, PhysicalCard physicalCard, CardType cardType) {
        return cardType == _cardType;
    }
}
