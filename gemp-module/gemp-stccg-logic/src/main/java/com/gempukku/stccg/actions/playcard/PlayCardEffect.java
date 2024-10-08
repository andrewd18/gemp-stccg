package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collections;

public class PlayCardEffect extends DefaultEffect {
    private final Zone _playedFrom;
    private final PhysicalCard _cardPlayed;
    private PhysicalCard _attachedToCard;
    private final Zone _zone;
    private final PhysicalCard _attachedOrStackedPlayedFrom;
    protected final DefaultGame _game;
    protected final String _performingPlayerId;

    public PlayCardEffect(String performingPlayerId, Zone playedFrom, PhysicalCard cardPlayed, Zone playedTo) {
        super(performingPlayerId);
        _playedFrom = playedFrom;
        _cardPlayed = cardPlayed;
        _zone = playedTo;
        _attachedOrStackedPlayedFrom = null;
        _game = cardPlayed.getGame();
        _performingPlayerId = performingPlayerId;
    }

    public PlayCardEffect(String performingPlayerId, Zone playedFrom, PhysicalCard cardPlayed, Zone playedTo,
                          PhysicalCard attachedOrStackedPlayedFrom) {
        super(performingPlayerId);
        _playedFrom = playedFrom;
        _cardPlayed = cardPlayed;
        _zone = playedTo;
        _game = cardPlayed.getGame();
        _performingPlayerId = performingPlayerId;
        _attachedOrStackedPlayedFrom = attachedOrStackedPlayedFrom;
    }

    public PlayCardEffect(String performingPlayerId, Zone playedFrom, PhysicalCard cardPlayed,
                          PhysicalCard attachedToCard, PhysicalCard attachedOrStackedPlayedFrom) {
        super(performingPlayerId);
        _playedFrom = playedFrom;
        _cardPlayed = cardPlayed;
        _attachedToCard = attachedToCard;
        _attachedOrStackedPlayedFrom = attachedOrStackedPlayedFrom;
        _zone = Zone.ATTACHED;
        _game = cardPlayed.getGame();
        _performingPlayerId = performingPlayerId;
    }

    public PhysicalCard getPlayedCard() {
        return _cardPlayed;
    }

    public PhysicalCard getAttachedTo() {
        return _attachedToCard;
    }

    @Override
    public String getText() {
        return "Play " + _cardPlayed.getFullName();
    }

    @Override
    public boolean isPlayableInFull() {
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        _game.getGameState().removeCardsFromZone(_cardPlayed.getOwnerName(), Collections.singleton(_cardPlayed));
        if (_attachedToCard != null) {
            _game.getGameState().attachCard(_cardPlayed, _attachedToCard);
        } else {
            _game.getGameState().addCardToZone(_cardPlayed, _zone);
        }

        _game.getActionsEnvironment().emitEffectResult(
                new PlayCardResult(this, _playedFrom, _cardPlayed,
                        _attachedToCard, _attachedOrStackedPlayedFrom));

        return new FullEffectResult(true);
    }
}
