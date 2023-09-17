package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.DateUtils;
import com.gempukku.lotro.async.HttpProcessingException;
import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.cache.CacheManager;
import com.gempukku.lotro.cards.CardBlueprintLibrary;
import com.gempukku.lotro.chat.ChatServer;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.db.LeagueDAO;
import com.gempukku.lotro.db.PlayerDAO;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.User;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.hall.HallServer;
import com.gempukku.lotro.league.*;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.lotro.service.AdminService;
import com.gempukku.lotro.tournament.TournamentService;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

public class AdminRequestHandler extends LotroServerRequestHandler implements UriRequestHandler {
    private final CardBlueprintLibrary _cardLibrary;
    private final ProductLibrary _productLibrary;
    private final SoloDraftDefinitions _soloDraftDefinitions;
    private final LeagueService _leagueService;
    private final TournamentService _tournamentService;
    private final CacheManager _cacheManager;
    private final HallServer _hallServer;
    private final LotroFormatLibrary _formatLibrary;
    private final LeagueDAO _leagueDao;
    private final CollectionsManager _collectionManager;
    private final PlayerDAO _playerDAO;
    private final AdminService _adminService;
    private final ChatServer _chatServer;

    private static final Logger _log = Logger.getLogger(AdminRequestHandler.class);

    public AdminRequestHandler(Map<Type, Object> context) {
        super(context);
        _soloDraftDefinitions = extractObject(context, SoloDraftDefinitions.class);
        _leagueService = extractObject(context, LeagueService.class);
        _tournamentService = extractObject(context, TournamentService.class);
        _cacheManager = extractObject(context, CacheManager.class);
        _hallServer = extractObject(context, HallServer.class);
        _formatLibrary = extractObject(context, LotroFormatLibrary.class);
        _leagueDao = extractObject(context, LeagueDAO.class);
        _playerDAO = extractObject(context, PlayerDAO.class);
        _collectionManager = extractObject(context, CollectionsManager.class);
        _adminService = extractObject(context, AdminService.class);
        _cardLibrary = extractObject(context, CardBlueprintLibrary.class);
        _productLibrary = extractObject(context, ProductLibrary.class);
        _chatServer = extractObject(context, ChatServer.class);
    }

    @Override
    public void handleRequest(String uri, HttpRequest request, Map<Type, Object> context, ResponseWriter responseWriter, String remoteIp) throws Exception {
        if (uri.equals("/clearCache") && request.method() == HttpMethod.POST) {
            clearCache(request, responseWriter);
        } else if (uri.equals("/shutdown") && request.method() == HttpMethod.POST) {
            shutdown(request, responseWriter);
        } else if (uri.equals("/reloadCards") && request.method() == HttpMethod.POST) {
            reloadCards(request, responseWriter);
        } else if (uri.equals("/getMOTD") && request.method() == HttpMethod.GET) {
            getMotd(request, responseWriter);
        }else if (uri.equals("/setMOTD") && request.method() == HttpMethod.POST) {
            setMotd(request, responseWriter);
        }else if (uri.equals("/previewSealedLeague") && request.method() == HttpMethod.POST) {
            previewSealedLeague(request, responseWriter);
        } else if (uri.equals("/addSealedLeague") && request.method() == HttpMethod.POST) {
            addSealedLeague(request, responseWriter);
        } else if (uri.equals("/previewConstructedLeague") && request.method() == HttpMethod.POST) {
            previewConstructedLeague(request, responseWriter);
        } else if (uri.equals("/addConstructedLeague") && request.method() == HttpMethod.POST) {
            addConstructedLeague(request, responseWriter);
        } else if (uri.equals("/previewSoloDraftLeague") && request.method() == HttpMethod.POST) {
            previewSoloDraftLeague(request, responseWriter);
        } else if (uri.equals("/addSoloDraftLeague") && request.method() == HttpMethod.POST) {
            addSoloDraftLeague(request, responseWriter);
        } else if (uri.equals("/addItems") && request.method() == HttpMethod.POST) {
            addItems(request, responseWriter);
        } else if (uri.equals("/addItemsToCollection") && request.method() == HttpMethod.POST) {
            addItemsToCollection(request, responseWriter);
        } else if (uri.equals("/banUser") && request.method() == HttpMethod.POST) {
            banUser(request, responseWriter);
        } else if (uri.equals("/resetUserPassword") && request.method() == HttpMethod.POST) {
            resetUserPassword(request, responseWriter);
        } else if (uri.equals("/banMultiple") && request.method() == HttpMethod.POST) {
            banMultiple(request, responseWriter);
        } else if (uri.equals("/banUserTemp") && request.method() == HttpMethod.POST) {
            banUserTemp(request, responseWriter);
        } else if (uri.equals("/unBanUser") && request.method() == HttpMethod.POST) {
            unBanUser(request, responseWriter);
        } else if (uri.equals("/findMultipleAccounts") && request.method() == HttpMethod.POST) {
            findMultipleAccounts(request, responseWriter);
        } else {
            throw new HttpProcessingException(404);
        }
    }

    private void findMultipleAccounts(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String login = getFormParameterSafely(postDecoder, "login").trim();

            List<User> similarPlayers = _playerDAO.findSimilarAccounts(login);
            if (similarPlayers == null)
                throw new HttpProcessingException(400);

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            Document doc = documentBuilder.newDocument();
            Element players = doc.createElement("players");

            for (User similarPlayer : similarPlayers) {
                Element playerElem = doc.createElement("player");
                playerElem.setAttribute("id", String.valueOf(similarPlayer.getId()));
                playerElem.setAttribute("name", similarPlayer.getName());
                playerElem.setAttribute("password", similarPlayer.getPassword());
                playerElem.setAttribute("status", getStatus(similarPlayer));
                playerElem.setAttribute("createIp", similarPlayer.getCreateIp());
                playerElem.setAttribute("loginIp", similarPlayer.getLastIp());
                players.appendChild(playerElem);
            }

            doc.appendChild(players);

            responseWriter.writeXmlResponse(doc);
        } finally {
            postDecoder.destroy();
        }
    }

    private String getStatus(User similarPlayer) {
        if (similarPlayer.getType().equals(""))
            return "Banned permanently";
        if (similarPlayer.getBannedUntil() != null) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            return "Banned until " + format.format(similarPlayer.getBannedUntil());
        }
        if (similarPlayer.hasType(User.Type.UNBANNED))
            return "Unbanned";
        return "OK";
    }

    private void resetUserPassword(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String login = getFormParameterSafely(postDecoder, "login");

            if (login==null)
                throw new HttpProcessingException(400);

            if (!_adminService.resetUserPassword(login))
                throw new HttpProcessingException(404);

            responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private void banUser(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String login = getFormParameterSafely(postDecoder, "login");

            if (login==null)
                throw new HttpProcessingException(400);

            if (!_adminService.banUser(login))
                throw new HttpProcessingException(404);

            responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private void banMultiple(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            List<String> logins = getFormParametersSafely(postDecoder);
            if (logins == null)
                throw new HttpProcessingException(400);

            for (String login : logins) {
                if (!_adminService.banUser(login))
                    throw new HttpProcessingException(404);
        }

        responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private void banUserTemp(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String login = getFormParameterSafely(postDecoder, "login");
            int duration = Integer.parseInt(getFormParameterSafely(postDecoder, "duration"));

            if (!_adminService.banUserTemp(login, duration))
                throw new HttpProcessingException(404);

            responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private void unBanUser(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String login = getFormParameterSafely(postDecoder, "login");

            if (!_adminService.unBanUser(login))
                throw new HttpProcessingException(404);

            responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private void addItemsToCollection(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String reason = getFormParameterSafely(postDecoder, "reason");
            String product = getFormParameterSafely(postDecoder, "product");
            String collectionType = getFormParameterSafely(postDecoder, "collectionType");

            Collection<CardCollection.Item> productItems = getProductItems(product);

            Map<User, CardCollection> playersCollection = _collectionManager.getPlayersCollection(collectionType);

            for (Map.Entry<User, CardCollection> playerCollection : playersCollection.entrySet())
                _collectionManager.addItemsToPlayerCollection(true, reason, playerCollection.getKey(), createCollectionType(collectionType), productItems);

            responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private void addItems(HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException, IOException {
        validateAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String players = getFormParameterSafely(postDecoder, "players");
            String product = getFormParameterSafely(postDecoder, "product");
            String collectionType = getFormParameterSafely(postDecoder, "collectionType");

            Collection<CardCollection.Item> productItems = getProductItems(product);

            List<String> playerNames = getItems(players);

            for (String playerName : playerNames) {
                User player = _playerDao.getPlayer(playerName);

            _collectionManager.addItemsToPlayerCollection(true, "Administrator action", player, createCollectionType(collectionType), productItems);
        }

        responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private List<String> getItems(String values) {
        List<String> result = new LinkedList<>();
        for (String pack : values.split("\n")) {
            String blueprint = pack.trim();
            if (blueprint.length() > 0)
                result.add(blueprint);
        }
        return result;
    }

    private Collection<CardCollection.Item> getProductItems(String values) {
        List<CardCollection.Item> result = new LinkedList<>();
        for (String item : values.split("\n")) {
            item = item.trim();
            if (item.length() > 0) {
                final String[] itemSplit = item.split("x", 2);
                if (itemSplit.length != 2)
                    throw new RuntimeException("Unable to parse the items");
                result.add(CardCollection.Item.createItem(itemSplit[1].trim(), Integer.parseInt(itemSplit[0].trim())));
            }
        }
        return result;
    }

    private CollectionType createCollectionType(String collectionType) {
        final CollectionType result = CollectionType.getCollectionTypeByCode(collectionType);
        if (result != null)
            return result;

        return _leagueService.getCollectionTypeByCode(collectionType);
    }

    private void addConstructedLeague(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateLeagueAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String start = getFormParameterSafely(postDecoder, "start");
            String collectionType = getFormParameterSafely(postDecoder, "collectionType");
            String prizeMultiplier = getFormParameterSafely(postDecoder, "prizeMultiplier");
            List<String> formats = getFormMultipleParametersSafely(postDecoder, "format[]");
            List<String> seriesDurations = getFormMultipleParametersSafely(postDecoder, "seriesDuration[]");
            List<String> maxMatches = getFormMultipleParametersSafely(postDecoder, "maxMatches[]");
            String name = getFormParameterSafely(postDecoder, "name");
            String costStr = getFormParameterSafely(postDecoder, "cost");

            if(start == null || start.trim().isEmpty()
                    ||collectionType == null || collectionType.trim().isEmpty()
                    ||prizeMultiplier == null || prizeMultiplier.trim().isEmpty()
                    ||name == null || name.trim().isEmpty()
                    ||costStr == null || costStr.trim().isEmpty()) {
                throw new HttpProcessingException(400);
            }

            if(formats.size() != seriesDurations.size() || formats.size() != maxMatches.size())
                throw new HttpProcessingException(400);

            int cost = Integer.parseInt(costStr);

            String code = String.valueOf(System.currentTimeMillis());

            //The 1 is a hard-coded maximum number of player matches per league.
            //TODO: Get this put into the UI properly.
            StringJoiner sj = new StringJoiner(",");
            sj.add(start).add(collectionType).add(prizeMultiplier).add("1").add(Integer.toString(formats.size()));
            for (int i = 0; i < formats.size(); i++)
                sj.add(formats.get(i)).add(seriesDurations.get(i)).add(maxMatches.get(i));
            String parameters = sj.toString();

            LeagueData leagueData = new NewConstructedLeagueData(_cardLibrary, _formatLibrary, parameters);
            List<LeagueSeriesData> series = leagueData.getSeries();
            int leagueStart = series.get(0).getStart();
            int displayEnd = DateUtils.offsetDate(series.get(series.size() - 1).getEnd(), 2);

            _leagueDao.addLeague(cost, name, code, leagueData.getClass().getName(), parameters, leagueStart, displayEnd);

            _leagueService.clearCache();

            responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private void previewConstructedLeague(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateLeagueAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String start = getFormParameterSafely(postDecoder, "start");
            String collectionType = getFormParameterSafely(postDecoder, "collectionType");
            String prizeMultiplier = getFormParameterSafely(postDecoder, "prizeMultiplier");
            List<String> formats = getFormMultipleParametersSafely(postDecoder, "format[]");
            List<String> seriesDurations = getFormMultipleParametersSafely(postDecoder, "seriesDuration[]");
            List<String> maxMatches = getFormMultipleParametersSafely(postDecoder, "maxMatches[]");
            String name = getFormParameterSafely(postDecoder, "name");
            String costStr = getFormParameterSafely(postDecoder, "cost");

            if(start == null || start.trim().isEmpty()
                    ||collectionType == null || collectionType.trim().isEmpty()
                    ||prizeMultiplier == null || prizeMultiplier.trim().isEmpty()
                    ||name == null || name.trim().isEmpty()
                    ||costStr == null || costStr.trim().isEmpty()) {
                throw new HttpProcessingException(400);
            }

            if(formats.size() != seriesDurations.size() || formats.size() != maxMatches.size())
                throw new HttpProcessingException(400);

            int cost = Integer.parseInt(costStr);

            StringJoiner sj = new StringJoiner(",");
            sj.add(start).add(collectionType).add(prizeMultiplier).add("1").add(Integer.toString(formats.size()));
            for (int i = 0; i < formats.size(); i++)
                sj.add(formats.get(i)).add(seriesDurations.get(i)).add(maxMatches.get(i));
            String parameters = sj.toString();

            LeagueData leagueData = new NewConstructedLeagueData(_cardLibrary, _formatLibrary, parameters);

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            Document doc = documentBuilder.newDocument();

            final List<LeagueSeriesData> series = leagueData.getSeries();

            int end = series.get(series.size() - 1).getEnd();

            Element leagueElem = doc.createElement("league");

            leagueElem.setAttribute("name", name);
            leagueElem.setAttribute("cost", String.valueOf(cost));
            leagueElem.setAttribute("start", String.valueOf(series.get(0).getStart()));
            leagueElem.setAttribute("end", String.valueOf(end));

            for (LeagueSeriesData serie : series) {
                Element seriesElem = doc.createElement("series");
                seriesElem.setAttribute("type", serie.getName());
                seriesElem.setAttribute("maxMatches", String.valueOf(serie.getMaxMatches()));
                seriesElem.setAttribute("start", String.valueOf(serie.getStart()));
                seriesElem.setAttribute("end", String.valueOf(serie.getEnd()));
                seriesElem.setAttribute("format", serie.getFormat().getName());
                seriesElem.setAttribute("collection", serie.getCollectionType().getFullName());
                seriesElem.setAttribute("limited", String.valueOf(serie.isLimited()));

                leagueElem.appendChild(seriesElem);
            }

            doc.appendChild(leagueElem);

            responseWriter.writeXmlResponse(doc);
        } finally {
            postDecoder.destroy();
        }
    }

    private void addSoloDraftLeague(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateLeagueAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String format = getFormParameterSafely(postDecoder, "format");
            String start = getFormParameterSafely(postDecoder, "start");
            String seriesDuration = getFormParameterSafely(postDecoder, "seriesDuration");
            String maxMatches = getFormParameterSafely(postDecoder, "maxMatches");
            String name = getFormParameterSafely(postDecoder, "name");
            String costStr = getFormParameterSafely(postDecoder, "cost");

            if(format == null || format.trim().isEmpty()
                    ||start == null || start.trim().isEmpty()
                    ||seriesDuration == null || seriesDuration.trim().isEmpty()
                    ||maxMatches == null || maxMatches.trim().isEmpty()
                    ||name == null || name.trim().isEmpty()
                    ||costStr == null || costStr.trim().isEmpty()) {
                throw new HttpProcessingException(400);
            }

            int cost = Integer.parseInt(costStr);

            String code = String.valueOf(System.currentTimeMillis());

            String parameters = format + "," + start + "," + seriesDuration + "," + maxMatches + "," + code + "," + name;
            LeagueData leagueData = new SoloDraftLeagueData(_cardLibrary, _formatLibrary, _soloDraftDefinitions, parameters);
            List<LeagueSeriesData> series = leagueData.getSeries();
            int leagueStart = series.get(0).getStart();
            int displayEnd = DateUtils.offsetDate(series.get(series.size() - 1).getEnd(), 2);

            _leagueDao.addLeague(cost, name, code, leagueData.getClass().getName(), parameters, leagueStart, displayEnd);

            _leagueService.clearCache();

            responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private void previewSoloDraftLeague(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateLeagueAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String format = getFormParameterSafely(postDecoder, "format");
            String start = getFormParameterSafely(postDecoder, "start");
            String seriesDuration = getFormParameterSafely(postDecoder, "seriesDuration");
            String maxMatches = getFormParameterSafely(postDecoder, "maxMatches");
            String name = getFormParameterSafely(postDecoder, "name");
            String costStr = getFormParameterSafely(postDecoder, "cost");

            if(format == null || format.trim().isEmpty()
                    ||start == null || start.trim().isEmpty()
                    ||seriesDuration == null || seriesDuration.trim().isEmpty()
                    ||maxMatches == null || maxMatches.trim().isEmpty()
                    ||name == null || name.trim().isEmpty()
                    ||costStr == null || costStr.trim().isEmpty()) {
                throw new HttpProcessingException(400);
            }

            int cost = Integer.parseInt(costStr);

            String code = String.valueOf(System.currentTimeMillis());

            String parameters = format + "," + start + "," + seriesDuration + "," + maxMatches + "," + code + "," + name;
            LeagueData leagueData = new SoloDraftLeagueData(_cardLibrary,  _formatLibrary, _soloDraftDefinitions, parameters);

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            Document doc = documentBuilder.newDocument();

            final List<LeagueSeriesData> series = leagueData.getSeries();

            int end = series.get(series.size() - 1).getEnd();

            Element leagueElem = doc.createElement("league");

            leagueElem.setAttribute("name", name);
            leagueElem.setAttribute("cost", String.valueOf(cost));
            leagueElem.setAttribute("start", String.valueOf(series.get(0).getStart()));
            leagueElem.setAttribute("end", String.valueOf(end));

            for (LeagueSeriesData serie : series) {
                Element seriesElem = doc.createElement("series");
                seriesElem.setAttribute("type", serie.getName());
                seriesElem.setAttribute("maxMatches", String.valueOf(serie.getMaxMatches()));
                seriesElem.setAttribute("start", String.valueOf(serie.getStart()));
                seriesElem.setAttribute("end", String.valueOf(serie.getEnd()));
                seriesElem.setAttribute("format", serie.getFormat().getName());
                seriesElem.setAttribute("collection", serie.getCollectionType().getFullName());
                seriesElem.setAttribute("limited", String.valueOf(serie.isLimited()));

                leagueElem.appendChild(seriesElem);
            }

            doc.appendChild(leagueElem);

            responseWriter.writeXmlResponse(doc);
        } finally {
            postDecoder.destroy();
        }
    }

    private void addSealedLeague(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateLeagueAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String format = getFormParameterSafely(postDecoder, "format");
            String start = getFormParameterSafely(postDecoder, "start");
            String seriesDuration = getFormParameterSafely(postDecoder, "seriesDuration");
            String maxMatches = getFormParameterSafely(postDecoder, "maxMatches");
            String name = getFormParameterSafely(postDecoder, "name");
            String costStr = getFormParameterSafely(postDecoder, "cost");

            if(format == null || format.trim().isEmpty()
                    ||start == null || start.trim().isEmpty()
                    ||seriesDuration == null || seriesDuration.trim().isEmpty()
                    ||maxMatches == null || maxMatches.trim().isEmpty()
                    ||name == null || name.trim().isEmpty()
                    ||costStr == null || costStr.trim().isEmpty()) {
                throw new HttpProcessingException(400);
            }

            int cost = Integer.parseInt(costStr);

            String code = String.valueOf(System.currentTimeMillis());

            String parameters = _formatLibrary.GetSealedTemplate(format).GetID() + "," + start + "," + seriesDuration + "," + maxMatches + "," + code + "," + name;
            LeagueData leagueData = new NewSealedLeagueData(_cardLibrary, _formatLibrary, parameters);
            List<LeagueSeriesData> series = leagueData.getSeries();
            int leagueStart = series.get(0).getStart();
            int displayEnd = DateUtils.offsetDate(series.get(series.size() - 1).getEnd(), 2);

            _leagueDao.addLeague(cost, name, code, leagueData.getClass().getName(), parameters, leagueStart, displayEnd);

            _leagueService.clearCache();

            responseWriter.writeHtmlResponse("OK");
        }
        catch (RuntimeException ex) {
            logHttpError(_log, 500, request.uri(), ex);
            throw new HttpProcessingException(500);
        }
        finally {
            postDecoder.destroy();
        }
    }

    private void previewSealedLeague(HttpRequest request, ResponseWriter responseWriter) throws Exception {
        validateLeagueAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String format = getFormParameterSafely(postDecoder, "format");
            String start = getFormParameterSafely(postDecoder, "start");
            String seriesDuration = getFormParameterSafely(postDecoder, "seriesDuration");
            String maxMatches = getFormParameterSafely(postDecoder, "maxMatches");
            String name = getFormParameterSafely(postDecoder, "name");
            String costStr = getFormParameterSafely(postDecoder, "cost");

            if(format == null || format.trim().isEmpty()
                ||start == null || start.trim().isEmpty()
                ||seriesDuration == null || seriesDuration.trim().isEmpty()
                ||maxMatches == null || maxMatches.trim().isEmpty()
                ||name == null || name.trim().isEmpty()
                ||costStr == null || costStr.trim().isEmpty()) {
                throw new HttpProcessingException(400);
            }

            int cost = Integer.parseInt(costStr);

            String code = String.valueOf(System.currentTimeMillis());

            String parameters = format + "," + start + "," + seriesDuration + "," + maxMatches + "," + code + "," + name;
            LeagueData leagueData = new NewSealedLeagueData(_cardLibrary, _formatLibrary, parameters);

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            Document doc = documentBuilder.newDocument();

            final List<LeagueSeriesData> series = leagueData.getSeries();

            int end = series.get(series.size() - 1).getEnd();

            Element leagueElem = doc.createElement("league");

            leagueElem.setAttribute("name", name);
            leagueElem.setAttribute("cost", String.valueOf(cost));
            leagueElem.setAttribute("start", String.valueOf(series.get(0).getStart()));
            leagueElem.setAttribute("end", String.valueOf(end));

            for (LeagueSeriesData serie : series) {
                Element seriesElem = doc.createElement("series");
                seriesElem.setAttribute("type", serie.getName());
                seriesElem.setAttribute("maxMatches", String.valueOf(serie.getMaxMatches()));
                seriesElem.setAttribute("start", String.valueOf(serie.getStart()));
                seriesElem.setAttribute("end", String.valueOf(serie.getEnd()));
                seriesElem.setAttribute("format", serie.getFormat().getName());
                seriesElem.setAttribute("collection", serie.getCollectionType().getFullName());
                seriesElem.setAttribute("limited", String.valueOf(serie.isLimited()));

                leagueElem.appendChild(seriesElem);
            }

            doc.appendChild(leagueElem);

            responseWriter.writeXmlResponse(doc);
        } finally {
            postDecoder.destroy();
        }
    }

    private void getMotd(HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException {
        validateAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String motd = _hallServer.getMOTD();

            if(motd != null) {
                responseWriter.writeJsonResponse(motd.replace("\n", "<br>"));
            }
        } finally {
            postDecoder.destroy();
        }
    }

    private void setMotd(HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException, IOException {
        validateAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            String motd = getFormParameterSafely(postDecoder, "motd");

            _hallServer.setMOTD(motd);

            responseWriter.writeHtmlResponse("OK");
        } finally {
            postDecoder.destroy();
        }
    }

    private void shutdown(HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException {
        validateAdmin(request);

        HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
        try {
            boolean shutdown = Boolean.parseBoolean(getFormParameterSafely(postDecoder, "shutdown"));

            _hallServer.setShutdown(shutdown);

            responseWriter.writeHtmlResponse("OK");
        } catch (Exception e) {
            _log.error("Error response for " + request.uri(), e);
            responseWriter.writeHtmlResponse("Error handling request");
        } finally {
            postDecoder.destroy();
        }
    }

    private void reloadCards(HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException, InterruptedException {
        validateAdmin(request);

        _chatServer.sendSystemMessageToAllChatRooms("@everyone Server is reloading card definitions.  This will impact game speed until it is complete.");

        Thread.sleep(6000);
        _cardLibrary.reloadAllDefinitions();

        _productLibrary.ReloadPacks();

        _formatLibrary.ReloadFormats();
        _formatLibrary.ReloadSealedTemplates();

        _chatServer.sendSystemMessageToAllChatRooms("@everyone Card definition reload complete.  If you are mid-game and you notice any oddities, reload the page and please let the mod team know in the game hall ASAP if the problem doesn't go away.");

        responseWriter.writeHtmlResponse("OK");
    }

    private void clearCache(HttpRequest request, ResponseWriter responseWriter) throws HttpProcessingException {
        validateAdmin(request);

        _leagueService.clearCache();
        _tournamentService.clearCache();

        int before = _cacheManager.getTotalCount();

        _cacheManager.clearCaches();

        int after = _cacheManager.getTotalCount();

        responseWriter.writeHtmlResponse("Before: " + before + "<br><br>After: " + after);
    }

    private void validateAdmin(HttpRequest request) throws HttpProcessingException {
        User player = getResourceOwnerSafely(request, null);

        if (!player.hasType(User.Type.ADMIN))
            throw new HttpProcessingException(403);
    }

    private void validateLeagueAdmin(HttpRequest request) throws HttpProcessingException {
        User player = getResourceOwnerSafely(request, null);

        if (!player.hasType(User.Type.LEAGUE_ADMIN))
            throw new HttpProcessingException(403);
    }
}
