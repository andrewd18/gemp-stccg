package com.gempukku.lotro.game.state;

import com.gempukku.lotro.cards.*;
import com.gempukku.lotro.cards.LotroCardBlueprint;
import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.cards.lotronly.LotroPhysicalCardImpl;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.communication.GameStateListener;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.game.state.lotronly.Assignment;
import com.gempukku.lotro.game.timing.PlayerOrder;
import com.gempukku.lotro.game.decisions.AwaitingDecision;
import com.gempukku.lotro.game.modifiers.ModifierFlag;
import org.apache.log4j.Logger;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GameState {
    private static final Logger _log = Logger.getLogger(GameState.class);
    private static final int LAST_MESSAGE_STORED_COUNT = 15;
    private PlayerOrder _playerOrder;
    private LotroFormat _format;

    private final Map<String, List<LotroPhysicalCardImpl>> _adventureDecks = new HashMap<>();
    private final Map<String, List<LotroPhysicalCardImpl>> _decks = new HashMap<>();
    private final Map<String, List<LotroPhysicalCardImpl>> _hands = new HashMap<>();
    private final Map<String, List<LotroPhysicalCardImpl>> _discards = new HashMap<>();
    private final Map<String, List<LotroPhysicalCardImpl>> _deadPiles = new HashMap<>();
    private final Map<String, List<LotroPhysicalCardImpl>> _stacked = new HashMap<>();

    private final Map<String, List<LotroPhysicalCardImpl>> _voids = new HashMap<>();
    private final Map<String, List<LotroPhysicalCardImpl>> _voidsFromHand = new HashMap<>();
    private final Map<String, List<LotroPhysicalCardImpl>> _removed = new HashMap<>();

    private final List<LotroPhysicalCardImpl> _inPlay = new LinkedList<>();

    private final Map<Integer, LotroPhysicalCardImpl> _allCards = new HashMap<>();

    private String _currentPlayerId;
    private Phase _currentPhase = Phase.PUT_RING_BEARER;
    private int _twilightPool;

    private int _moveCount;
    private boolean _moving;
    private boolean _fierceSkirmishes;
    private boolean _extraSkirmishes;

    private boolean _wearingRing;
    private boolean _consecutiveAction;

    private final Map<String, Integer> _playerPosition = new HashMap<>();
    private final Map<String, Integer> _playerThreats = new HashMap<>();

    private final Map<LotroPhysicalCard, Map<Token, Integer>> _cardTokens = new HashMap<>();

    private final Map<String, LotroPhysicalCard> _ringBearers = new HashMap<>();
    private final Map<String, LotroPhysicalCard> _rings = new HashMap<>();

    private final Map<String, AwaitingDecision> _playerDecisions = new HashMap<>();

    private final List<Assignment> _assignments = new LinkedList<>();
    private Skirmish _skirmish = null;

    private final Set<GameStateListener> _gameStateListeners = new HashSet<>();
    private final LinkedList<String> _lastMessages = new LinkedList<>();

    private int _nextCardId = 0;

    private int nextCardId() {
        return _nextCardId++;
    }

    public void init(PlayerOrder playerOrder, String firstPlayer, Map<String, List<String>> cards, Map<String, String> ringBearers, Map<String, String> rings, CardBlueprintLibrary library, LotroFormat format) {
        _playerOrder = playerOrder;
        _currentPlayerId = firstPlayer;
        _format = format;

        for (Map.Entry<String, List<String>> stringListEntry : cards.entrySet()) {
            String playerId = stringListEntry.getKey();
            List<String> decks = stringListEntry.getValue();

            _adventureDecks.put(playerId, new LinkedList<>());
            _decks.put(playerId, new LinkedList<>());
            _hands.put(playerId, new LinkedList<>());
            _voids.put(playerId, new LinkedList<>());
            _voidsFromHand.put(playerId, new LinkedList<>());
            _removed.put(playerId, new LinkedList<>());
            _discards.put(playerId, new LinkedList<>());
            _deadPiles.put(playerId, new LinkedList<>());
            _stacked.put(playerId, new LinkedList<>());

            addPlayerCards(playerId, decks, library);
            try {
                _ringBearers.put(playerId, createPhysicalCardImpl(playerId, library, ringBearers.get(playerId)));
                String ringBlueprintId = rings.get(playerId);
                if (ringBlueprintId != null)
                    _rings.put(playerId, createPhysicalCardImpl(playerId, library, ringBlueprintId));
            } catch (CardNotFoundException exp) {
                throw new RuntimeException("Unable to create game, due to either ring-bearer or ring being invalid cards");
            }
        }

        for (String playerId : playerOrder.getAllPlayers()) {
            _playerThreats.put(playerId, 0);
        }

        for (GameStateListener listener : getAllGameStateListeners()) {
            listener.initializeBoard(playerOrder.getAllPlayers(), format.discardPileIsPublic());
        }

        //This needs done after the Player Order initialization has been issued, or else the player
        // adventure deck areas don't exist.
        for (String playerId : playerOrder.getAllPlayers()) {
            for(var site : getAdventureDeck(playerId)) {
                for (GameStateListener listener : getAllGameStateListeners()) {
                    listener.cardCreated(site);
                }
            }
        }
    }

    public void finish() {
        for (GameStateListener listener : getAllGameStateListeners()) {
            listener.endGame();
        }

        if(_playerOrder == null || _playerOrder.getAllPlayers() == null)
            return;

        for (String playerId : _playerOrder.getAllPlayers()) {
            for(var card : getDeck(playerId)) {
                for (GameStateListener listener : getAllGameStateListeners()) {
                    listener.cardCreated(card, true);
                }
            }
        }
    }

    public boolean isMoving() {
        return _moving;
    }

    public void setMoving(boolean moving) {
        _moving = moving;
    }

    private void addPlayerCards(String playerId, List<String> cards, CardBlueprintLibrary library) {
        for (String blueprintId : cards) {
            try {
                LotroPhysicalCardImpl physicalCard = createPhysicalCardImpl(playerId, library, blueprintId);
                if (physicalCard.getBlueprint().getCardType() == CardType.SITE) {
                    physicalCard.setZone(Zone.ADVENTURE_DECK);
                    _adventureDecks.get(playerId).add(physicalCard);
                } else {
                    physicalCard.setZone(Zone.DECK);
                    _decks.get(playerId).add(physicalCard);
                }
            } catch (CardNotFoundException exp) {
                // Ignore the card
            }
        }
    }

    public LotroPhysicalCard createPhysicalCard(String ownerPlayerId, CardBlueprintLibrary library, String blueprintId) throws CardNotFoundException {
        return createPhysicalCardImpl(ownerPlayerId, library, blueprintId);
    }

    private LotroPhysicalCardImpl createPhysicalCardImpl(String playerId, CardBlueprintLibrary library, String blueprintId) throws CardNotFoundException {
        LotroCardBlueprint card = library.getLotroCardBlueprint(blueprintId);

        int cardId = nextCardId();
        LotroPhysicalCardImpl result = new LotroPhysicalCardImpl(cardId, blueprintId, playerId, card);

        _allCards.put(cardId, result);

        return result;
    }

    public boolean isConsecutiveAction() {
        return _consecutiveAction;
    }

    public void setConsecutiveAction(boolean consecutiveAction) {
        _consecutiveAction = consecutiveAction;
    }

    public void setWearingRing(boolean wearingRing) {
        _wearingRing = wearingRing;
    }

    public boolean isWearingRing() {
        return _wearingRing;
    }

    public PlayerOrder getPlayerOrder() {
        return _playerOrder;
    }

    public void addGameStateListener(String playerId, GameStateListener gameStateListener, GameStats gameStats) {
        _gameStateListeners.add(gameStateListener);
        sendStateToPlayer(playerId, gameStateListener, gameStats);
    }

    public void removeGameStateListener(GameStateListener gameStateListener) {
        _gameStateListeners.remove(gameStateListener);
    }

    private Collection<GameStateListener> getAllGameStateListeners() {
        return Collections.unmodifiableSet(_gameStateListeners);
    }

    private String getPhaseString() {
        if (isFierceSkirmishes())
            return "Fierce " + _currentPhase.getHumanReadable();
        if (isExtraSkirmishes())
            return "Extra " + _currentPhase.getHumanReadable();
        return _currentPhase.getHumanReadable();
    }

    private void sendStateToPlayer(String playerId, GameStateListener listener, GameStats gameStats) {
        if (_playerOrder != null) {
            listener.initializeBoard(_playerOrder.getAllPlayers(), _format.discardPileIsPublic());
            if (_currentPlayerId != null)
                listener.setCurrentPlayerId(_currentPlayerId);
            if (_currentPhase != null)
                listener.setCurrentPhase(getPhaseString());
            listener.setTwilight(_twilightPool);
            for (Map.Entry<String, Integer> stringIntegerEntry : _playerPosition.entrySet())
                listener.setPlayerPosition(stringIntegerEntry.getKey(), stringIntegerEntry.getValue());

            Set<LotroPhysicalCard> cardsLeftToSent = new LinkedHashSet<>(_inPlay);
            Set<LotroPhysicalCard> sentCardsFromPlay = new HashSet<>();

            int cardsToSendAtLoopStart;
            do {
                cardsToSendAtLoopStart = cardsLeftToSent.size();
                Iterator<LotroPhysicalCard> cardIterator = cardsLeftToSent.iterator();
                while (cardIterator.hasNext()) {
                    LotroPhysicalCard physicalCard = cardIterator.next();
                    LotroPhysicalCard attachedTo = physicalCard.getAttachedTo();
                    if (attachedTo == null || sentCardsFromPlay.contains(attachedTo)) {
                        listener.cardCreated(physicalCard);
                        sentCardsFromPlay.add(physicalCard);

                        cardIterator.remove();
                    }
                }
            } while (cardsToSendAtLoopStart != cardsLeftToSent.size() && cardsLeftToSent.size() > 0);

            // Finally the stacked ones
            for (List<LotroPhysicalCardImpl> physicalCards : _stacked.values())
                for (LotroPhysicalCardImpl physicalCard : physicalCards)
                    listener.cardCreated(physicalCard);

            List<LotroPhysicalCardImpl> hand = _hands.get(playerId);
            if (hand != null) {
                for (LotroPhysicalCardImpl physicalCard : hand)
                    listener.cardCreated(physicalCard);
            }

            for (List<LotroPhysicalCardImpl> physicalCards : _deadPiles.values())
                for (LotroPhysicalCardImpl physicalCard : physicalCards)
                    listener.cardCreated(physicalCard);

            List<LotroPhysicalCardImpl> discard = _discards.get(playerId);
            if (discard != null) {
                for (LotroPhysicalCardImpl physicalCard : discard)
                    listener.cardCreated(physicalCard);
            }

            List<LotroPhysicalCardImpl> adventureDeck = _adventureDecks.get(playerId);
            if (adventureDeck != null) {
                for (LotroPhysicalCardImpl physicalCard : adventureDeck)
                    listener.cardCreated(physicalCard);
            }

            for (Assignment assignment : _assignments)
                listener.addAssignment(assignment.getFellowshipCharacter(), assignment.getShadowCharacters());

            if (_skirmish != null)
                listener.startSkirmish(_skirmish.getFellowshipCharacter(), _skirmish.getShadowCharacters());

            for (Map.Entry<LotroPhysicalCard, Map<Token, Integer>> physicalCardMapEntry : _cardTokens.entrySet()) {
                LotroPhysicalCard card = physicalCardMapEntry.getKey();
                for (Map.Entry<Token, Integer> tokenIntegerEntry : physicalCardMapEntry.getValue().entrySet()) {
                    Integer count = tokenIntegerEntry.getValue();
                    if (count != null && count > 0)
                        listener.addTokens(card, tokenIntegerEntry.getKey(), count);
                }
            }

            listener.sendGameStats(gameStats);
        }

        for (String lastMessage : _lastMessages)
            listener.sendMessage(lastMessage);

        final AwaitingDecision awaitingDecision = _playerDecisions.get(playerId);
        if (awaitingDecision != null)
            listener.decisionRequired(playerId, awaitingDecision);
    }

    public void sendMessage(String message) {
        _lastMessages.add(message);
        if (_lastMessages.size() > LAST_MESSAGE_STORED_COUNT)
            _lastMessages.removeFirst();
        for (GameStateListener listener : getAllGameStateListeners())
            listener.sendMessage(message);
    }

    public void playerDecisionStarted(String playerId, AwaitingDecision awaitingDecision) {
        _playerDecisions.put(playerId, awaitingDecision);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.decisionRequired(playerId, awaitingDecision);
    }

    public void playerDecisionFinished(String playerId) {
        _playerDecisions.remove(playerId);
    }

    public void transferCard(LotroPhysicalCard card, LotroPhysicalCard transferTo) {
        if (card.getZone() != Zone.ATTACHED)
            ((LotroPhysicalCardImpl) card).setZone(Zone.ATTACHED);

        ((LotroPhysicalCardImpl) card).attachTo((LotroPhysicalCardImpl) transferTo);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.cardMoved(card);
    }

    public void takeControlOfCard(String playerId, DefaultGame game, LotroPhysicalCard card, Zone zone) {
        ((LotroPhysicalCardImpl) card).setCardController(playerId);
        ((LotroPhysicalCardImpl) card).setZone(zone);
        if (card.getBlueprint().getCardType() == CardType.SITE)
            startAffectingControlledSite(game, card);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.cardMoved(card);
    }

    public void loseControlOfCard(LotroPhysicalCard card, Zone zone) {
        if (card.getBlueprint().getCardType() == CardType.SITE)
            stopAffectingControlledSite(card);
        ((LotroPhysicalCardImpl) card).setCardController(null);
        ((LotroPhysicalCardImpl) card).setZone(zone);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.cardMoved(card);
    }

    public void attachCard(DefaultGame game, LotroPhysicalCard card, LotroPhysicalCard attachTo) throws InvalidParameterException {
        if(card == attachTo)
            throw new InvalidParameterException("Cannot attach card to itself!");

        ((LotroPhysicalCardImpl) card).attachTo((LotroPhysicalCardImpl) attachTo);
        addCardToZone(game, card, Zone.ATTACHED);
    }

    public void stackCard(DefaultGame game, LotroPhysicalCard card, LotroPhysicalCard stackOn) throws InvalidParameterException {
        if(card == stackOn)
            throw new InvalidParameterException("Cannot stack card on itself!");

        ((LotroPhysicalCardImpl) card).stackOn((LotroPhysicalCardImpl) stackOn);
        addCardToZone(game, card, Zone.STACKED);
    }

    public void cardAffectsCard(String playerPerforming, LotroPhysicalCard card, Collection<LotroPhysicalCard> affectedCards) {
        for (GameStateListener listener : getAllGameStateListeners())
            listener.cardAffectedByCard(playerPerforming, card, affectedCards);
    }

    public void eventPlayed(LotroPhysicalCard card) {
        for (GameStateListener listener : getAllGameStateListeners())
            listener.eventPlayed(card);
    }

    public void activatedCard(String playerPerforming, LotroPhysicalCard card) {
        for (GameStateListener listener : getAllGameStateListeners())
            listener.cardActivated(playerPerforming, card);
    }

    public void setRingBearer(LotroPhysicalCard card) {
        _ringBearers.put(card.getOwner(), card);
    }

    public LotroPhysicalCard getRingBearer(String playerId) {
        return _ringBearers.get(playerId);
    }

    public LotroPhysicalCard getRing(String playerId) {
        return _rings.get(playerId);
    }

    private List<LotroPhysicalCardImpl> getZoneCards(String playerId, Zone zone) {
        if (zone == Zone.DECK)
            return _decks.get(playerId);
        else if (zone == Zone.ADVENTURE_DECK)
            return _adventureDecks.get(playerId);
        else if (zone == Zone.DISCARD)
            return _discards.get(playerId);
        else if (zone == Zone.DEAD)
            return _deadPiles.get(playerId);
        else if (zone == Zone.HAND)
            return _hands.get(playerId);
        else if (zone == Zone.VOID)
            return _voids.get(playerId);
        else if (zone == Zone.VOID_FROM_HAND)
            return _voidsFromHand.get(playerId);
        else if (zone == Zone.REMOVED)
            return _removed.get(playerId);
        else if (zone == Zone.STACKED)
            return _stacked.get(playerId);
        else
            return _inPlay;
    }

    public void removeFromSkirmish(LotroPhysicalCard card) {
        removeFromSkirmish(card, true);
    }

    public void replaceInSkirmish(LotroPhysicalCard card) {
        _skirmish.setFellowshipCharacter(card);
        for (GameStateListener gameStateListener : getAllGameStateListeners()) {
            gameStateListener.finishSkirmish();
            gameStateListener.startSkirmish(_skirmish.getFellowshipCharacter(), _skirmish.getShadowCharacters());
        }
    }

    public void replaceInSkirmishMinion(LotroPhysicalCard card, LotroPhysicalCard removeMinion) {
        removeFromSkirmish(removeMinion);
        _skirmish.getShadowCharacters().add(card);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.addToSkirmish(card);
    }

    private void removeFromSkirmish(LotroPhysicalCard card, boolean notify) {
        if (_skirmish != null) {
            if (_skirmish.getFellowshipCharacter() == card) {
                _skirmish.setFellowshipCharacter(null);
                _skirmish.addRemovedFromSkirmish(card);
                if (notify)
                    for (GameStateListener listener : getAllGameStateListeners())
                        listener.removeFromSkirmish(card);
            }
            if (_skirmish.getShadowCharacters().remove(card)) {
                _skirmish.addRemovedFromSkirmish(card);
                if (notify)
                    for (GameStateListener listener : getAllGameStateListeners())
                        listener.removeFromSkirmish(card);
            }
        }
    }

    public void removeCardsFromZone(String playerPerforming, Collection<LotroPhysicalCard> cards) {
        for (LotroPhysicalCard card : cards) {
            List<LotroPhysicalCardImpl> zoneCards = getZoneCards(card.getOwner(), card.getZone());
            if (!zoneCards.contains(card))
                _log.error("Card was not found in the expected zone");
        }

        for (LotroPhysicalCard card : cards) {
            Zone zone = card.getZone();

            if (zone.isInPlay())
                if (card.getBlueprint().getCardType() != CardType.SITE ||
                        (getCurrentPhase() != Phase.PLAY_STARTING_FELLOWSHIP && getCurrentSite() == card))
                    stopAffecting(card);

            if (zone == Zone.STACKED)
                stopAffectingStacked(card);
            else if (zone == Zone.DISCARD)
                stopAffectingInDiscard(card);

            List<LotroPhysicalCardImpl> zoneCards = getZoneCards(card.getOwner(), zone);
            zoneCards.remove(card);

            if (zone == Zone.ATTACHED)
                ((LotroPhysicalCardImpl) card).attachTo(null);

            if (zone == Zone.STACKED)
                ((LotroPhysicalCardImpl) card).stackOn(null);

            for (Assignment assignment : new LinkedList<>(_assignments)) {
                if (assignment.getFellowshipCharacter() == card)
                    removeAssignment(assignment);
                if (assignment.getShadowCharacters().remove(card))
                    if (assignment.getShadowCharacters().size() == 0)
                        removeAssignment(assignment);
            }

            if (_skirmish != null)
                removeFromSkirmish(card, false);

            removeAllTokens(card);
            //If this is reset, then there is no way for self-discounting effects (which are evaluated while in the void)
            // to have any sort of permanent effect once the card is in play.
            if(zone != Zone.VOID_FROM_HAND && zone != Zone.VOID)
                card.setWhileInZoneData(null);
        }

        for (GameStateListener listener : getAllGameStateListeners())
            listener.cardsRemoved(playerPerforming, cards);

        for (LotroPhysicalCard card : cards) {
            ((LotroPhysicalCardImpl) card).setZone(null);
        }
    }

    public void addCardToZone(DefaultGame game, LotroPhysicalCard card, Zone zone) {
        addCardToZone(game, card, zone, true);
    }

    private void addCardToZone(DefaultGame game, LotroPhysicalCard card, Zone zone, boolean end) {
        if (zone == Zone.DISCARD && game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.REMOVE_CARDS_GOING_TO_DISCARD))
            zone = Zone.REMOVED;

        if (zone.isInPlay())
            assignNewCardId(card);

        List<LotroPhysicalCardImpl> zoneCards = getZoneCards(card.getOwner(), zone);
        if (end)
            zoneCards.add((LotroPhysicalCardImpl) card);
        else
            zoneCards.add(0, (LotroPhysicalCardImpl) card);

        if (card.getZone() != null)
            _log.error("Card was in " + card.getZone() + " when tried to add to zone: " + zone);

        ((LotroPhysicalCardImpl) card).setZone(zone);

        if (zone == Zone.ADVENTURE_PATH) {
            for (GameStateListener listener : getAllGameStateListeners())
                listener.setSite(card);
        } else {
            for (GameStateListener listener : getAllGameStateListeners())
                listener.cardCreated(card);
        }

        if (_currentPhase.isCardsAffectGame()) {
            if (zone.isInPlay())
                if (card.getBlueprint().getCardType() != CardType.SITE || (getCurrentPhase() != Phase.PLAY_STARTING_FELLOWSHIP && getCurrentSite() == card))
                    startAffecting(game, card);

            if (zone == Zone.STACKED)
                startAffectingStacked(game, card);
            else if (zone == Zone.DISCARD)
                startAffectingInDiscard(game, card);
        }
    }

    private void assignNewCardId(LotroPhysicalCard card) {
        _allCards.remove(card.getCardId());
        int newCardId = nextCardId();
        ((LotroPhysicalCardImpl) card).setCardId(newCardId);
        _allCards.put(newCardId, ((LotroPhysicalCardImpl) card));
    }

    private void removeAllTokens(LotroPhysicalCard card) {
        Map<Token, Integer> map = _cardTokens.get(card);
        if (map != null) {
            for (Map.Entry<Token, Integer> tokenIntegerEntry : map.entrySet())
                if (tokenIntegerEntry.getValue() > 0)
                    for (GameStateListener listener : getAllGameStateListeners())
                        listener.removeTokens(card, tokenIntegerEntry.getKey(), tokenIntegerEntry.getValue());

            map.clear();
        }
    }

    public void shuffleCardsIntoDeck(Collection<? extends LotroPhysicalCard> cards, String playerId) {
        List<LotroPhysicalCardImpl> zoneCards = _decks.get(playerId);

        for (LotroPhysicalCard card : cards) {
            zoneCards.add((LotroPhysicalCardImpl) card);

            ((LotroPhysicalCardImpl) card).setZone(Zone.DECK);
        }

        shuffleDeck(playerId);
    }

    public void putCardOnBottomOfDeck(LotroPhysicalCard card) {
        addCardToZone(null, card, Zone.DECK, true);
    }

    public void putCardOnTopOfDeck(LotroPhysicalCard card) {
        addCardToZone(null, card, Zone.DECK, false);
    }

    public boolean iterateActiveCards(PhysicalCardVisitor physicalCardVisitor) {
        for (LotroPhysicalCardImpl physicalCard : _inPlay) {
            if (isCardInPlayActive(physicalCard))
                if (physicalCardVisitor.visitPhysicalCard(physicalCard))
                    return true;
        }

        return false;
    }

    public LotroPhysicalCard findCardById(int cardId) {
        return _allCards.get(cardId);
    }

    public Iterable<? extends LotroPhysicalCard> getAllCards() {
        return Collections.unmodifiableCollection(_allCards.values());
    }

    public List<? extends LotroPhysicalCard> getHand(String playerId) {
        return Collections.unmodifiableList(_hands.get(playerId));
    }

    public List<? extends LotroPhysicalCard> getVoidFromHand(String playerId) {
        return Collections.unmodifiableList(_voidsFromHand.get(playerId));
    }

    public List<? extends LotroPhysicalCard> getRemoved(String playerId) {
        return Collections.unmodifiableList(_removed.get(playerId));
    }

    public List<? extends LotroPhysicalCard> getDeck(String playerId) {
        return Collections.unmodifiableList(_decks.get(playerId));
    }

    public List<? extends LotroPhysicalCard> getDiscard(String playerId) {
        return Collections.unmodifiableList(_discards.get(playerId));
    }

    public List<? extends LotroPhysicalCard> getDeadPile(String playerId) {
        return Collections.unmodifiableList(_deadPiles.get(playerId));
    }

    public List<? extends LotroPhysicalCard> getAdventureDeck(String playerId) {
        return Collections.unmodifiableList(_adventureDecks.get(playerId));
    }

    public List<? extends LotroPhysicalCard> getInPlay() {
        return Collections.unmodifiableList(_inPlay);
    }

    public List<? extends LotroPhysicalCard> getStacked(String playerId) {
        return Collections.unmodifiableList(_stacked.get(playerId));
    }

    public String getCurrentPlayerId() {
        return _currentPlayerId;
    }

    public int getCurrentSiteNumber() {
        return _playerPosition.getOrDefault(_currentPlayerId, 0);
    }

    public void setPlayerPosition(String playerId, int i) {
        _playerPosition.put(playerId, i);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.setPlayerPosition(playerId, i);
    }

    public void addThreats(String playerId, int count) {
        _playerThreats.put(playerId, _playerThreats.get(playerId) + count);
    }

    public void removeThreats(String playerId, int count) {
        final int oldThreats = _playerThreats.get(playerId);
        count = Math.min(count, oldThreats);
        _playerThreats.put(playerId, oldThreats - count);
    }

    public void movePlayerToNextSite(DefaultGame game) {
        final String currentPlayerId = getCurrentPlayerId();
        final int oldPlayerPosition = getPlayerPosition(currentPlayerId);
        stopAffecting(getCurrentSite());
        setPlayerPosition(currentPlayerId, oldPlayerPosition + 1);
        increaseMoveCount();
        startAffecting(game, getCurrentSite());
    }

    public int getPlayerPosition(String playerId) {
        return _playerPosition.getOrDefault(playerId, 0);
    }

    public Map<Token, Integer> getTokens(LotroPhysicalCard card) {
        Map<Token, Integer> map = _cardTokens.get(card);
        if (map == null)
            return Collections.emptyMap();
        return Collections.unmodifiableMap(map);
    }

    public int getTokenCount(LotroPhysicalCard physicalCard, Token token) {
        Map<Token, Integer> tokens = _cardTokens.get(physicalCard);
        if (tokens == null)
            return 0;
        Integer count = tokens.get(token);
        if (count == null)
            return 0;
        return count;
    }

    public List<LotroPhysicalCard> getAttachedCards(LotroPhysicalCard card) {
        List<LotroPhysicalCard> result = new LinkedList<>();
        for (LotroPhysicalCardImpl physicalCard : _inPlay) {
            if (physicalCard.getAttachedTo() != null && physicalCard.getAttachedTo() == card)
                result.add(physicalCard);
        }
        return result;
    }

    public List<LotroPhysicalCard> getStackedCards(LotroPhysicalCard card) {
        List<LotroPhysicalCard> result = new LinkedList<>();
        for (List<LotroPhysicalCardImpl> physicalCardList : _stacked.values()) {
            for (LotroPhysicalCardImpl physicalCard : physicalCardList) {
                if (physicalCard.getStackedOn() == card)
                    result.add(physicalCard);
            }
        }
        return result;
    }

    public int getWounds(LotroPhysicalCard physicalCard) {
        return getTokenCount(physicalCard, Token.WOUND);
    }

    public void addBurdens(int burdens) {
        addTokens(_ringBearers.get(getCurrentPlayerId()), Token.BURDEN, Math.max(0, burdens));
    }

    public int getBurdens() {
        return getTokenCount(_ringBearers.get(getCurrentPlayerId()), Token.BURDEN);
    }

    public int getPlayerThreats(String playerId) {
        return _playerThreats.get(playerId);
    }

    public int getThreats() {
        return _playerThreats.get(getCurrentPlayerId());
    }

    public void removeBurdens(int burdens) {
        removeTokens(_ringBearers.get(getCurrentPlayerId()), Token.BURDEN, Math.max(0, burdens));
    }

    public void addWound(LotroPhysicalCard card) {
        addTokens(card, Token.WOUND, 1);
    }

    public void removeWound(LotroPhysicalCard card) {
        removeTokens(card, Token.WOUND, 1);
    }

    public void addTokens(LotroPhysicalCard card, Token token, int count) {
        Map<Token, Integer> tokens = _cardTokens.get(card);
        if (tokens == null) {
            tokens = new HashMap<>();
            _cardTokens.put(card, tokens);
        }
        Integer currentCount = tokens.get(token);
        if (currentCount == null)
            tokens.put(token, count);
        else
            tokens.put(token, currentCount + count);

        for (GameStateListener listener : getAllGameStateListeners())
            listener.addTokens(card, token, count);
    }

    public void removeTokens(LotroPhysicalCard card, Token token, int count) {
        Map<Token, Integer> tokens = _cardTokens.get(card);
        if (tokens == null) {
            tokens = new HashMap<>();
            _cardTokens.put(card, tokens);
        }
        Integer currentCount = tokens.get(token);
        if (currentCount != null) {
            if (currentCount < count)
                count = currentCount;

            tokens.put(token, currentCount - count);

            for (GameStateListener listener : getAllGameStateListeners())
                listener.removeTokens(card, token, count);
        }
    }

    public void setTwilight(int twilight) {
        _twilightPool = twilight;
        for (GameStateListener listener : getAllGameStateListeners())
            listener.setTwilight(_twilightPool);
    }

    public int getTwilightPool() {
        return _twilightPool;
    }

    public void startPlayerTurn(String playerId) {
        _currentPlayerId = playerId;
        setTwilight(0);
        _moveCount = 0;
        _fierceSkirmishes = false;

        for (GameStateListener listener : getAllGameStateListeners())
            listener.setCurrentPlayerId(_currentPlayerId);
    }

    public boolean isExtraSkirmishes() {
        return _extraSkirmishes;
    }

    public void setExtraSkirmishes(boolean extraSkirmishes) {
        _extraSkirmishes = extraSkirmishes;
    }

    public void setFierceSkirmishes(boolean value) {
        _fierceSkirmishes = value;
    }

    public boolean isFierceSkirmishes() {
        return _fierceSkirmishes;
    }

    public boolean isNormalSkirmishes() {
        return !_fierceSkirmishes && !_extraSkirmishes;
    }

    public boolean isCardInPlayActive(LotroPhysicalCard card) {
        Side side = card.getBlueprint().getSide();
        // Either it's not attached or attached to active card
        // AND is a site or fp/ring of current player or shadow of any other player
        if (card.getBlueprint().getCardType() == CardType.SITE)
            return _currentPhase != Phase.PUT_RING_BEARER && _currentPhase != Phase.PLAY_STARTING_FELLOWSHIP;

        if (card.getBlueprint().getCardType() == CardType.THE_ONE_RING)
            return card.getOwner().equals(_currentPlayerId);

        if (card.getOwner().equals(_currentPlayerId) && side == Side.SHADOW)
            return false;

        if (!card.getOwner().equals(_currentPlayerId) && side == Side.FREE_PEOPLE)
            return false;

        if (card.getAttachedTo() != null)
            return isCardInPlayActive(card.getAttachedTo());

        return true;
    }

    public void startAffectingCardsForCurrentPlayer(DefaultGame game) {
        // Active non-sites are affecting
        for (LotroPhysicalCardImpl physicalCard : _inPlay) {
            if (isCardInPlayActive(physicalCard) && physicalCard.getBlueprint().getCardType() != CardType.SITE)
                startAffecting(game, physicalCard);
            else if (physicalCard.getBlueprint().getCardType() == CardType.SITE &&
                    physicalCard.getCardController() != null) {
                startAffectingControlledSite(game, physicalCard);
            }
        }

        // Current site is affecting
        if (_currentPhase != Phase.PLAY_STARTING_FELLOWSHIP)
            startAffecting(game, getCurrentSite());

        // Stacked cards on active cards are stack-affecting
        for (List<LotroPhysicalCardImpl> stackedCards : _stacked.values())
            for (LotroPhysicalCardImpl stackedCard : stackedCards)
                if (isCardInPlayActive(stackedCard.getStackedOn()))
                    startAffectingStacked(game, stackedCard);

        for (List<LotroPhysicalCardImpl> discardedCards : _discards.values())
            for (LotroPhysicalCardImpl discardedCard : discardedCards)
                startAffectingInDiscard(game, discardedCard);
    }

    private void startAffectingControlledSite(DefaultGame game, LotroPhysicalCard physicalCard) {
        ((LotroPhysicalCardImpl) physicalCard).startAffectingGameControlledSite(game);
    }

    public void reapplyAffectingForCard(DefaultGame game, LotroPhysicalCard card) {
        ((LotroPhysicalCardImpl) card).stopAffectingGame();
        ((LotroPhysicalCardImpl) card).startAffectingGame(game);
    }

    public void stopAffectingCardsForCurrentPlayer() {
        for (LotroPhysicalCardImpl physicalCard : _inPlay) {
            if (isCardInPlayActive(physicalCard) && physicalCard.getBlueprint().getCardType() != CardType.SITE)
                stopAffecting(physicalCard);
            else if (physicalCard.getBlueprint().getCardType() == CardType.SITE &&
                    physicalCard.getCardController() != null) {
                stopAffectingControlledSite(physicalCard);
            }
        }

        stopAffecting(getCurrentSite());

        for (List<LotroPhysicalCardImpl> stackedCards : _stacked.values())
            for (LotroPhysicalCardImpl stackedCard : stackedCards)
                if (isCardInPlayActive(stackedCard.getStackedOn()))
                    stopAffectingStacked(stackedCard);

        for (List<LotroPhysicalCardImpl> discardedCards : _discards.values())
            for (LotroPhysicalCardImpl discardedCard : discardedCards)
                stopAffectingInDiscard(discardedCard);
    }

    private void stopAffectingControlledSite(LotroPhysicalCard physicalCard) {
        ((LotroPhysicalCardImpl) physicalCard).stopAffectingGameControlledSite();
    }

    private void startAffecting(DefaultGame game, LotroPhysicalCard card) {
        ((LotroPhysicalCardImpl) card).startAffectingGame(game);
    }

    private void stopAffecting(LotroPhysicalCard card) {
        ((LotroPhysicalCardImpl) card).stopAffectingGame();
    }

    private void startAffectingStacked(DefaultGame game, LotroPhysicalCard card) {
        if (isCardAffectingGame(card))
            ((LotroPhysicalCardImpl) card).startAffectingGameStacked(game);
    }

    private void stopAffectingStacked(LotroPhysicalCard card) {
        if (isCardAffectingGame(card))
            ((LotroPhysicalCardImpl) card).stopAffectingGameStacked();
    }

    private void startAffectingInDiscard(DefaultGame game, LotroPhysicalCard card) {
        if (isCardAffectingGame(card))
            ((LotroPhysicalCardImpl) card).startAffectingGameInDiscard(game);
    }

    private void stopAffectingInDiscard(LotroPhysicalCard card) {
        if (isCardAffectingGame(card))
            ((LotroPhysicalCardImpl) card).stopAffectingGameInDiscard();
    }

    private boolean isCardAffectingGame(LotroPhysicalCard card) {
        final Side side = card.getBlueprint().getSide();
        if (side == Side.SHADOW)
            return !getCurrentPlayerId().equals(card.getOwner());
        else if (side == Side.FREE_PEOPLE)
            return getCurrentPlayerId().equals(card.getOwner());
        else
            return false;
    }

    public void setCurrentPhase(Phase phase) {
        _currentPhase = phase;
        for (GameStateListener listener : getAllGameStateListeners())
            listener.setCurrentPhase(getPhaseString());
    }

    public Phase getCurrentPhase() {
        return _currentPhase;
    }

    public LotroPhysicalCard getSite(int siteNumber) {
        for (LotroPhysicalCardImpl physicalCard : _inPlay) {
            LotroCardBlueprint blueprint = physicalCard.getBlueprint();
            if (blueprint.getCardType() == CardType.SITE && physicalCard.getSiteNumber() == siteNumber)
                return physicalCard;
        }
        return null;
    }

    public LotroPhysicalCard getCurrentSite() {
        return getSite(getCurrentSiteNumber());
    }

    public SitesBlock getCurrentSiteBlock() {
        return getCurrentSite().getBlueprint().getSiteBlock();
    }

    public void increaseMoveCount() {
        _moveCount++;
    }

    public int getMoveCount() {
        return _moveCount;
    }

    public void addTwilight(int twilight) {
        setTwilight(_twilightPool + Math.max(0, twilight));
    }

    public void removeTwilight(int twilight) {
        setTwilight(_twilightPool - Math.min(Math.max(0, twilight), _twilightPool));
    }

    public void assignToSkirmishes(LotroPhysicalCard fp, Set<LotroPhysicalCard> minions) {
        removeFromSkirmish(fp);
        for (LotroPhysicalCard minion : minions) {
            removeFromSkirmish(minion);

            for (Assignment assignment : new LinkedList<>(_assignments)) {
                if (assignment.getShadowCharacters().remove(minion))
                    if (assignment.getShadowCharacters().size() == 0)
                        removeAssignment(assignment);
            }
        }

        Assignment assignment = findAssignment(fp);
        if (assignment != null)
            assignment.getShadowCharacters().addAll(minions);
        else
            _assignments.add(new Assignment(fp, new HashSet<>(minions)));

        for (GameStateListener listener : getAllGameStateListeners())
            listener.addAssignment(fp, minions);
    }

    public void removeAssignment(Assignment assignment) {
        _assignments.remove(assignment);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.removeAssignment(assignment.getFellowshipCharacter());
    }

    public List<Assignment> getAssignments() {
        return _assignments;
    }

    private Assignment findAssignment(LotroPhysicalCard fp) {
        for (Assignment assignment : _assignments)
            if (assignment.getFellowshipCharacter() == fp)
                return assignment;
        return null;
    }

    public void startSkirmish(LotroPhysicalCard fellowshipCharacter, Set<LotroPhysicalCard> shadowCharacters) {
        _skirmish = new Skirmish(fellowshipCharacter, new HashSet<>(shadowCharacters));
        for (GameStateListener listener : getAllGameStateListeners())
            listener.startSkirmish(_skirmish.getFellowshipCharacter(), _skirmish.getShadowCharacters());
    }

    public void restartSkirmish(Skirmish skirmish) {
        _skirmish = skirmish;
        for (GameStateListener listener : getAllGameStateListeners())
            listener.startSkirmish(_skirmish.getFellowshipCharacter(), _skirmish.getShadowCharacters());
    }

    public Skirmish getSkirmish() {
        return _skirmish;
    }

    public void finishSkirmish() {
        if (_skirmish != null) {
            _skirmish = null;
            for (GameStateListener listener : getAllGameStateListeners())
                listener.finishSkirmish();
        }
    }

    public LotroPhysicalCard removeTopDeckCard(String player) {
        List<LotroPhysicalCardImpl> deck = _decks.get(player);
        if (deck.size() > 0) {
            final LotroPhysicalCard topDeckCard = deck.get(0);
            removeCardsFromZone(null, Collections.singleton(topDeckCard));
            return topDeckCard;
        } else {
            return null;
        }
    }

    public LotroPhysicalCard removeBottomDeckCard(String player) {
        List<LotroPhysicalCardImpl> deck = _decks.get(player);
        if (deck.size() > 0) {
            final LotroPhysicalCard topDeckCard = deck.get(deck.size() - 1);
            removeCardsFromZone(null, Collections.singleton(topDeckCard));
            return topDeckCard;
        } else {
            return null;
        }
    }

    public void playerDrawsCard(String player) {
        List<LotroPhysicalCardImpl> deck = _decks.get(player);
        if (deck.size() > 0) {
            LotroPhysicalCard card = deck.get(0);
            removeCardsFromZone(null, Collections.singleton(card));
            addCardToZone(null, card, Zone.HAND);
        }
    }

    public void shuffleDeck(String player) {
        List<LotroPhysicalCardImpl> deck = _decks.get(player);
        Collections.shuffle(deck, ThreadLocalRandom.current());
    }

    public void sendGameStats(GameStats gameStats) {
        for (GameStateListener listener : getAllGameStateListeners())
            listener.sendGameStats(gameStats);
    }

    public void sendWarning(String player, String warning) {
        for (GameStateListener listener : getAllGameStateListeners())
            listener.sendWarning(player, warning);
    }

    public void playEffectReturningResult(LotroPhysicalCard cardPlayed) {
        sendMessage("DEBUG: playEffectReturningResult called for a default game state");
    }

    public void playerPassEffect() {}

    public int getPlayerScore(String playerId) { return 0; }
}