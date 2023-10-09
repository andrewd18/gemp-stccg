package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.TribblesPlayCardEffect;
import com.gempukku.stccg.rules.GameUtils;

import java.util.Collections;

public class TribblesPlayPermanentAction extends AbstractCostToEffectAction {
    private final PhysicalCard _permanentPlayed;
    private boolean _cardRemoved;
    private Effect _playCardEffect;
    private boolean _cardPlayed;
    private final Zone _fromZone;
    private final Zone _toZone;

    public TribblesPlayPermanentAction(PhysicalCard card, Zone zone) {
        _permanentPlayed = card;
        setText("Play " + GameUtils.getFullName(_permanentPlayed));
        setPerformingPlayer(card.getOwner());

        _fromZone = card.getZone();
        _toZone = zone;
    }

    @Override
    public ActionType getActionType() {
        return ActionType.PLAY_CARD;
    }

    @Override
    public PhysicalCard getActionSource() {
        return _permanentPlayed;
    }

    @Override
    public PhysicalCard getActionAttachedToCard() {
        return _permanentPlayed;
    }

    @Override
    public Effect nextEffect(DefaultGame game) {
        if (!_cardRemoved) {
            _cardRemoved = true;
            final Zone playedFromZone = _permanentPlayed.getZone();
            game.getGameState().sendMessage(_permanentPlayed.getOwner() + " plays " +
                    GameUtils.getCardLink(_permanentPlayed) +  " from " + playedFromZone.getHumanReadable() +
                    " to " + _toZone.getHumanReadable());
            game.getGameState().removeCardsFromZone(_permanentPlayed.getOwner(),
                    Collections.singleton(_permanentPlayed));
            if (playedFromZone == Zone.HAND)
                game.getGameState().addCardToZone(game, _permanentPlayed, Zone.VOID_FROM_HAND);
            else
                game.getGameState().addCardToZone(game, _permanentPlayed, Zone.VOID);
            if (playedFromZone == Zone.DRAW_DECK) {
                game.getGameState().sendMessage(_permanentPlayed.getOwner() + " shuffles their deck");
                game.getGameState().shuffleDeck(_permanentPlayed.getOwner());
            }
        }

        if (!_cardPlayed) {
            _cardPlayed = true;
            _playCardEffect = new TribblesPlayCardEffect(_fromZone, _permanentPlayed, _toZone);
            return _playCardEffect;
        }

        return getNextEffect();
    }

    public boolean wasCarriedOut() {
        return _cardPlayed && _playCardEffect != null && _playCardEffect.wasCarriedOut();
    }
}
