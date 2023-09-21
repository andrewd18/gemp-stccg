package com.gempukku.lotro.effects;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.game.DefaultGame;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class LookAtOpponentsHandEffect extends AbstractEffect {
    private final String _playerId;
    private final String _opponentId;

    public LookAtOpponentsHandEffect(String playerId, String opponentId) {
        _playerId = playerId;
        _opponentId = opponentId;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return game.getModifiersQuerying().canLookOrRevealCardsInHand(game, _opponentId, _playerId);
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        if (game.getModifiersQuerying().canLookOrRevealCardsInHand(game, _opponentId, _playerId)) {
            List<LotroPhysicalCard> opponentHand = new LinkedList<>(game.getGameState().getHand(_opponentId));

            game.getGameState().sendMessage(_playerId + " looked at " + _opponentId + "'s entire hand");

            if (opponentHand.size() > 0) {
                game.getGameState().sendMessage(_playerId + " looked at " + _opponentId + "'s entire hand");

                game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new ArbitraryCardsSelectionDecision(1, "Opponent's hand", opponentHand, Collections.emptyList(), 0, 0) {
                        @Override
                        public void decisionMade(String result) {
                        }
                    });
            }
            else {
                game.getGameState().sendMessage("No cards in " + _opponentId + " hand to look at");
            }

            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
