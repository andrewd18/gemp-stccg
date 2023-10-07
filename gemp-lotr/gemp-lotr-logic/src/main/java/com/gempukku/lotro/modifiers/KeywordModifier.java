package com.gempukku.lotro.modifiers;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.condition.Condition;
import com.gempukku.lotro.evaluator.ConstantEvaluator;
import com.gempukku.lotro.evaluator.Evaluator;

public class KeywordModifier extends AbstractModifier implements KeywordAffectingModifier {
    private final Keyword _keyword;
    private final Evaluator _evaluator;

    public KeywordModifier(PhysicalCard physicalCard, Filterable affectFilter, Keyword keyword) {
        this(physicalCard, affectFilter, keyword, 1);
    }

    public KeywordModifier(PhysicalCard physicalCard, Filterable affectFilter, Keyword keyword, int count) {
        this(physicalCard, affectFilter, null, keyword, count);
    }

    public KeywordModifier(PhysicalCard physicalCard, Filterable affectFilter, Condition condition, Keyword keyword, int count) {
        this(physicalCard, affectFilter, condition, keyword, new ConstantEvaluator(count));
    }

    public KeywordModifier(PhysicalCard physicalCard, Filterable affectFilter, Condition condition, Keyword keyword, Evaluator evaluator) {
        super(physicalCard, null, affectFilter, condition, ModifierEffect.GIVE_KEYWORD_MODIFIER);
        _keyword = keyword;
        _evaluator = evaluator;
    }

    @Override
    public Keyword getKeyword() {
        return _keyword;
    }

    @Override
    public String getText(DefaultGame game, PhysicalCard self) {
        if (_keyword.isMultiples()) {
            int count = _evaluator.evaluateExpression(game, self);
            return _keyword.getHumanReadable() + " +" + count;
        }
        return _keyword.getHumanReadable();
    }

    @Override
    public boolean hasKeyword(DefaultGame game, PhysicalCard physicalCard, Keyword keyword) {
        return (keyword == _keyword && _evaluator.evaluateExpression(game, physicalCard) > 0);
    }

    @Override
    public int getKeywordCountModifier(DefaultGame game, PhysicalCard physicalCard, Keyword keyword) {
        if (keyword == _keyword)
            return _evaluator.evaluateExpression(game, physicalCard);
        else
            return 0;
    }
}
