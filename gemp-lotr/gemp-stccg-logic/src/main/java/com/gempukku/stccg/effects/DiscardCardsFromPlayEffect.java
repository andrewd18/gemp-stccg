package com.gempukku.stccg.effects;

import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.rules.GameUtils;
import com.gempukku.stccg.results.DiscardCardsFromPlayResult;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DiscardCardsFromPlayEffect extends AbstractPreventableCardEffect {
    private final PhysicalCard _source;
    private final String _performingPlayer;

    public DiscardCardsFromPlayEffect(String performingPlayer, PhysicalCard source, Filterable... filters) {
        super(filters);
        _performingPlayer = performingPlayer;
        _source = source;
    }

    public PhysicalCard getSource() {
        return _source;
    }

    @Override
    protected Filter getExtraAffectableFilter() {
        if (_source == null)
            return Filters.any;
        return Filters.canBeDiscarded(_performingPlayer, _source);
    }

    public String getPerformingPlayer() {
        return _performingPlayer;
    }

    @Override
    public Effect.Type getType() {
        return Type.BEFORE_DISCARD_FROM_PLAY;
    }


    @Override
    public String getText(DefaultGame game) {
        Collection<PhysicalCard> cards = getAffectedCardsMinusPrevented(game);
        return "Discard " + getAppendedTextNames(cards);
    }

    protected void forEachDiscardedByEffectCallback(Collection<PhysicalCard> discardedCards) {

    }

    @Override
    protected void playOutEffectOn(DefaultGame game, Collection<PhysicalCard> cards) {
        Set<PhysicalCard> discardedCards = new HashSet<>();

        Set<PhysicalCard> toMoveFromZoneToDiscard = new HashSet<>();

        GameState gameState = game.getGameState();

        DiscardUtils.cardsToChangeZones(game, cards, discardedCards, toMoveFromZoneToDiscard);

        discardedCards.addAll(cards);
        toMoveFromZoneToDiscard.addAll(cards);

        gameState.removeCardsFromZone(_performingPlayer, toMoveFromZoneToDiscard);

        for (PhysicalCard card : toMoveFromZoneToDiscard)
            gameState.addCardToZone(game, card, Zone.DISCARD);

        if (_source != null && discardedCards.size() > 0)
            game.getGameState().sendMessage(_performingPlayer + " discards " + getAppendedNames(discardedCards) + " from play using " + GameUtils.getCardLink(_source));

        for (PhysicalCard discardedCard : discardedCards)
            game.getActionsEnvironment().emitEffectResult(new DiscardCardsFromPlayResult(_source, getPerformingPlayer(), discardedCard));

        forEachDiscardedByEffectCallback(cards);
    }
}
