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
import com.gempukku.stccg.modifiers.ModifierFlag;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.UnrespondableEffect;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ChooseAndPlayCardFromDiscardEffect implements Effect {
    private final String _playerId;
    private final Filter _filter;
    private final int _twilightModifier;
    private CostToEffectAction _playCardAction;

    public ChooseAndPlayCardFromDiscardEffect(String playerId, DefaultGame game, Filterable... filter) {
        this(playerId, game, 0, filter);
    }

    public ChooseAndPlayCardFromDiscardEffect(String playerId, DefaultGame game, int twilightModifier, Filterable... filter) {
        _playerId = playerId;
        // Card has to be in discard when you start playing the card (we need to copy the collection)
        _filter = Filters.and(filter, Filters.in(new LinkedList<PhysicalCard>(game.getGameState().getDiscard(playerId))));
        _twilightModifier = twilightModifier;
    }

    @Override
    public String getText(DefaultGame game) {
        return "Play card from discard";
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return getPlayableInDiscard(game).size() > 0;
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    private Collection<PhysicalCard> getPlayableInDiscard(DefaultGame game) {
        return Filters.filter(game.getGameState().getDiscard(_playerId), game, _filter, Filters.playable(game, _twilightModifier));
    }

    @Override
    public void playEffect(final DefaultGame game) {
        if (game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_PLAY_FROM_DISCARD_OR_DECK))
            return;
        Collection<PhysicalCard> discard = getPlayableInDiscard(game);
        if (discard.size() > 0) {
            game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new ArbitraryCardsSelectionDecision(1, "Choose a card to play", new LinkedList<>(discard), 1, 1) {
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
