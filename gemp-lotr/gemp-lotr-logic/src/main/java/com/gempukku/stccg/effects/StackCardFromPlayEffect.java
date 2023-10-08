package com.gempukku.stccg.effects;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.rules.GameUtils;
import com.gempukku.stccg.results.DiscardCardsFromPlayResult;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class StackCardFromPlayEffect extends AbstractEffect {
    private final PhysicalCard _card;
    private final PhysicalCard _stackOn;

    public StackCardFromPlayEffect(PhysicalCard card, PhysicalCard stackOn) {
        _card = card;
        _stackOn = stackOn;
    }

    @Override
    public String getText(DefaultGame game) {
        return "Stack " + GameUtils.getFullName(_card) + " on " + GameUtils.getFullName(_stackOn);
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return _card.getZone().isInPlay() && _stackOn.getZone().isInPlay();
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        if (isPlayableInFull(game)) {
            GameState gameState = game.getGameState();

            Set<PhysicalCard> discardedFromPlayCards = new HashSet<>();
            Set<PhysicalCard> toMoveToDiscardCards = new HashSet<>();

            DiscardUtils.cardsToChangeZones(game, Collections.singleton(_card), discardedFromPlayCards, toMoveToDiscardCards);

            Set<PhysicalCard> removeFromPlay = new HashSet<>(toMoveToDiscardCards);
            removeFromPlay.add(_card);

            gameState.removeCardsFromZone(_card.getOwner(), removeFromPlay);

            // And put them in new zones (attached and stacked to discard, the card gets stacked on)
            for (PhysicalCard attachedCard : toMoveToDiscardCards)
                gameState.addCardToZone(game, attachedCard, Zone.DISCARD);

            game.getGameState().sendMessage(GameUtils.getCardLink(_card) + " is stacked on " + GameUtils.getCardLink(_stackOn));
            game.getGameState().stackCard(game, _card, _stackOn);

            // Send the result (attached cards get discarded)
            for (PhysicalCard discardedCard : discardedFromPlayCards)
                game.getActionsEnvironment().emitEffectResult(new DiscardCardsFromPlayResult(null, null, discardedCard));

            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
