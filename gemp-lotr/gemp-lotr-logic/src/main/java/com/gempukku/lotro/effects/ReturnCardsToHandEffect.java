package com.gempukku.lotro.effects;

import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.gamestate.GameState;
import com.gempukku.lotro.rules.GameUtils;
import com.gempukku.lotro.effects.results.DiscardCardsFromPlayResult;
import com.gempukku.lotro.effects.results.ReturnCardsToHandResult;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ReturnCardsToHandEffect extends AbstractEffect {
    private final LotroPhysicalCard _source;
    private final Filterable _filter;

    public ReturnCardsToHandEffect(LotroPhysicalCard source, Filterable filter) {
        _source = source;
        _filter = filter;
    }

    @Override
    public String getText(DefaultGame game) {
        Collection<LotroPhysicalCard> cards = Filters.filterActive(game, _filter);
        return "Return " + getAppendedNames(cards) + " to hand";
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return Filters.filterActive(game, _filter,
                (Filter) (game1, physicalCard) -> (_source == null || game1.getModifiersQuerying().canBeReturnedToHand(game1, physicalCard, _source))).size() > 0;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        GameState gameState = game.getGameState();
        Collection<LotroPhysicalCard> cardsToReturnToHand = Filters.filterActive(game, _filter);

        // Preparation, figure out, what's going where...
        Set<LotroPhysicalCard> discardedFromPlay = new HashSet<>();
        Set<LotroPhysicalCard> toGoToDiscardCards = new HashSet<>();

        DiscardUtils.cardsToChangeZones(game, cardsToReturnToHand, discardedFromPlay, toGoToDiscardCards);

        Set<LotroPhysicalCard> cardsToRemoveFromZones = new HashSet<>(toGoToDiscardCards);
        cardsToRemoveFromZones.addAll(cardsToReturnToHand);

        // Remove from their zone
        gameState.removeCardsFromZone(_source.getOwner(), cardsToRemoveFromZones);

        // Add cards to hand
        for (LotroPhysicalCard card : cardsToReturnToHand)
            gameState.addCardToZone(game, card, Zone.HAND);

        // Add discarded to discard
        for (LotroPhysicalCard card : toGoToDiscardCards)
            gameState.addCardToZone(game, card, Zone.DISCARD);

        if (cardsToReturnToHand.size() > 0)
            gameState.sendMessage(GameUtils.getCardLink(_source) + " returns " + getAppendedNames(cardsToReturnToHand) + " to hand");

        for (LotroPhysicalCard discardedCard : discardedFromPlay)
            game.getActionsEnvironment().emitEffectResult(new DiscardCardsFromPlayResult(null, null, discardedCard));
        for (LotroPhysicalCard cardReturned : cardsToReturnToHand)
            game.getActionsEnvironment().emitEffectResult(new ReturnCardsToHandResult(cardReturned));

        return new FullEffectResult(true);
    }
}
