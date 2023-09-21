package com.gempukku.lotro.effects;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.rules.GameUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class RemoveCardsFromDeadPileEffect extends AbstractEffect {
    private final String _playerPerforming;
    private final LotroPhysicalCard _source;
    private final Collection<LotroPhysicalCard> _cardsToRemove;

    public RemoveCardsFromDeadPileEffect(String playerPerforming, LotroPhysicalCard source, Collection<LotroPhysicalCard> cardsToRemove) {
        _playerPerforming = playerPerforming;
        _source = source;
        _cardsToRemove = cardsToRemove;
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        for (LotroPhysicalCard physicalCard : _cardsToRemove) {
            if (physicalCard.getZone() != Zone.DEAD)
                return false;
        }

        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        Set<LotroPhysicalCard> removedCards = new HashSet<>();
        for (LotroPhysicalCard physicalCard : _cardsToRemove)
            if (physicalCard.getZone() == Zone.DEAD)
                removedCards.add(physicalCard);

        game.getGameState().removeCardsFromZone(_playerPerforming, removedCards);
        for (LotroPhysicalCard removedCard : removedCards)
            game.getGameState().addCardToZone(game, removedCard, Zone.REMOVED);

        game.getGameState().sendMessage(_playerPerforming + " removed " + GameUtils.getAppendedNames(removedCards) + " from dead pile using " + GameUtils.getCardLink(_source));

        return new FullEffectResult(_cardsToRemove.size() == removedCards.size());
    }
}
