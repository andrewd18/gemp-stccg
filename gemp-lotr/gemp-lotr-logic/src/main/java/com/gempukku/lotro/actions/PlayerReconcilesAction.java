package com.gempukku.lotro.actions;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.decisions.CardsSelectionDecision;
import com.gempukku.lotro.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.effects.DiscardCardsFromHandEffect;
import com.gempukku.lotro.effects.DrawCardsEffect;
import com.gempukku.lotro.effects.PlayoutDecisionEffect;
import com.gempukku.lotro.effects.TriggeringResultEffect;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.gamestate.GameState;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.results.ReconcileResult;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class PlayerReconcilesAction implements Action {
    private final DefaultGame _game;
    private final String _playerId;

    private Queue<Effect> _effectQueue;

    public PlayerReconcilesAction(DefaultGame game, String playerId) {
        _game = game;
        _playerId = playerId;
    }

    @Override
    public Type getType() {
        return Type.RECONCILE;
    }

    @Override
    public void setVirtualCardAction(boolean virtualCardAction) {
    }

    @Override
    public boolean isVirtualCardAction() {
        return false;
    }

    @Override
    public Phase getActionTimeword() {
        return null;
    }

    @Override
    public void setActionTimeword(Phase phase) {
    }

    @Override
    public String getPerformingPlayer() {
        return null;
    }

    @Override
    public void setPerformingPlayer(String playerId) {
    }

    @Override
    public LotroPhysicalCard getActionSource() {
        return null;
    }

    @Override
    public LotroPhysicalCard getActionAttachedToCard() {
        return null;
    }

    @Override
    public String getText(DefaultGame game) {
        return "Player reconciles";
    }

    @Override
    public Effect nextEffect(DefaultGame game) {
        if (_effectQueue == null) {

            _effectQueue = new LinkedList<>();

            final int handSize = game.getFormat().getHandSize();

            GameState gameState = _game.getGameState();
            final Set<? extends LotroPhysicalCard> cardsInHand = new HashSet<LotroPhysicalCard>(gameState.getHand(_playerId));

            // Formats which are set to end the game at the end of the regroup phase instead of at the start of the regroup phase
            // should prematurely end the game instead of doing a true reconcile.
            if(game.getFormat().winWhenShadowReconciles() && game.getGameState().getCurrentPhase() == Phase.REGROUP
                    && game.getGameState().getCurrentSiteNumber() == 9 && !_playerId.equals(game.getGameState().getCurrentPlayerId())) {
                game.getGameState().sendMessage("End of regroup phase reached.");
                _effectQueue.add(new TriggeringResultEffect(new ReconcileResult(_playerId), "Player reconciled"));
            }
            else {
                game.getGameState().sendMessage(_playerId + " reconciles");

                if (cardsInHand.size() > handSize) {
                    _effectQueue.add(new PlayoutDecisionEffect(_playerId,
                            new CardsSelectionDecision(1, "Choose cards to discard down to "+handSize, cardsInHand, cardsInHand.size() - handSize, cardsInHand.size() - handSize) {
                                @Override
                                public void decisionMade(String result) throws DecisionResultInvalidException {
                                    Set<LotroPhysicalCard> cards = getSelectedCardsByResponse(result);
                                    _effectQueue.add(new DiscardCardsFromHandEffect(null, _playerId, cards, false));
                                    _effectQueue.add(
                                            new TriggeringResultEffect(new ReconcileResult(_playerId), "Player reconciled"));
                                }
                            }));
                } else if (cardsInHand.size() > 0) {
                    _effectQueue.add(new PlayoutDecisionEffect(_playerId,
                            new CardsSelectionDecision(1, "Reconcile - choose card to discard or press DONE", cardsInHand, 0, 1) {
                                @Override
                                public void decisionMade(String result) throws DecisionResultInvalidException {
                                    Set<LotroPhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                                    if (selectedCards.size() > 0) {
                                        _effectQueue.add(new DiscardCardsFromHandEffect(null, _playerId, selectedCards, false));
                                    }
                                    int cardsInHandAfterDiscard = cardsInHand.size() - selectedCards.size();
                                    if (cardsInHandAfterDiscard < handSize) {
                                        _effectQueue.add(new DrawCardsEffect(PlayerReconcilesAction.this, _playerId, handSize - cardsInHandAfterDiscard));
                                    }
                                    _effectQueue.add(
                                            new TriggeringResultEffect(new ReconcileResult(_playerId), "Player reconciled"));
                                }
                            }));
                } else {
                    _effectQueue.add(new DrawCardsEffect(PlayerReconcilesAction.this, _playerId, handSize));
                    _effectQueue.add(
                            new TriggeringResultEffect(new ReconcileResult(_playerId), "Player reconciled"));
                }
            }
        }

        return _effectQueue.poll();
    }
}
