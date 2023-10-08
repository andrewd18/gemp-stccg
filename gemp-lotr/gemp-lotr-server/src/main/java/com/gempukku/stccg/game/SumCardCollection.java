package com.gempukku.stccg.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SumCardCollection implements CardCollection {
    private final List<CardCollection> _cardCollections;

    public SumCardCollection(List<CardCollection> cardCollections) {
        _cardCollections = cardCollections;
    }

    @Override
    public int getCurrency() {
        int sum = 0;
        for (CardCollection cardCollection : _cardCollections)
            sum += cardCollection.getCurrency();

        return sum;
    }

    @Override
    public Map<String, Object> getExtraInformation() {
        Map<String, Object> result = new HashMap<>();
        for (CardCollection cardCollection : _cardCollections) {
            result.putAll(cardCollection.getExtraInformation());
        }
        return result;
    }

    @Override
    public Iterable<Item> getAll() {
        Map<String, Item> sum = new HashMap<>();
        for (CardCollection cardCollection : _cardCollections) {
            Iterable<Item> inCollection = cardCollection.getAll();
            for (Item cardCount : inCollection) {
                String cardId = cardCount.getBlueprintId();
                int count = sum.get(cardId).getCount();
                sum.put(cardId, Item.createItem(cardId, count + cardCount.getCount()));
            }
        }

        return sum.values();
    }

    @Override
    public int getItemCount(String blueprintId) {
        int sum = 0;
        for (CardCollection cardCollection : _cardCollections)
            sum += cardCollection.getItemCount(blueprintId);

        return sum;
    }
}
