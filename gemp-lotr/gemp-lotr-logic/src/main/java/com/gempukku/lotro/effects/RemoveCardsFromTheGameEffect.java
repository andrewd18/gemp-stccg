package com.gempukku.lotro.effects;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.rules.GameUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class RemoveCardsFromTheGameEffect extends AbstractEffect {
    private final String _playerPerforming;
    private final LotroPhysicalCard _source;
    private final Collection<? extends LotroPhysicalCard> _cardsToRemove;

    public RemoveCardsFromTheGameEffect(String playerPerforming, LotroPhysicalCard source, Collection<? extends LotroPhysicalCard> cardsToRemove) {
        _playerPerforming = playerPerforming;
        _source = source;
        _cardsToRemove = cardsToRemove;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        for (LotroPhysicalCard physicalCard : _cardsToRemove) {
            if (!physicalCard.getZone().isInPlay())
                return false;
        }

        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        Set<LotroPhysicalCard> removedCards = new HashSet<>();
        for (LotroPhysicalCard physicalCard : _cardsToRemove)
            if (physicalCard.getZone().isInPlay())
                removedCards.add(physicalCard);

        Set<LotroPhysicalCard> discardedCards = new HashSet<>();

        Set<LotroPhysicalCard> toMoveFromZoneToDiscard = new HashSet<>();

        DiscardUtils.cardsToChangeZones(game, removedCards, discardedCards, toMoveFromZoneToDiscard);

        Set<LotroPhysicalCard> toRemoveFromZone = new HashSet<>();
        toRemoveFromZone.addAll(removedCards);
        toRemoveFromZone.addAll(toMoveFromZoneToDiscard);

        game.getGameState().removeCardsFromZone(_playerPerforming, toRemoveFromZone);
        for (LotroPhysicalCard removedCard : removedCards)
            game.getGameState().addCardToZone(game, removedCard, Zone.REMOVED);
        for (LotroPhysicalCard card : toMoveFromZoneToDiscard)
            game.getGameState().addCardToZone(game, card, Zone.DISCARD);

        game.getGameState().sendMessage(_playerPerforming + " removed " + GameUtils.getAppendedNames(removedCards) + " from the game using " + GameUtils.getCardLink(_source));

        return new FullEffectResult(_cardsToRemove.size() == removedCards.size());
    }
}
