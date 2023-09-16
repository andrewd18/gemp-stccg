package com.gempukku.lotro.actions.lotronly;

import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.effects.WoundCharactersEffect;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.modifiers.ModifiersQuerying;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.results.NormalSkirmishResult;

import java.util.Set;

public class ResolveSkirmishDamageAction extends RequiredTriggerAction {
    private final NormalSkirmishResult _skirmishResult;

    private Integer _damageToDo;
    private int _remainingDamage;

    public ResolveSkirmishDamageAction(NormalSkirmishResult skirmishResult) {
        super(null);
        setText("Resolve skirmish damage");
        _skirmishResult = skirmishResult;
    }

    @Override
    public Type getType() {
        return Type.RESOLVE_DAMAGE;
    }

    @Override
    public Effect nextEffect(DefaultGame game) {
        if (_damageToDo == null) {
            _damageToDo = calculateDamageToDo(game);
            _remainingDamage = _damageToDo;
        }

        if (_remainingDamage > 0) {
            _remainingDamage--;
            return new WoundCharactersEffect(_skirmishResult.getWinners(), Filters.in(_skirmishResult.getInSkirmishLosers()),
                    (Filter) (game1, physicalCard) -> game1.getModifiersQuerying().canTakeWoundsFromLosingSkirmish(game1, physicalCard, _skirmishResult.getWinners()));
        }

        return null;
    }

    public void consumeRemainingDamage() {
        _remainingDamage = 0;
    }

    public int getRemainingDamage() {
        return _remainingDamage;
    }

    private int calculateDamageToDo(DefaultGame game) {
        Set<LotroPhysicalCard> winners = _skirmishResult.getWinners();
        int dmg = 1;
        ModifiersQuerying modifiersQuerying = game.getModifiersQuerying();
        for (LotroPhysicalCard winner : winners)
            dmg += game.getModifiersQuerying().getKeywordCount(game, winner, Keyword.DAMAGE);

        return dmg;
    }
}
