package com.gempukku.lotro.processes.turn.tribbles;

import com.gempukku.lotro.actions.Action;
import com.gempukku.lotro.actions.ActionStack;
import com.gempukku.lotro.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.cards.CardBlueprintLibrary;
import com.gempukku.lotro.cards.CardDeck;
import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.communication.UserFeedback;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.actions.OptionalTriggerAction;
import com.gempukku.lotro.actions.lotronly.SystemQueueAction;
import com.gempukku.lotro.adventure.InvalidSoloAdventureException;
import com.gempukku.lotro.decisions.ActionSelectionDecision;
import com.gempukku.lotro.decisions.CardActionSelectionDecision;
import com.gempukku.lotro.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.EffectResult;
import com.gempukku.lotro.effects.UnrespondableEffect;
import com.gempukku.lotro.gamestate.GameStats;
import com.gempukku.lotro.game.PlayerOrderFeedback;
import com.gempukku.lotro.processes.GameProcess;
import com.gempukku.lotro.game.TribblesGame;
import com.gempukku.lotro.game.PlayOrder;

import java.util.*;

// Action generates multiple Effects, both costs and result of an action are Effects.

// Decision is also an Effect.
public class TribblesTurnProcedure {
    private final UserFeedback _userFeedback;
    private final TribblesGame _game;
    private final ActionStack _actionStack;
    private GameProcess _gameProcess;
    private boolean _playedGameProcess;
    private final GameStats _gameStats;
    public TribblesTurnProcedure(TribblesGame tribblesGame, Map<String, CardDeck> decks, final UserFeedback userFeedback,
                                 CardBlueprintLibrary library, ActionStack actionStack,
                                 final PlayerOrderFeedback playerOrderFeedback) {
        _userFeedback = userFeedback;
        _game = tribblesGame;
        _actionStack = actionStack;

        _gameStats = new GameStats();

        _gameProcess = new TribblesPlayerOrderProcess(decks, library, playerOrderFeedback);
    }

    public GameStats getGameStats() {
        return _gameStats;
    }

    public void carryOutPendingActionsUntilDecisionNeeded() {
        while (!_userFeedback.hasPendingDecisions() && _game.getWinnerPlayerId() == null) {
            // First check for any "state-based" effects
            Set<EffectResult> effectResults = ((DefaultActionsEnvironment) _game.getActionsEnvironment()).consumeEffectResults();
            if (effectResults.size() > 0) {
                _actionStack.stackAction(new PlayOutEffectResults(effectResults));
            } else {
                if (_actionStack.isEmpty()) {
                    if (_playedGameProcess) {
                        _gameProcess = _gameProcess.getNextProcess();
                        _playedGameProcess = false;
                    } else {
                        _gameProcess.process(_game);
                        if (_gameStats.updateGameStats(_game))
                            _game.getGameState().sendGameStats(_gameStats);
                        _playedGameProcess = true;
                    }
                } else {
                    Effect effect = _actionStack.getNextEffect(_game);
                    if (effect != null) {
                        if (effect.getType() == null) {
                            try {
                                effect.playEffect(_game);
                            } catch (InvalidSoloAdventureException exp) {
                                _game.playerLost(_game.getGameState().getCurrentPlayerId(), exp.getMessage());
                            }
                        } else
                            _actionStack.stackAction(new PlayOutEffect(effect));
                    }
                }
            }

            if (_gameStats.updateGameStats(_game))
                _game.getGameState().sendGameStats(_gameStats);
        }
    }

    private class PlayOutEffect extends SystemQueueAction {
        private final Effect _effect;
        private boolean _initialized;

        private PlayOutEffect(Effect effect) {
            _effect = effect;
        }

        @Override
        public String getText(DefaultGame game) {
            return _effect.getText(game);
        }

        @Override
        public Effect nextEffect(DefaultGame game) {
            if (!_initialized) {
                _initialized = true;
                appendEffect(new PlayoutRequiredBeforeResponsesEffect(this, new HashSet<>(), _effect));
                appendEffect(new PlayoutOptionalBeforeResponsesEffect(this, new HashSet<>(), _game.getGameState().getPlayerOrder().getCounterClockwisePlayOrder(_game.getGameState().getCurrentPlayerId(), true), 0, _effect));
                appendEffect(new PlayEffect(_effect));
            }

            return getNextEffect();
        }
    }

    private class PlayEffect extends UnrespondableEffect {
        private final Effect _effect;

        private PlayEffect(Effect effect) {
            _effect = effect;
        }

        @Override
        protected void doPlayEffect(DefaultGame game) {
            try {
                _effect.playEffect(game);
            } catch (InvalidSoloAdventureException exp) {
                _game.playerLost(_game.getGameState().getCurrentPlayerId(), exp.getMessage());
            }
        }
    }

    private class PlayOutEffectResults extends SystemQueueAction {
        private final Set<EffectResult> _effectResults;
        private boolean _initialized;

        private PlayOutEffectResults(Set<EffectResult> effectResults) {
            _effectResults = effectResults;
        }

        @Override
        public Effect nextEffect(DefaultGame game) {
            if (!_initialized) {
                _initialized = true;
                List<Action> requiredResponses = _game.getActionsEnvironment().getRequiredAfterTriggers(_effectResults);
                if (requiredResponses.size() > 0)
                    appendEffect(new PlayoutAllActionsIfEffectNotCancelledEffect(this, requiredResponses));

                appendEffect(
                        new PlayoutOptionalAfterResponsesEffect(this, _game.getGameState().getPlayerOrder().getCounterClockwisePlayOrder(_game.getGameState().getCurrentPlayerId(), true), 0, _effectResults));
/*                appendEffect(
                        new UnrespondableEffect() {
                            @Override
                            protected void doPlayEffect(LotroGame game) {
                                if (hasKilledRingBearer())
                                    _game.checkRingBearerAlive();
                            }
                        }); */
            }
            return getNextEffect();
        }

    }

    private class PlayoutRequiredBeforeResponsesEffect extends UnrespondableEffect {
        private final SystemQueueAction _action;
        private final Set<LotroPhysicalCard> _cardTriggersUsed;
        private final Effect _effect;

        private PlayoutRequiredBeforeResponsesEffect(SystemQueueAction action, Set<LotroPhysicalCard> cardTriggersUsed, Effect effect) {
            _action = action;
            _cardTriggersUsed = cardTriggersUsed;
            _effect = effect;
        }

        @Override
        protected void doPlayEffect(DefaultGame game) {
            final List<Action> requiredBeforeTriggers = game.getActionsEnvironment().getRequiredBeforeTriggers(_effect);
            // Remove triggers already resolved
            requiredBeforeTriggers.removeIf(action -> _cardTriggersUsed.contains(action.getActionSource()));
            
            if (requiredBeforeTriggers.size() == 1) {
                _game.getActionsEnvironment().addActionToStack(requiredBeforeTriggers.get(0));
            } else if (requiredBeforeTriggers.size() > 1) {
                _game.getUserFeedback().sendAwaitingDecision(_game.getGameState().getCurrentPlayerId(),
                        new ActionSelectionDecision(game, 1, _effect.getText(game) + " - Required \"is about to\" responses", requiredBeforeTriggers) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                Action action = getSelectedAction(result);
                                if (action != null) {
                                    _game.getActionsEnvironment().addActionToStack(action);
                                    if (requiredBeforeTriggers.contains(action))
                                        _cardTriggersUsed.add(action.getActionSource());
                                    _game.getActionsEnvironment().addActionToStack(action);
                                    _action.insertEffect(new PlayoutRequiredBeforeResponsesEffect(_action, _cardTriggersUsed, _effect));
                                }
                            }
                        });
            }
        }
    }

    private class PlayoutOptionalBeforeResponsesEffect extends UnrespondableEffect {
        private final SystemQueueAction _action;
        private final Set<LotroPhysicalCard> _cardTriggersUsed;
        private final PlayOrder _playOrder;
        private final int _passCount;
        private final Effect _effect;

        private PlayoutOptionalBeforeResponsesEffect(SystemQueueAction action, Set<LotroPhysicalCard> cardTriggersUsed, PlayOrder playOrder, int passCount, Effect effect) {
            _action = action;
            _cardTriggersUsed = cardTriggersUsed;
            _playOrder = playOrder;
            _passCount = passCount;
            _effect = effect;
        }

        @Override
        public void doPlayEffect(DefaultGame game) {
            final String activePlayer = _playOrder.getNextPlayer();

            final List<Action> optionalBeforeTriggers = game.getActionsEnvironment().getOptionalBeforeTriggers(activePlayer, _effect);
            // Remove triggers already resolved
            optionalBeforeTriggers.removeIf(action -> _cardTriggersUsed.contains(action.getActionSource()));

            final List<Action> optionalBeforeActions = _game.getActionsEnvironment().getOptionalBeforeActions(activePlayer, _effect);

            List<Action> possibleActions = new LinkedList<>(optionalBeforeTriggers);
            possibleActions.addAll(optionalBeforeActions);

            if (possibleActions.size() > 0) {
                _game.getUserFeedback().sendAwaitingDecision(activePlayer,
                        new CardActionSelectionDecision(game, 1, _effect.getText(game) + " - Optional \"is about to\" responses", possibleActions) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                Action action = getSelectedAction(result);
                                if (action != null) {
                                    _game.getActionsEnvironment().addActionToStack(action);
                                    if (optionalBeforeTriggers.contains(action))
                                        _cardTriggersUsed.add(action.getActionSource());
                                    _action.insertEffect(new PlayoutOptionalBeforeResponsesEffect(_action, _cardTriggersUsed, _playOrder, 0, _effect));
                                } else {
                                    if ((_passCount + 1) < _playOrder.getPlayerCount()) {
                                        _action.insertEffect(new PlayoutOptionalBeforeResponsesEffect(_action, _cardTriggersUsed, _playOrder, _passCount + 1, _effect));
                                    }
                                }
                            }
                        });
            } else {
                if ((_passCount + 1) < _playOrder.getPlayerCount()) {
                    _action.insertEffect(new PlayoutOptionalBeforeResponsesEffect(_action, _cardTriggersUsed, _playOrder, _passCount + 1, _effect));
                }
            }
        }
    }

    private class PlayoutOptionalAfterResponsesEffect extends UnrespondableEffect {
        private final SystemQueueAction _action;
        private final PlayOrder _playOrder;
        private final int _passCount;
        private final Collection<? extends EffectResult> _effectResults;

        private PlayoutOptionalAfterResponsesEffect(SystemQueueAction action, PlayOrder playOrder, int passCount, Collection<? extends EffectResult> effectResults) {
            _action = action;
            _playOrder = playOrder;
            _passCount = passCount;
            _effectResults = effectResults;
        }

        @Override
        public void doPlayEffect(DefaultGame game) {
            final String activePlayer = _playOrder.getNextPlayer();

            final Map<OptionalTriggerAction, EffectResult> optionalAfterTriggers = _game.getActionsEnvironment().getOptionalAfterTriggers(activePlayer, _effectResults);

            final List<Action> optionalAfterActions = _game.getActionsEnvironment().getOptionalAfterActions(activePlayer, _effectResults);

            List<Action> possibleActions = new LinkedList<>(optionalAfterTriggers.keySet());
            possibleActions.addAll(optionalAfterActions);

            if (possibleActions.size() > 0) {
                _game.getUserFeedback().sendAwaitingDecision(activePlayer,
                        new CardActionSelectionDecision(game, 1, "Activate Tribble power?", possibleActions) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                Action action = getSelectedAction(result);
                                if (action != null) {
                                    _game.getActionsEnvironment().addActionToStack(action);
                                    if (optionalAfterTriggers.containsKey(action))
                                        optionalAfterTriggers.get(action).optionalTriggerUsed((OptionalTriggerAction) action);

                                    _action.insertEffect(new PlayoutOptionalAfterResponsesEffect(_action, _playOrder, 0, _effectResults));
                                } else {
                                    if ((_passCount + 1) < _playOrder.getPlayerCount()) {
                                        _action.insertEffect(new PlayoutOptionalAfterResponsesEffect(_action, _playOrder, _passCount + 1, _effectResults));
                                    }
                                }
                            }
                        });
            } else {
                if ((_passCount + 1) < _playOrder.getPlayerCount()) {
                    _action.insertEffect(new PlayoutOptionalAfterResponsesEffect(_action, _playOrder, _passCount + 1, _effectResults));
                }
            }
        }
    }

    private class PlayoutAllActionsIfEffectNotCancelledEffect extends UnrespondableEffect {
        private final SystemQueueAction _action;
        private final List<Action> _actions;

        private PlayoutAllActionsIfEffectNotCancelledEffect(SystemQueueAction action, List<Action> actions) {
            _action = action;
            _actions = actions;
        }

        @Override
        public void doPlayEffect(DefaultGame game) {
            if (_actions.size() == 1) {
                _game.getActionsEnvironment().addActionToStack(_actions.get(0));
            } else if (areAllActionsTheSame()) {
                Action anyAction = _actions.get(0);
                _actions.remove(anyAction);
                _game.getActionsEnvironment().addActionToStack(anyAction);
                _action.insertEffect(new PlayoutAllActionsIfEffectNotCancelledEffect(_action, _actions));
            } else {
                _game.getUserFeedback().sendAwaitingDecision(_game.getGameState().getCurrentPlayerId(),
                        new ActionSelectionDecision(_game, 1, "Required responses", _actions) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                Action action = getSelectedAction(result);
                                _game.getActionsEnvironment().addActionToStack(action);
                                _actions.remove(action);
                                _action.insertEffect(new PlayoutAllActionsIfEffectNotCancelledEffect(_action, _actions));
                            }
                        });
            }
        }

        private boolean areAllActionsTheSame() {
            Iterator<Action> actionIterator = _actions.iterator();

            Action firstAction = actionIterator.next();
            if (firstAction.getActionSource() == null)
                return false;

            while (actionIterator.hasNext()) {
                Action otherAction = actionIterator.next();
                if (otherAction.getActionSource() == null || otherAction.getActionSource().getBlueprint() != firstAction.getActionSource().getBlueprint())
                    return false;
            }
            return true;
        }
    }
}
