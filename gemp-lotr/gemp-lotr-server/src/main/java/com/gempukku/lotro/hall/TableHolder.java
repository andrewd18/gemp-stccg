package com.gempukku.lotro.hall;

import com.gempukku.lotro.db.IgnoreDAO;
import com.gempukku.lotro.db.vo.League;
import com.gempukku.lotro.game.LotroGameMediator;
import com.gempukku.lotro.game.LotroGameParticipant;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.league.LeagueSerieData;
import com.gempukku.lotro.league.LeagueService;
import com.gempukku.lotro.cards.LotroDeck;

import java.util.*;
import org.apache.log4j.Logger;

public class TableHolder {
    private static final Logger logger = Logger.getLogger(TableHolder.class);
    private final LeagueService leagueService;
    private final IgnoreDAO ignoreDAO;

    private final Map<String, GameTable> awaitingTables = new LinkedHashMap<>();
    private final Map<String, GameTable> runningTables = new LinkedHashMap<>();

    private int _nextTableId = 1;

    public TableHolder(LeagueService leagueService, IgnoreDAO ignoreDAO) {
        this.leagueService = leagueService;
        this.ignoreDAO = ignoreDAO;
    }

    public int getTableCount() {
        return runningTables.size();
    }

    public void cancelWaitingTables() {
        awaitingTables.clear();
    }

    public GameTable createTable(Player player, GameSettings gameSettings, LotroDeck lotroDeck) throws HallException {
        logger.debug("TableHolder - createTable function called");
        String tableId = String.valueOf(_nextTableId++);

        final League league = gameSettings.getLeague();
        if (league != null) {
            verifyNotPlayingLeagueGame(player, league);

            if (!leagueService.isPlayerInLeague(league, player))
                throw new HallException("You're not in that league");

            if (!leagueService.canPlayRankedGame(league, gameSettings.getLeagueSerie(), player.getName()))
                throw new HallException("You have already played max games in league");
        }

        GameTable table = new GameTable(gameSettings);

        boolean tableFull = table.addPlayer(new LotroGameParticipant(player.getName(), lotroDeck));
        if (tableFull) {
            runningTables.put(tableId, table);
            return table;
        }

        awaitingTables.put(tableId, table);
        return null;
    }

    public GameTable joinTable(String tableId, Player player, LotroDeck lotroDeck) throws HallException {
        final GameTable awaitingTable = awaitingTables.get(tableId);

        if (awaitingTable == null || awaitingTable.wasGameStarted())
            throw new HallException("Table is already taken or was removed");

        if (awaitingTable.hasPlayer(player.getName()))
            throw new HallException("You can't play against yourself");

        final League league = awaitingTable.getGameSettings().getLeague();
        if (league != null) {
            verifyNotPlayingLeagueGame(player, league);

            if (!leagueService.isPlayerInLeague(league, player))
                throw new HallException("You're not in that league");

            LeagueSerieData leagueSerie = awaitingTable.getGameSettings().getLeagueSerie();
            if (!leagueService.canPlayRankedGame(league, leagueSerie, player.getName()))
                throw new HallException("You have already played max games in league");
            if (awaitingTable.getPlayerNames().size() != 0 && !leagueService.canPlayRankedGameAgainst(league, leagueSerie, awaitingTable.getPlayerNames().iterator().next(), player.getName()))
                throw new HallException("You have already played ranked league game against this player in that series");
        }

        final boolean tableFull = awaitingTable.addPlayer(new LotroGameParticipant(player.getName(), lotroDeck));
        if (tableFull) {
            awaitingTables.remove(tableId);
            runningTables.put(tableId, awaitingTable);

            // Leave all other tables this player is waiting on
            for (LotroGameParticipant awaitingTablePlayer : awaitingTable.getPlayers())
                leaveAwaitingTablesForPlayer(awaitingTablePlayer.getPlayerId());

            return awaitingTable;
        }
        return null;
    }

    public GameTable setupTournamentTable(GameSettings gameSettings, LotroGameParticipant[] participants) {
        String tableId = String.valueOf(_nextTableId++);

        GameTable table = new GameTable(gameSettings);
        for (LotroGameParticipant participant : participants) {
            table.addPlayer(participant);
        }
        runningTables.put(tableId, table);

        return table;
    }

    public GameSettings getGameSettings(String tableId) throws HallException {
        final GameTable gameTable = awaitingTables.get(tableId);
        if (gameTable != null)
            return gameTable.getGameSettings();
        GameTable runningTable = runningTables.get(tableId);
        if (runningTable != null)
            return runningTable.getGameSettings();
        throw new HallException("Table was already removed");
    }

    public boolean leaveAwaitingTable(Player player, String tableId) {
        GameTable table = awaitingTables.get(tableId);
        if (table != null && table.hasPlayer(player.getName())) {
            boolean empty = table.removePlayer(player.getName());
            if (empty)
                awaitingTables.remove(tableId);
            return true;
        }
        return false;
    }

    public boolean leaveAwaitingTablesForPlayer(Player player) {
        return leaveAwaitingTablesForPlayer(player.getName());
    }

    private boolean leaveAwaitingTablesForPlayer(String playerId) {
        boolean result = false;
        final Iterator<Map.Entry<String, GameTable>> iterator = awaitingTables.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<String, GameTable> table = iterator.next();
            if (table.getValue().hasPlayer(playerId)) {
                boolean empty = table.getValue().removePlayer(playerId);
                if (empty)
                    iterator.remove();
                result = true;
            }
        }
        return result;
    }

    private void verifyNotPlayingLeagueGame(Player player, League league) throws HallException {
        for (GameTable awaitingTable : awaitingTables.values()) {
            if (league.equals(awaitingTable.getGameSettings().getLeague())
                    && awaitingTable.hasPlayer(player.getName())) {
                throw new HallException("You can't play in multiple league games at the same time");
            }
        }

        for (GameTable runningTable : runningTables.values()) {
            if (league.equals(runningTable.getGameSettings().getLeague())) {
                LotroGameMediator game = runningTable.getLotroGameMediator();
                if (game != null && !game.isFinished() && game.getPlayersPlaying().contains(player.getName()))
                    throw new HallException("You can't play in multiple league games at the same time");
            }
        }
    }

    public void processTables(boolean isAdmin, Player player, HallInfoVisitor visitor) {
        // First waiting
        for (Map.Entry<String, GameTable> tableInformation : awaitingTables.entrySet()) {
            final GameTable table = tableInformation.getValue();

            List<String> players;
            if (table.getGameSettings().getLeague() != null)
                players = Collections.emptyList();
            else
                players = table.getPlayerNames();

            if (isAdmin || isNoIgnores(players, player.getName()))
                visitor.visitTable(tableInformation.getKey(), null, false, HallInfoVisitor.TableStatus.WAITING, "Waiting", table.getGameSettings().getLotroFormat().getName(), getTournamentName(table), table.getGameSettings().getUserDescription(), players, table.getPlayerNames().contains(player.getName()), table.getGameSettings().isPrivateGame(), table.getGameSettings().isUserInviteOnly(), null);
        }

        // Then non-finished
        Map<String, GameTable> finishedTables = new HashMap<>();

        for (Map.Entry<String, GameTable> runningGame : runningTables.entrySet()) {
            final GameTable runningTable = runningGame.getValue();
            LotroGameMediator lotroGameMediator = runningTable.getLotroGameMediator();
            if (lotroGameMediator != null) {
                if (isAdmin || (lotroGameMediator.isVisibleToUser(player.getName()) &&
                        isNoIgnores(lotroGameMediator.getPlayersPlaying(), player.getName()))) {
                    if (lotroGameMediator.isFinished())
                        finishedTables.put(runningGame.getKey(), runningTable);
                    else
                        visitor.visitTable(runningGame.getKey(), lotroGameMediator.getGameId(), isAdmin || lotroGameMediator.isAllowSpectators(), HallInfoVisitor.TableStatus.PLAYING, lotroGameMediator.getGameStatus(), runningTable.getGameSettings().getLotroFormat().getName(), getTournamentName(runningTable), runningTable.getGameSettings().getUserDescription(), lotroGameMediator.getPlayersPlaying(), lotroGameMediator.getPlayersPlaying().contains(player.getName()), runningTable.getGameSettings().isPrivateGame(),  runningTable.getGameSettings().isUserInviteOnly(), lotroGameMediator.getWinner());

                    if (!lotroGameMediator.isFinished() && lotroGameMediator.getPlayersPlaying().contains(player.getName()))
                        visitor.runningPlayerGame(lotroGameMediator.getGameId());
                }
            }
        }

        // Then rest
        for (Map.Entry<String, GameTable> nonPlayingGame : finishedTables.entrySet()) {
            final GameTable runningTable = nonPlayingGame.getValue();
            LotroGameMediator lotroGameMediator = runningTable.getLotroGameMediator();
            if (lotroGameMediator != null) {
                if (isAdmin || isNoIgnores(lotroGameMediator.getPlayersPlaying(), player.getName()))
                    visitor.visitTable(nonPlayingGame.getKey(), lotroGameMediator.getGameId(), false, HallInfoVisitor.TableStatus.FINISHED, lotroGameMediator.getGameStatus(), runningTable.getGameSettings().getLotroFormat().getName(), getTournamentName(runningTable), runningTable.getGameSettings().getUserDescription(), lotroGameMediator.getPlayersPlaying(), lotroGameMediator.getPlayersPlaying().contains(player.getName()), runningTable.getGameSettings().isPrivateGame(),  runningTable.getGameSettings().isUserInviteOnly(), lotroGameMediator.getWinner());
            }
        }
    }

    public boolean removeFinishedGames() {
        boolean changed = false;
        final Iterator<Map.Entry<String, GameTable>> iterator = runningTables.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<String, GameTable> runningTable = iterator.next();
            LotroGameMediator lotroGameMediator = runningTable.getValue().getLotroGameMediator();
            if (lotroGameMediator.isDestroyed()) {
                iterator.remove();
                changed = true;
                ;
            }
        }
        return changed;
    }

    private boolean isNoIgnores(Collection<String> participants, String playerLooking) {
        // Do not ignore your own stuff
        if (participants.contains(playerLooking))
            return true;

        // This player ignores someone of the participants
        final Set<String> ignoredUsers = ignoreDAO.getIgnoredUsers(playerLooking);
        if (!Collections.disjoint(ignoredUsers, participants))
            return false;

        // One of the participants ignores this player
        for (String player : participants) {
            final Set<String> ignored = ignoreDAO.getIgnoredUsers(player);
            if (ignored.contains(playerLooking))
                return false;
        }

        return true;
    }

    private String getTournamentName(GameTable table) {
        final League league = table.getGameSettings().getLeague();
        if (league != null)
            return league.getName() + " - " + table.getGameSettings().getLeagueSerie().getName();
        else
            return "Casual - " + table.getGameSettings().getTimeSettings().name();
    }
}
