package com.gempukku.lotro.packs;

import com.gempukku.lotro.draft.DraftPack;

import java.util.HashMap;
import java.util.Map;

public class DraftPackStorage {
    private final Map<String, DraftPack> _draftPacksByType = new HashMap<>();

    public void getDraftPack(String draftType) {
        _draftPacksByType.get(draftType);
    }
}
