package com.gempukku.stccg.draft.builder;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

public class DraftPoolBuilder {
    public static DraftPoolProducer buildDraftPoolProducer(JSONArray draftPoolComponents) {
    
        List<DraftPoolElement> fullDraftPool = new ArrayList<>();
        for (JSONObject draftPoolComponent : (Iterable<JSONObject>) draftPoolComponents) {
            fullDraftPool.add(buildDraftPool(draftPoolComponent));
        }

        return (seed, code) -> {
            List<String> completedDraftPool = new ArrayList<>();
            Random randomSource = new Random();
            int mod = 0;

            for (DraftPoolElement element : fullDraftPool) {
                List<ArrayList<String>> draftPacks;
                draftPacks = element.getDraftPackList();
                if (Objects.equals(element.getDraftPoolType(), "singleDraft"))
                    randomSource = new Random(seed+mod);
                else if (Objects.equals(element.getDraftPoolType(), "sharedDraft"))
                    randomSource = new Random(code);
                mod++;

                float thisFixesARandomnessBug = randomSource.nextFloat();
                Collections.shuffle(draftPacks, randomSource);
                for (int i = 0; i < element.getPacksToDraft(); i++) {
                    completedDraftPool.addAll(draftPacks.get(i));
                }
            }
            return completedDraftPool;
        };
    }

    public static DraftPoolElement buildDraftPool(JSONObject draftPool) {
        String draftPoolProducerType = (String) draftPool.get("type");
        if (draftPoolProducerType.equals("singleDraft")) {
            return buildSingleDraftPool((JSONObject) draftPool.get("data"));
        } else if (draftPoolProducerType.equals("sharedDraft")) {
            return buildSharedDraftPool((JSONObject) draftPool.get("data"));
        }
        throw new RuntimeException("Unknown draftPoolProducer type: " + draftPoolProducerType);
    }
    
    private static DefaultDraftPoolElement buildSingleDraftPool(JSONObject data) {
        int choose = ((Number) data.get("choose")).intValue();
        JSONArray draftPackPool = (JSONArray) data.get("packs");

        List draftPacks = new ArrayList();
        for (JSONArray cards : (Iterable<JSONArray>) draftPackPool) {
            List<String> draftPack = new ArrayList<>();
            for (String card : (Iterable<String>) cards) {
                draftPack.add(card);
            }
            draftPacks.add(draftPack);
        }
        return new DefaultDraftPoolElement("singleDraft", draftPacks, choose);
    }

    private static DefaultDraftPoolElement buildSharedDraftPool(JSONObject data) {
        int choose = ((Number) data.get("choose")).intValue();
        JSONArray draftPackPool = (JSONArray) data.get("packs");

        List draftPacks = new ArrayList();
        for (JSONArray cards : (Iterable<JSONArray>) draftPackPool) {
            List<String> draftPack = new ArrayList<>();
            for (String card : (Iterable<String>) cards) {
                draftPack.add(card);
            }
            draftPacks.add(draftPack);
        }
        return new DefaultDraftPoolElement("sharedDraft", draftPacks, choose);
    }
}
