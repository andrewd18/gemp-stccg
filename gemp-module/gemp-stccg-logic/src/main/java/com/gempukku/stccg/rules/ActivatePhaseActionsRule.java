package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionsEnvironment;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;

import java.util.LinkedList;
import java.util.List;

public class ActivatePhaseActionsRule {
    private final ActionsEnvironment actionsEnvironment;

    public ActivatePhaseActionsRule(ActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends Action> getPhaseActions(String playerId) {
                        List<Action> result = new LinkedList<>();
                        if (actionsEnvironment.getGame().getGameState().getCurrentPhase() == Phase.EXECUTE_ORDERS) {
                            for (PhysicalCard activatableCard : Filters.filter(actionsEnvironment.getGame().getGameState().getAllCardsInPlay(), actionsEnvironment.getGame(), getActivatableCardsFilter(playerId))) {
                                if (!activatableCard.hasTextRemoved()) {
                                    final List<? extends Action> actions = activatableCard.getPhaseActionsInPlay(playerId);
                                    if (actions != null)
                                        result.addAll(actions);

                                    final List<? extends Action> extraActions = actionsEnvironment.getGame().getModifiersQuerying().getExtraPhaseActions(actionsEnvironment.getGame(), activatableCard);
                                    if (extraActions != null) {
                                        for (Action action : extraActions) {
                                            if (action != null)
                                                result.add(action);
                                        }
                                    }
                                }
                            }
                        }
                        return result;
                    }
                });
    }

    private Filter getActivatableCardsFilter(String playerId) {
        return Filters.and(Filters.owner(playerId), Filters.active);
    }
}
