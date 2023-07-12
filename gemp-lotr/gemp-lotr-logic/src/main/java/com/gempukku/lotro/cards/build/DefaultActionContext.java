package com.gempukku.lotro.cards.build;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.game.effects.Effect;
import com.gempukku.lotro.game.effects.EffectResult;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DefaultActionContext implements ActionContext {
    private final Multimap<String, PhysicalCard> cardMemory = HashMultimap.create();
    private final Map<String, String> valueMemory = new HashMap<>();

    private final String performingPlayer;
    private final DefaultGame game;
    private final PhysicalCard source;
    private final EffectResult effectResult;
    private final Effect effect;

    public DefaultActionContext(String performingPlayer, DefaultGame game, PhysicalCard source, EffectResult effectResult, Effect effect) {
        this.performingPlayer = performingPlayer;
        this.game = game;
        this.source = source;
        this.effectResult = effectResult;
        this.effect = effect;
    }

    @Override
    public void setValueToMemory(String memory, String value) {
        if(memory != null) {
            memory = memory.toLowerCase();
        }
        valueMemory.put(memory, value);
    }

    @Override
    public String getValueFromMemory(String memory) {
        if(memory != null) {
            memory = memory.toLowerCase();
        }
        final String result = valueMemory.get(memory);
        if (result == null)
            throw new IllegalArgumentException("Memory not found - " + memory);
        return result;
    }

    @Override
    public void setCardMemory(String memory, PhysicalCard card) {
        if(memory != null) {
            memory = memory.toLowerCase();
        }
        cardMemory.removeAll(memory);
        if (card != null)
            cardMemory.put(memory, card);
    }

    @Override
    public void setCardMemory(String memory, Collection<? extends PhysicalCard> cards) {
        if(memory != null) {
            memory = memory.toLowerCase();
        }
        cardMemory.removeAll(memory);
        cardMemory.putAll(memory, cards);
    }

    @Override
    public Collection<? extends PhysicalCard> getCardsFromMemory(String memory) {
        if(memory != null) {
            memory = memory.toLowerCase();
        }
        return cardMemory.get(memory);
    }

    @Override
    public PhysicalCard getCardFromMemory(String memory) {
        if(memory != null) {
            memory = memory.toLowerCase();
        }
        final Collection<PhysicalCard> physicalCards = cardMemory.get(memory);
        if (physicalCards.size() == 0)
            return null;
        if (physicalCards.size() != 1)
            throw new RuntimeException("Unable to retrieve one card from memory: " + memory);
        return physicalCards.iterator().next();
    }

    @Override
    public String getPerformingPlayer() {
        return performingPlayer;
    }

    @Override
    public DefaultGame getGame() {
        return game;
    }

    @Override
    public PhysicalCard getSource() {
        return source;
    }

    @Override
    public EffectResult getEffectResult() {
        return effectResult;
    }

    @Override
    public Effect getEffect() {
        return effect;
    }
}
