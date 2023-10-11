package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.effects.PreventableCardEffect;
import com.gempukku.stccg.effects.utils.DiscardUtils;
import com.gempukku.stccg.effects.utils.EffectType;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.results.DiscardCardsFromPlayResult;
import com.gempukku.stccg.rules.GameUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DiscardCardsFromPlayEffect extends DefaultEffect implements PreventableCardEffect {
    private final PhysicalCard _source;
    private final Filter _filter;
    private final Set<PhysicalCard> _preventedTargets = new HashSet<>();
    private final String _performingPlayer;
    private int _requiredTargets;
    private final DefaultGame _game;

    public DiscardCardsFromPlayEffect(DefaultGame game, String performingPlayer, PhysicalCard source, Filterable... filters) {
        _game = game;
        _filter = Filters.and(filters);
        _performingPlayer = performingPlayer;
        _source = source;
    }

    public DiscardCardsFromPlayEffect(ActionContext actionContext, String performingPlayer, Filterable... filters) {
        _filter = Filters.and(filters);
        _performingPlayer = performingPlayer;
        _source = actionContext.getSource();
        _game = actionContext.getGame();
    }

    public PhysicalCard getSource() {
        return _source;
    }

    public void preventEffect(DefaultGame game, PhysicalCard card) {
        _preventedTargets.add(card);
        _prevented = true;
    }
    protected Filter getExtraAffectableFilter() {
        if (_source == null)
            return Filters.any;
        return Filters.canBeDiscarded(_performingPlayer, _source);
    }

    public String getPerformingPlayer() {
        return _performingPlayer;
    }

    @Override
    public EffectType getType() {
        return EffectType.BEFORE_DISCARD_FROM_PLAY;
    }


    @Override
    public String getText() {
        Collection<PhysicalCard> cards = getAffectedCardsMinusPrevented(_game);
        return "Discard " + GameUtils.getAppendedTextNames(cards);
    }

    protected void forEachDiscardedByEffectCallback(Collection<PhysicalCard> discardedCards) {

    }

    @Override
    public FullEffectResult playEffectReturningResult() {
        Collection<PhysicalCard> affectedMinusPreventedCards = getAffectedCardsMinusPrevented(_game);
        playOutEffectOn(_game, affectedMinusPreventedCards);
        return new FullEffectResult(affectedMinusPreventedCards.size() >= _requiredTargets);
    }

    @Override
    public boolean isPlayableInFull() {
        return getAffectedCardsMinusPrevented(_game).size() >= _requiredTargets;
    }

    protected final Collection<PhysicalCard> getAffectedCards(DefaultGame game) {
        return Filters.filterActive(game, _filter, getExtraAffectableFilter());
    }

    public final Collection<PhysicalCard> getAffectedCardsMinusPrevented(DefaultGame game) {
        Collection<PhysicalCard> affectedCards = getAffectedCards(game);
        affectedCards.removeAll(_preventedTargets);
        return affectedCards;
    }

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
            game.getGameState().sendMessage(_performingPlayer + " discards " + GameUtils.getAppendedNames(cards) + " from play using " + GameUtils.getCardLink(_source));

        for (PhysicalCard discardedCard : discardedCards)
            game.getActionsEnvironment().emitEffectResult(new DiscardCardsFromPlayResult(_source, getPerformingPlayer(), discardedCard));

        forEachDiscardedByEffectCallback(cards);
    }
}
