package com.gempukku.lotro.rules.lotronly;

import com.gempukku.lotro.actions.AbstractActionProxy;
import com.gempukku.lotro.actions.OptionalTriggerAction;
import com.gempukku.lotro.actions.RequiredTriggerAction;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.effects.EffectResult;
import com.gempukku.lotro.results.KilledResult;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class KilledCardRule {
    private final DefaultActionsEnvironment _actionsEnvironment;

    public KilledCardRule(DefaultActionsEnvironment actionsEnvironment) {
        _actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        _actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(DefaultGame game, EffectResult effectResult) {
                        if (effectResult.getType() == EffectResult.Type.ANY_NUMBER_KILLED) {
                            KilledResult killResult = (KilledResult) effectResult;
                            Set<PhysicalCard> killedCards = killResult.getKilledCards();
                            List<RequiredTriggerAction> actions = new LinkedList<>();
                            for (PhysicalCard killedCard : killedCards) {
                                RequiredTriggerAction trigger = killedCard.getBlueprint().getKilledRequiredTrigger(game, killedCard);
                                if (trigger != null) {
                                    trigger.setVirtualCardAction(true);
                                    actions.add(trigger);
                                }
                            }
                            return actions;
                        }
                        return null;
                    }

                    @Override
                    public List<? extends OptionalTriggerAction> getOptionalAfterTriggerActions(String playerId, DefaultGame game, EffectResult effectResult) {
                        if (effectResult.getType() == EffectResult.Type.ANY_NUMBER_KILLED) {
                            KilledResult killResult = (KilledResult) effectResult;
                            Set<PhysicalCard> killedCards = killResult.getKilledCards();
                            List<OptionalTriggerAction> actions = new LinkedList<>();
                            for (PhysicalCard killedCard : killedCards) {
                                OptionalTriggerAction trigger = killedCard.getBlueprint().getKilledOptionalTrigger(playerId, game, killedCard);
                                if (trigger != null) {
                                    trigger.setVirtualCardAction(true);
                                    actions.add(trigger);
                                }
                            }
                            return actions;
                        }
                        return null;
                    }
                });
    }
}
