package com.gempukku.lotro.packs;

import com.gempukku.lotro.game.CardCollection;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class UnweightedRandomPack implements PackBox {
    private final Map<String, Integer> _contents = new LinkedHashMap<>();

    private UnweightedRandomPack() {
    }

    public static UnweightedRandomPack LoadFromArray(Iterable<String> items) {
        UnweightedRandomPack box = new UnweightedRandomPack();
        for (String item : items) {
            item = item.trim();
            if (!item.startsWith("#") && item.length() > 0) {
                String[] result = item.split("x", 2);
                box._contents.put(result[1], Integer.parseInt(result[0]));
            }
        }

        return box;
    }

    @Override
    public List<CardCollection.Item> openPack() {
        int selection = ThreadLocalRandom.current().nextInt(_contents.size());
        return openPack(selection);
    }

    public List<CardCollection.Item> openPack(int selection) {
        String key = _contents.keySet().stream().skip(selection).findFirst().orElse(null);
        assert key != null;
        var result = CardCollection.Item.createItem(key, _contents.get(key), true);
        return Collections.singletonList(result);
    }

    @Override
    public List<String> GetAllOptions() {
        return _contents.keySet().stream().toList();
    }
}
