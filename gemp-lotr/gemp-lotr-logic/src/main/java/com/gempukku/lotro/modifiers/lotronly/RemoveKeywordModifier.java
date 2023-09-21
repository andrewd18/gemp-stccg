package com.gempukku.lotro.modifiers.lotronly;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.modifiers.AbstractModifier;
import com.gempukku.lotro.modifiers.ModifierEffect;
import com.gempukku.lotro.condition.Condition;

public class RemoveKeywordModifier extends AbstractModifier implements KeywordAffectingModifier {
    private final Keyword _keyword;

    public RemoveKeywordModifier(LotroPhysicalCard physicalCard, Filterable affectFilter, Keyword keyword) {
        this(physicalCard, affectFilter, null, keyword);
    }

    public RemoveKeywordModifier(LotroPhysicalCard physicalCard, Filterable affectFilter, Condition condition, Keyword keyword) {
        super(physicalCard, "Loses " + keyword.getHumanReadable() + " keyword(s)", affectFilter, condition, ModifierEffect.REMOVE_KEYWORD_MODIFIER);
        _keyword = keyword;
    }

    @Override
    public Keyword getKeyword() {
        return _keyword;
    }

    @Override
    public boolean isKeywordRemoved(DefaultGame game, LotroPhysicalCard physicalCard, Keyword keyword) {
        return _keyword == keyword;
    }
}
