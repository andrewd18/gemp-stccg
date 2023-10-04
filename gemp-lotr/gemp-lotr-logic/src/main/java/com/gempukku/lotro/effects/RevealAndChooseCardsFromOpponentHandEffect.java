package com.gempukku.lotro.effects;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.game.PlayOrder;
import com.gempukku.lotro.rules.GameUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class RevealAndChooseCardsFromOpponentHandEffect extends AbstractSubActionEffect {
    private final String _playerId;
    private final String _opponentId;
    private final PhysicalCard _source;
    private final String _text;
    private final Filterable _selectionFilter;
    private final int _minChosen;
    private final int _maxChosen;

    protected RevealAndChooseCardsFromOpponentHandEffect(String playerId, String opponentId, PhysicalCard source, String text, Filterable selectionFilter, int minChosen, int maxChosen) {
        _playerId = playerId;
        _opponentId = opponentId;
        _source = source;
        _text = text;
        _selectionFilter = selectionFilter;
        _minChosen = minChosen;
        _maxChosen = maxChosen;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return (game.getModifiersQuerying().canLookOrRevealCardsInHand(game, _opponentId, _playerId))
                && game.getGameState().getHand(_opponentId).size() >= _minChosen;
    }

    @Override
    public void playEffect(DefaultGame game) {
        if (game.getModifiersQuerying().canLookOrRevealCardsInHand(game, _opponentId, _playerId)) {
            List<PhysicalCard> opponentHand = new LinkedList<>(game.getGameState().getHand(_opponentId));
            game.getGameState().sendMessage(GameUtils.getCardLink(_source) + " revealed " + _opponentId + " cards in hand - " + getAppendedNames(opponentHand));

            final PlayOrder playOrder = game.getGameState().getPlayerOrder().getCounterClockwisePlayOrder(_opponentId, false);
            // Skip hand owner (opponent)
            playOrder.getNextPlayer();

            String nextPlayer;
            while ((nextPlayer = playOrder.getNextPlayer()) != null) {
                if (nextPlayer.equals(_playerId)) {
                    Collection<PhysicalCard> selectable = Filters.filter(opponentHand, game, _selectionFilter);

                    game.getUserFeedback().sendAwaitingDecision(nextPlayer,
                            new ArbitraryCardsSelectionDecision(1, _text, opponentHand, new LinkedList<>(selectable), Math.min(_minChosen, selectable.size()), Math.min(_maxChosen, selectable.size())) {
                                @Override
                                public void decisionMade(String result) throws DecisionResultInvalidException {
                                    List<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                                    cardsSelected(selectedCards);
                                }
                            });
                } else if (!nextPlayer.equals(_opponentId)) {
                    game.getUserFeedback().sendAwaitingDecision(nextPlayer,
                            new ArbitraryCardsSelectionDecision(1, "Hand of " + _opponentId, opponentHand, Collections.emptySet(), 0, 0) {
                                @Override
                                public void decisionMade(String result) {
                                }
                            });
                }
            }
        }
    }

    @Override
    public String getText(DefaultGame game) {
        return null;
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    protected abstract void cardsSelected(List<PhysicalCard> selectedCards);
}
