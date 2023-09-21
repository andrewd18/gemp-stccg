package com.gempukku.lotro.results;

import com.gempukku.lotro.effects.EffectResult;

public class ReplaceSiteResult extends EffectResult {
    private final int _siteNumber;
    private final String _playerId;

    public ReplaceSiteResult(String playerId, int siteNumber) {
        super(Type.REPLACE_SITE);
        _playerId = playerId;
        _siteNumber = siteNumber;
    }

    public String getPlayerId() {
        return _playerId;
    }

    public int getSiteNumber() {
        return _siteNumber;
    }
}
