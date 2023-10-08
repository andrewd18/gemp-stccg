package com.gempukku.stccg.at.effects;

import com.gempukku.stccg.at.AbstractAtTest;
import com.gempukku.stccg.common.Zone;
import com.gempukku.stccg.cards.PhysicalCardImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PlayCardFromStackedAtTest extends AbstractAtTest {
    @Test
    public void playFromGoblinSwarms() throws Exception {
        initializeSimplestGame();

        skipMulligans();

        final PhysicalCardImpl goblinSwarms = createCard(P2, "1_183");
        _game.getGameState().addCardToZone(_game, goblinSwarms, Zone.SUPPORT);

        final PhysicalCardImpl goblinRunner = createCard(P2, "1_178");
        _game.getGameState().stackCard(_game, goblinRunner, goblinSwarms);

        _game.getGameState().setTwilight(20);

        // Fellowship phase
        playerDecided(P1, "");

        playerDecided(P2, getCardActionId(P2, "Use Goblin Swarms"));

        assertEquals(Zone.SHADOW_CHARACTERS, goblinRunner.getZone());
    }
}
