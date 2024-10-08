package com.gempukku.stccg.at.effects;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.draw.DrawCardsEffect;
import com.gempukku.stccg.at.AbstractAtTest;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCardGeneric;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.game.Preventable;
import com.gempukku.stccg.requirement.trigger.TriggerConditions;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class DrawEffectAtTest extends AbstractAtTest {
    @Test
    public void drawingSuccessful() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        skipMulligans();

        final PhysicalCardGeneric merry = new PhysicalCardGeneric(_game, 101, P1, _cardLibrary.getCardBlueprint("1_303"));

        _game.getGameState().putCardOnTopOfDeck(merry);

        final AtomicInteger triggerCount = new AtomicInteger(0);

        _game.getActionsEnvironment().addUntilEndOfTurnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(EffectResult effectResult) {
                        if (TriggerConditions.forEachCardDrawn(effectResult, P1)) {
                            RequiredTriggerAction action = new RequiredTriggerAction(merry);
                            action.appendEffect(
                                    new IncrementEffect(triggerCount));
                            return Collections.singletonList(action);
                        }
                        return null;
                    }
                });

        DrawCardsEffect drawEffect = new DrawCardsEffect(_game, null, P1, 1);

        carryOutEffectInPhaseActionByPlayer(P1, drawEffect);

        assertEquals(1, _game.getGameState().getHand(P1).size());
        assertEquals(0, _game.getGameState().getDrawDeck(P1).size());
        assertTrue(_game.getGameState().getHand(P1).contains(merry));
        assertTrue(drawEffect.wasCarriedOut());

        assertEquals(1, triggerCount.get());
    }

    @Test
    public void drawingMultipleNotSuccessful() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        skipMulligans();

        final PhysicalCardGeneric merry = new PhysicalCardGeneric(_game, 101, P1, _cardLibrary.getCardBlueprint("1_303"));

        _game.getGameState().putCardOnTopOfDeck(merry);

        final AtomicInteger triggerCount = new AtomicInteger(0);

        _game.getActionsEnvironment().addUntilEndOfTurnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(EffectResult effectResult) {
                        if (TriggerConditions.forEachCardDrawn(effectResult, P1)) {
                            RequiredTriggerAction action = new RequiredTriggerAction(merry);
                            action.appendEffect(
                                    new IncrementEffect(triggerCount));
                            return Collections.singletonList(action);
                        }
                        return null;
                    }
                });

        DrawCardsEffect drawEffect = new DrawCardsEffect(_game, null, P1, 2);

        carryOutEffectInPhaseActionByPlayer(P1, drawEffect);

        assertEquals(1, _game.getGameState().getHand(P1).size());
        assertEquals(0, _game.getGameState().getDrawDeck(P1).size());
        assertTrue(_game.getGameState().getHand(P1).contains(merry));
        assertFalse(drawEffect.wasCarriedOut());

        assertEquals(1, triggerCount.get());
    }

    @Test
    public void drawingMultipleSuccessful() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        skipMulligans();

        final PhysicalCardGeneric merry = new PhysicalCardGeneric(_game, 101, P1, _cardLibrary.getCardBlueprint("1_303"));
        final PhysicalCardGeneric merry2 = new PhysicalCardGeneric(_game, 102, P1, _cardLibrary.getCardBlueprint("1_303"));

        _game.getGameState().putCardOnTopOfDeck(merry);
        _game.getGameState().putCardOnTopOfDeck(merry2);

        final AtomicInteger triggerCount = new AtomicInteger(0);

        _game.getActionsEnvironment().addUntilEndOfTurnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(EffectResult effectResult) {
                        if (TriggerConditions.forEachCardDrawn(effectResult, P1)) {
                            RequiredTriggerAction action = new RequiredTriggerAction(merry);
                            action.appendEffect(
                                    new IncrementEffect(triggerCount));
                            return Collections.singletonList(action);
                        }
                        return null;
                    }
                });

        DrawCardsEffect drawEffect = new DrawCardsEffect(_game, null, P1, 2);

        carryOutEffectInPhaseActionByPlayer(P1, drawEffect);

        assertEquals(2, _game.getGameState().getHand(P1).size());
        assertEquals(0, _game.getGameState().getDrawDeck(P1).size());
        assertTrue(_game.getGameState().getHand(P1).contains(merry));
        assertTrue(_game.getGameState().getHand(P1).contains(merry2));
        assertTrue(drawEffect.wasCarriedOut());

        assertEquals(2, triggerCount.get());
    }

    @Test
    public void insteadOfDraw() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        skipMulligans();

        final PhysicalCardGeneric merry = new PhysicalCardGeneric(_game, 101, P1, _cardLibrary.getCardBlueprint("1_303"));

        _game.getGameState().putCardOnTopOfDeck(merry);

        final AtomicInteger triggerCount = new AtomicInteger(0);
        final AtomicInteger preventCount = new AtomicInteger(0);

        _game.getActionsEnvironment().addUntilEndOfTurnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(EffectResult effectResult) {
                        if (TriggerConditions.forEachCardDrawn(effectResult, P1)) {
                            RequiredTriggerAction action = new RequiredTriggerAction(merry);
                            action.appendEffect(
                                    new IncrementEffect(triggerCount));
                            return Collections.singletonList(action);
                        }
                        return null;
                    }

                    @Override
                    public List<? extends Action> getRequiredBeforeTriggers(Effect effect) {
                        if (TriggerConditions.isDrawingACard(effect, _game, P1)) {
                            RequiredTriggerAction action = new RequiredTriggerAction(merry);
                            action.appendEffect(
                                    new PreventEffect(_game, (Preventable) effect));
                            action.appendEffect(
                                    new IncrementEffect(preventCount));
                            return Collections.singletonList(action);
                        }
                        return null;
                    }
                });

        DrawCardsEffect drawEffect = new DrawCardsEffect(_game, null, P1, 1);

        carryOutEffectInPhaseActionByPlayer(P1, drawEffect);

        assertEquals(0, _game.getGameState().getHand(P1).size());
        assertEquals(1, _game.getGameState().getDrawDeck(P1).size());
        assertFalse(drawEffect.wasCarriedOut());

        assertEquals(0, triggerCount.get());
        assertEquals(1, preventCount.get());
    }
}
