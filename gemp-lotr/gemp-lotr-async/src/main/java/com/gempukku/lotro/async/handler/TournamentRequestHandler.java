package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.cards.lotronly.LotroDeck;
import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.game.SortAndFilterCards;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.tournament.Tournament;
import com.gempukku.lotro.tournament.TournamentService;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class TournamentRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {
    private final TournamentService _tournamentService;
    private final LotroFormatLibrary _formatLibrary;
    private final SortAndFilterCards _sortAndFilterCards;

    private static final Logger _log = Logger.getLogger(TournamentRequestHandler.class);

    public TournamentRequestHandler(Map<Type, Object> context) {
        super(context);

        _tournamentService = extractObject(context, TournamentService.class);
        _formatLibrary = extractObject(context, LotroFormatLibrary.class);
        _sortAndFilterCards = new SortAndFilterCards();
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.equals("") && request.method() == HttpMethod.GET) {
            getCurrentTournaments(request, responseWriter);
        } else if (uri.equals("/history") && request.method() == HttpMethod.GET) {
            getTournamentHistory(responseWriter);
        } else if (uri.startsWith("/") && uri.endsWith("/html") && uri.contains("/deck/") && request.method() == HttpMethod.GET) {
            getTournamentDeck(uri.substring(1, uri.indexOf("/deck/")), uri.substring(uri.indexOf("/deck/") + 6, uri.lastIndexOf("/html")), responseWriter);
        } else if (uri.startsWith("/") && request.method() == HttpMethod.GET) {
            getTournamentInfo(request, uri.substring(1), responseWriter);
        } else {
            throw new HttpProcessingException(404);
        }
    }

    private void getTournamentInfo(HttpRequest request, String tournamentId, ResponseWriter responseWriter) throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Document doc = documentBuilder.newDocument();

        Tournament tournament = _tournamentService.getTournamentById(tournamentId);
        if (tournament == null)
            throw new HttpProcessingException(404);

        Element tournamentElem = doc.createElement("tournament");

        tournamentElem.setAttribute("id", tournament.getTournamentId());
        tournamentElem.setAttribute("name", tournament.getTournamentName());
        tournamentElem.setAttribute("format", _formatLibrary.getFormat(tournament.getFormat()).getName());
        tournamentElem.setAttribute("collection", tournament.getCollectionType().getFullName());
        tournamentElem.setAttribute("round", String.valueOf(tournament.getCurrentRound()));
        tournamentElem.setAttribute("stage", tournament.getTournamentStage().getHumanReadable());

        List<PlayerStanding> leagueStandings = tournament.getCurrentStandings();
        for (PlayerStanding standing : leagueStandings) {
            Element standingElem = doc.createElement("tournamentStanding");
            setStandingAttributes(standing, standingElem);
            tournamentElem.appendChild(standingElem);
        }

        doc.appendChild(tournamentElem);

        responseWriter.writeXmlResponse(doc);
    }

    private void setStandingAttributes(PlayerStanding standing, Element standingElem) {
        standingElem.setAttribute("player", standing.getPlayerName());
        standingElem.setAttribute("standing", String.valueOf(standing.getStanding()));
        standingElem.setAttribute("points", String.valueOf(standing.getPoints()));
        standingElem.setAttribute("gamesPlayed", String.valueOf(standing.getGamesPlayed()));
        DecimalFormat format = new DecimalFormat("##0.00%");
        standingElem.setAttribute("opponentWin", format.format(standing.getOpponentWin()));
    }

    private void getTournamentDeck(String tournamentId, String playerName, ResponseWriter responseWriter)
            throws Exception {
        Tournament tournament = _tournamentService.getTournamentById(tournamentId);
        if (tournament == null)
            throw new HttpProcessingException(404);

        if (tournament.getTournamentStage() != Tournament.Stage.FINISHED)
            throw new HttpProcessingException(403);

        LotroDeck deck = _tournamentService.getPlayerDeck(tournamentId, playerName, tournament.getFormat());
        if (deck == null)
            throw new HttpProcessingException(404);

        String result = "<html><body>" +
                "<h1>" + StringEscapeUtils.escapeHtml(deck.getDeckName()) + "</h1>" +
                "<h2>by " + playerName + "</h2>" +
                getHTMLDeck(deck, false, _sortAndFilterCards, _formatLibrary) +
                "</body></html>";
        responseWriter.writeHtmlResponse(result);
    }

    private void getTournamentHistory(ResponseWriter responseWriter) throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Document doc = documentBuilder.newDocument();
        Element tournaments = doc.createElement("tournaments");

        for (Tournament tournament : _tournamentService.getOldTournaments(System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 7))) {
            Element tournamentElem = doc.createElement("tournament");

            tournamentElem.setAttribute("id", tournament.getTournamentId());
            tournamentElem.setAttribute("name", tournament.getTournamentName());
            tournamentElem.setAttribute("format", _formatLibrary.getFormat(tournament.getFormat()).getName());
            tournamentElem.setAttribute("collection", tournament.getCollectionType().getFullName());
            tournamentElem.setAttribute("round", String.valueOf(tournament.getCurrentRound()));
            tournamentElem.setAttribute("stage", tournament.getTournamentStage().getHumanReadable());

            tournaments.appendChild(tournamentElem);
        }

        doc.appendChild(tournaments);

        responseWriter.writeXmlResponse(doc);
    }

    private void getCurrentTournaments(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Document doc = documentBuilder.newDocument();
        Element tournaments = doc.createElement("tournaments");

        for (Tournament tournament : _tournamentService.getLiveTournaments()) {
            Element tournamentElem = doc.createElement("tournament");

            tournamentElem.setAttribute("id", tournament.getTournamentId());
            tournamentElem.setAttribute("name", tournament.getTournamentName());
            tournamentElem.setAttribute("format", _formatLibrary.getFormat(tournament.getFormat()).getName());
            tournamentElem.setAttribute("collection", tournament.getCollectionType().getFullName());
            tournamentElem.setAttribute("round", String.valueOf(tournament.getCurrentRound()));
            tournamentElem.setAttribute("stage", tournament.getTournamentStage().getHumanReadable());

            tournaments.appendChild(tournamentElem);
        }

        doc.appendChild(tournaments);

        responseWriter.writeXmlResponse(doc);
    }
}
