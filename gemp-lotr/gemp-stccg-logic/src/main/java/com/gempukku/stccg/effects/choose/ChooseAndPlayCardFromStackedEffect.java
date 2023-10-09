package com.gempukku.stccg.effects.choose;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.PlayUtils;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.PlayPermanentAction;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.UnrespondableEffect;

import java.util.*;

public class ChooseAndPlayCardFromStackedEffect implements Effect {
    private final String _playerId;
    private final Filterable _stackedOn;
    private final Filter _filter;
    private final int _twilightModifier;
    private CostToEffectAction _playCardAction;

    public ChooseAndPlayCardFromStackedEffect(String playerId, Filterable stackedOn, Filterable... filter) {
        this(playerId, stackedOn, 0, filter);
    }

    public ChooseAndPlayCardFromStackedEffect(String playerId, Filterable stackedOn, int twilightModifier, Filterable... filter) {
        _playerId = playerId;
        _stackedOn = stackedOn;
        _filter = Filters.and(filter);
        _twilightModifier = twilightModifier;
    }

    @Override
    public String getText(DefaultGame game) {
        return "Play card from stacked";
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return getPlayableFromStacked(game).size() > 0;
    }

    @Override
    public Type getType() {
        return null;
    }

    private Collection<PhysicalCard> getPlayableFromStacked(DefaultGame game) {
        Set<PhysicalCard> possibleCards = new HashSet<>();
        for (PhysicalCard stackedOnCard : Filters.filterActive(game, _stackedOn))
            possibleCards.addAll(Filters.filter(game.getGameState().getStackedCards(stackedOnCard), game, _filter, Filters.playable(game, _twilightModifier)));

        return possibleCards;
    }

    @Override
    public void playEffect(final DefaultGame game) {
        Collection<PhysicalCard> playableFromStacked = getPlayableFromStacked(game);
        if (playableFromStacked.size() > 0) {
            game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new ArbitraryCardsSelectionDecision(1, "Choose a card to play", new LinkedList<>(playableFromStacked), 1, 1) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            List<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                            if (selectedCards.size() > 0) {
                                final PhysicalCard selectedCard = selectedCards.get(0);
                                _playCardAction = PlayUtils.getPlayCardAction(game, selectedCard, _twilightModifier, Filters.any, false);
                                _playCardAction.appendEffect(
                                        new UnrespondableEffect() {
                                            @Override
                                            protected void doPlayEffect(DefaultGame game) {
                                                afterCardPlayed(selectedCard);
                                            }
                                        });
                                game.getActionsEnvironment().addActionToStack(_playCardAction);
                            }
                        }
                    });
        }
    }

    protected void afterCardPlayed(PhysicalCard cardPlayed) {
    }

    @Override
    public boolean wasCarriedOut() {
        if (_playCardAction == null)
            return false;
        if (_playCardAction instanceof PlayPermanentAction)
            return _playCardAction.wasCarriedOut();
        return true;
    }
}