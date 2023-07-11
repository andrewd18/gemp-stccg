package com.gempukku.lotro.game.effects;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.game.GameUtils;
import com.gempukku.lotro.game.timing.Effect;
import com.gempukku.lotro.game.timing.results.HealResult;

import java.util.Collection;

public class HealCharactersEffect extends AbstractPreventableCardEffect {
    private final PhysicalCard _source;
    private final String _performingPlayer;

    public HealCharactersEffect(PhysicalCard source, String performingPlayer, PhysicalCard... cards) {
        super(cards);
        _source = source;
        _performingPlayer = performingPlayer;
    }

    public HealCharactersEffect(PhysicalCard source, String performingPlayer, Filterable... filter) {
        super(filter);
        _source = source;
        _performingPlayer = performingPlayer;
    }

    @Override
    protected Filter getExtraAffectableFilter() {
        return Filters.and(
                Filters.wounded,
                new Filter() {
                    @Override
                    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
                        return game.getModifiersQuerying().canBeHealed(game, physicalCard);
                    }
                });
    }

    @Override
    public Effect.Type getType() {
        return Type.BEFORE_HEALED;
    }

    @Override
    public String getText(DefaultGame game) {
        Collection<PhysicalCard> cards = getAffectedCardsMinusPrevented(game);
        return "Heal - " + getAppendedTextNames(cards);
    }

    @Override
    protected void playoutEffectOn(DefaultGame game, Collection<PhysicalCard> cards) {
        Collection<PhysicalCard> cardsToHeal = getAffectedCardsMinusPrevented(game);

        if (cardsToHeal.size() > 0) {
            game.getGameState().sendMessage(GameUtils.getCardLink(_source) + " heals " + getAppendedNames(cardsToHeal));
            for (PhysicalCard cardToHeal : cardsToHeal) {
                game.getGameState().removeWound(cardToHeal);
                game.getActionsEnvironment().emitEffectResult(new HealResult(cardToHeal, _source, _performingPlayer));
            }
        }
    }
}
