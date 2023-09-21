package com.gempukku.lotro.rules.lotronly;

import com.gempukku.lotro.actions.AbstractActionProxy;
import com.gempukku.lotro.actions.RequiredTriggerAction;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.effects.RemoveTwilightEffect;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.evaluator.LocationEvaluator;
import com.gempukku.lotro.effects.EffectResult;
import com.gempukku.lotro.game.TriggerConditions;

import java.util.*;

public class ConcealedRule {
    private final DefaultActionsEnvironment _actionsEnvironment;

    public ConcealedRule(DefaultActionsEnvironment actionsEnvironment) {
        _actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        _actionsEnvironment.addAlwaysOnActionProxy(
            new AbstractActionProxy() {
                @Override
                public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(final DefaultGame game, EffectResult effectResult) {
                    if (TriggerConditions.moves(effectResult)
                            && game.getGameState().getCurrentPhase() == Phase.FELLOWSHIP) {
                        List<RequiredTriggerAction> actions = new ArrayList<>();

                        int twilight = Filters.filterActive(game, CardType.COMPANION, Keyword.CONCEALED).size();

                        if(twilight == 0)
                            return null;

                        final RequiredTriggerAction action = new RequiredTriggerAction(null);
                        LocationEvaluator loc = new LocationEvaluator(twilight, 0, Keyword.EXPOSED);
                        action.appendEffect(new RemoveTwilightEffect(new LocationEvaluator(twilight, 0, Keyword.EXPOSED)));

                        if(loc.evaluateExpression(game, null) == 0) {
                            action.setText("Concealed companions were exposed!  No twilight was removed.");
                        }
                        else {
                            action.setText("Concealed companions trigger removal of " + twilight + " twilight.");
                        }
                        actions.add(action);

                        return actions;
                    }
                    return null;
                }
            });
    }
}
