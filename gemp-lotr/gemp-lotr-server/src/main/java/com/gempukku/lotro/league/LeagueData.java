package com.gempukku.lotro.league;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.draft2.SoloDraft;
import com.gempukku.lotro.game.User;

import java.util.List;

public interface LeagueData {
    boolean isSoloDraftLeague();

    List<LeagueSeriesData> getSeries();

    SoloDraft getSoloDraft();

    void joinLeague(CollectionsManager collecionsManager, User player, int currentTime);

    int process(CollectionsManager collectionsManager, List<PlayerStanding> leagueStandings, int oldStatus, int currentTime);

    default int getMaxRepeatMatchesPerSerie() {
        return 1;
    }
}
