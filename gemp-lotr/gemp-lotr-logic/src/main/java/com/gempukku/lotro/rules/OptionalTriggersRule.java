package com.gempukku.lotro.rules;

import com.gempukku.lotro.actions.AbstractActionProxy;
import com.gempukku.lotro.actions.OptionalTriggerAction;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.EffectResult;

import java.util.LinkedList;
import java.util.List;

public class OptionalTriggersRule {
    protected final DefaultActionsEnvironment actionsEnvironment;

    public OptionalTriggersRule(DefaultActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends OptionalTriggerAction> getOptionalBeforeTriggers(String playerId, DefaultGame game, Effect effect) {
                        List<OptionalTriggerAction> result = new LinkedList<>();
                        for (LotroPhysicalCard activatableCard : Filters.filter(game.getGameState().getInPlay(), game, getActivatableCardsFilter(playerId))) {
                            if (!game.getModifiersQuerying().hasTextRemoved(game, activatableCard)) {
                                final List<? extends OptionalTriggerAction> actions = activatableCard.getBlueprint().getOptionalBeforeTriggers(playerId, game, effect, activatableCard);
                                if (actions != null)
                                    result.addAll(actions);
                            }
                        }

                        return result;
                    }

                    @Override
                    public List<? extends OptionalTriggerAction> getOptionalAfterTriggerActions(String playerId, DefaultGame game, EffectResult effectResult) {
                        List<OptionalTriggerAction> result = new LinkedList<>();
                        for (LotroPhysicalCard activatableCard : Filters.filter(game.getGameState().getInPlay(), game, getActivatableCardsFilter(playerId))) {
                            if (!game.getModifiersQuerying().hasTextRemoved(game, activatableCard)) {
                                final List<? extends OptionalTriggerAction> actions =
                                        activatableCard.getOptionalAfterTriggerActions(playerId, game, effectResult,
                                                activatableCard);
                                if (actions != null)
                                    result.addAll(actions);
                            }
                        }

                        return result;
                    }
                }
        );
    }

    private Filter getActivatableCardsFilter(String playerId) {
        return Filters.or(
                Filters.and(CardType.SITE,
                        (Filter) (game, physicalCard) -> {
                            if (game.getGameState().getCurrentPhase().isRealPhase())
                                return Filters.currentSite.accepts(game, physicalCard);
                            return false;
                        }),
                Filters.and(Filters.not(CardType.SITE), Filters.owner(playerId), Filters.active));
    }
}