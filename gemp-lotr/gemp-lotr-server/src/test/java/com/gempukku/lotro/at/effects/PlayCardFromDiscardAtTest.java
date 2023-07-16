package com.gempukku.lotro.at.effects;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.cards.lotronly.LotroPhysicalCardImpl;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.decisions.AwaitingDecisionType;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class PlayCardFromDiscardAtTest extends AbstractAtTest {
    @Test
    public void cantPlayCardDiscardedWithTheyAreComing() throws Exception {
        initializeSimplestGame();

        final LotroPhysicalCardImpl theyAreComing = createCard(P2,  "1_196");
        final LotroPhysicalCardImpl goblinSneakInDiscard = createCard(P2, "1_181");

        _game.getGameState().addCardToZone(_game, theyAreComing, Zone.SUPPORT);
        _game.getGameState().addCardToZone(_game, goblinSneakInDiscard, Zone.DISCARD);

        for (int i = 0; i < 3; i++) {
            final LotroPhysicalCardImpl goblinRunner = createCard(P2, "1_178");
            _game.getGameState().addCardToZone(_game, goblinRunner, Zone.HAND);
        }

        skipMulligans();

        _game.getGameState().setTwilight(10);

        assertEquals(Phase.FELLOWSHIP, _game.getGameState().getCurrentPhase());
        playerDecided(P1, "");

        playerDecided(P2, getCardActionId(P2, "Use They Are Coming"));

        Map<String, String[]> decisionParameters = _userFeedback.getAwaitingDecision(P2).getDecisionParameters();
        String[] cardId = decisionParameters.get("cardId");
        assertEquals(1, cardId.length);
        assertEquals(String.valueOf(goblinSneakInDiscard.getCardId()), cardId[0]);
    }

    @Test
    public void flameOfUdunCantPlayCardIfNotEnoughTwilight() throws Exception {
        initializeSimplestGame();

        skipMulligans();

        // Fellowship phase
        playerDecided(P1, "");

        // Shadow phase
        playerDecided(P2, "");

        // End regroup phase
        assertEquals(Phase.REGROUP, _game.getGameState().getCurrentPhase());
        playerDecided(P1, "");
        playerDecided(P2, "");

        final LotroPhysicalCardImpl goblinRunner = createCard(P2, "1_178");
        _game.getGameState().addCardToZone(_game, goblinRunner, Zone.HAND);

        final LotroPhysicalCardImpl balrog = createCard(P2,  "2_52");
        _game.getGameState().addCardToZone(_game, balrog, Zone.HAND);

        final LotroPhysicalCardImpl goblinSneakInDiscard = createCard(P2, "1_181");
        _game.getGameState().addCardToZone(_game, goblinSneakInDiscard, Zone.DISCARD);

        _game.getGameState().setTwilight(18);

        // Move again
        playerDecided(P1, "0");

        playerDecided(P2, getCardActionId(P2, "Play Goblin Runner"));
        playerDecided(P2, getCardActionId(P2, "Play The Balrog"));
        assertNull(getCardActionId(P2, "Use The Balrog"));
    }

    @Test
    public void flameOfUdunCanPlayCardIfEnoughTwilight() throws Exception {
        initializeSimplestGame();

        skipMulligans();

        // Fellowship phase
        playerDecided(P1, "");

        // Shadow phase
        playerDecided(P2, "");

        // End regroup phase
        assertEquals(Phase.REGROUP, _game.getGameState().getCurrentPhase());
        playerDecided(P1, "");
        playerDecided(P2, "");

        final LotroPhysicalCardImpl goblinRunner = createCard(P2, "1_178");
        _game.getGameState().addCardToZone(_game, goblinRunner, Zone.HAND);

        final LotroPhysicalCardImpl balrog = createCard(P2,  "2_52");
        _game.getGameState().addCardToZone(_game, balrog, Zone.HAND);

        final LotroPhysicalCardImpl goblinSneakInDiscard = createCard(P2, "1_181");
        _game.getGameState().addCardToZone(_game, goblinSneakInDiscard, Zone.DISCARD);

        _game.getGameState().setTwilight(20);

        // Move again
        playerDecided(P1, "0");

        playerDecided(P2, getCardActionId(P2, "Play Goblin Runner"));
        playerDecided(P2, getCardActionId(P2, "Play The Balrog"));
        playerDecided(P2, getCardActionId(P2, "Use The Balrog"));
        assertEquals(Zone.SHADOW_CHARACTERS, goblinSneakInDiscard.getZone());
    }

    @Test
    public void ulaireNerteaCantPlayMinionsOnGreatRiver() throws Exception {
        initializeSimplestGame();

        for (int i=0; i<4; i++) {
            final LotroPhysicalCardImpl greatRiver = createCard(P1, "1_306");
            _game.getGameState().addCardToZone(_game, greatRiver, Zone.FREE_CHARACTERS);
        }

        skipMulligans();

        final LotroPhysicalCardImpl greatRiver = createCard(P2, "3_118");
        greatRiver.setSiteNumber(2);
        _game.getGameState().addCardToZone(_game, greatRiver, Zone.ADVENTURE_PATH);

        final LotroPhysicalCardImpl ulaireNertea = createCard(P2, "1_234");
        _game.getGameState().addCardToZone(_game, ulaireNertea, Zone.HAND);

        final LotroPhysicalCardImpl goblinRunner = createCard(P2, "1_178");
        _game.getGameState().addCardToZone(_game, goblinRunner, Zone.DISCARD);

        _game.getGameState().setTwilight(20);

        // Fellowship phase
        playerDecided(P1, "");

        assertEquals(greatRiver, _game.getGameState().getCurrentSite());

        playerDecided(P2, getCardActionId(P2, "Play Úlairë Nertëa"));

        assertFalse(_userFeedback.getAwaitingDecision(P2).getDecisionType() == AwaitingDecisionType.MULTIPLE_CHOICE);
    }

    @Test
    public void azogCantPlayTrollFromDiscardInRivendell() throws Exception {
        initializeSimplestGame();

        skipMulligans();

        final LotroPhysicalCardImpl rivendell = createCard(P2, "30_51");
        rivendell.setSiteNumber(2);
        _game.getGameState().addCardToZone(_game, rivendell, Zone.ADVENTURE_PATH);

        LotroPhysicalCardImpl azog = createCard(P2, "32_28");
        _game.getGameState().addCardToZone(_game, azog, Zone.SHADOW_CHARACTERS);

        LotroPhysicalCardImpl watchfulOrc = createCard(P2, "30_40");
        _game.getGameState().addCardToZone(_game, watchfulOrc, Zone.SHADOW_CHARACTERS);

        LotroPhysicalCardImpl demolitionTroll = createCard(P2, "32_31");
        _game.getGameState().addCardToZone(_game, demolitionTroll, Zone.DISCARD);

        _game.getGameState().setTwilight(20);

        // Fellowship phase
        playerDecided(P1, "");
        // Shadow phase
        playerDecided(P2, getCardActionId(P2, "Use Watchful Orc"));
        // Choose Battleground
        playerDecided(P2, "0");
        playerDecided(P2, "");
        // Maneuver phase
        playerDecided(P1, "");

        assertNull(getCardActionId(P2, "Use Azog"));
    }
}
