package com.gempukku.stccg.at;

import com.gempukku.stccg.cards.PhysicalCardImpl;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AttachAtTest extends AbstractAtTest {
    @Test
    public void extraPossessionClassAttachedTo() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardImpl aragorn = createCard(P1, "1_89");
        PhysicalCardImpl rangersSword = createCard(P1, "1_112");
        PhysicalCardImpl knifeOfTheGaladhrim = createCard(P1, "9_17");

        _game.getGameState().addCardToZone(_game, aragorn, Zone.FREE_CHARACTERS);
        _game.getGameState().attachCard(_game, rangersSword, aragorn);
        _game.getGameState().addCardToZone(_game, knifeOfTheGaladhrim, Zone.HAND);

        skipMulligans();

        playerDecided(P1, getCardActionId(P1, "Play Knife"));
        assertEquals(Zone.ATTACHED, knifeOfTheGaladhrim.getZone());
    }
}
