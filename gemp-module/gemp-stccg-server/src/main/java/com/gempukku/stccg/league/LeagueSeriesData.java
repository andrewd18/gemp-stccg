package com.gempukku.stccg.league;

import com.gempukku.stccg.db.vo.CollectionType;
import com.gempukku.stccg.cards.CardCollection;
import com.gempukku.stccg.formats.GameFormat;

public interface LeagueSeriesData {
    int getStart();

    int getEnd();

    int getMaxMatches();

    boolean isLimited();

    String getName();

    GameFormat getFormat();

    CollectionType getCollectionType();

    CardCollection getPrizeForLeagueMatchWinner(int winCount, int totalGamesPlayed);

    CardCollection getPrizeForLeagueMatchLoser(int winCount, int totalGamesPlayed);
}
