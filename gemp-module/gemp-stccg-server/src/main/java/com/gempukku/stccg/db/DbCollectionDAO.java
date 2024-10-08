package com.gempukku.stccg.db;

import com.gempukku.stccg.collection.CollectionSerializer;
import com.gempukku.stccg.common.DBDefs;
import com.gempukku.stccg.cards.CardCollection;
import com.gempukku.stccg.collection.DefaultCardCollection;
import com.gempukku.stccg.collection.MutableCardCollection;
import org.json.simple.JSONObject;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DbCollectionDAO implements CollectionDAO {
    private final DbAccess _dbAccess;
    private final CollectionSerializer _collectionSerializer;

    public DbCollectionDAO(DbAccess dbAccess, CollectionSerializer collectionSerializer) {
        _dbAccess = dbAccess;
        _collectionSerializer = collectionSerializer;
    }

    public Map<Integer, CardCollection> getPlayerCollectionsByType(String type) throws IOException {
        Map<Integer, CardCollection> result = new HashMap<>();
        for(var coll : getCollectionInfosByType(type))
            result.put(coll.player_id,
                    _collectionSerializer.deserializeCollection(coll, extractCollectionEntries(coll.id)));
        return result;
    }

    public CardCollection getPlayerCollection(int playerId, String type) throws SQLException, IOException {

        var collection = getCollectionInfo(playerId, type);
        if(collection == null)
            return null;

        var entries = extractCollectionEntries(collection.id);

        return _collectionSerializer.deserializeCollection(collection, entries);
    }

    private List<DBDefs.CollectionEntry> extractCollectionEntries(int collectionID) {
        try {
            Sql2o db = new Sql2o(_dbAccess.getDataSource());

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT 
                            collection_id, 
                            quantity, 
                            product_type, 
                            product_variant, 
                            product, 
                            source, 
                            created_date, 
                            modified_date, 
                            notes
                        FROM gemp_db.collection_entries
                        WHERE collection_id = :collID;
                                                
                        """;

                return conn.createQuery(sql)
                        .addParameter("collID", collectionID)
                        .executeAndFetch(DBDefs.CollectionEntry.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve collection entries", ex);
        }
    }

    @Override
    public List<DBDefs.Collection> getAllCollectionsForPlayer(int playerId) {

        try {
            Sql2o db = new Sql2o(_dbAccess.getDataSource());

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT
                            id, player_id, type, extra_info
                        FROM collection
                        WHERE player_id = :playerID
                        """;

                return conn.createQuery(sql)
                        .addParameter("playerID", playerId)
                        .executeAndFetch(DBDefs.Collection.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve collection types", ex);
        }
    }

    @Override
    public DBDefs.Collection getCollectionInfo(int playerId, String type) {

        try {
            Sql2o db = new Sql2o(_dbAccess.getDataSource());

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT
                            id, player_id, type, extra_info
                        FROM collection
                        WHERE type = :type
                            AND player_id = :playerID
                        LIMIT 1;
                        """;
                List<DBDefs.Collection> result = conn.createQuery(sql)
                        .addParameter("type", type)
                        .addParameter("playerID", playerId)
                        .executeAndFetch(DBDefs.Collection.class);

                return result.stream().findFirst().orElse(null);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve collection info", ex);
        }
    }

    @Override
    public DBDefs.Collection getCollectionInfo(int collectionID) {

        try {
            Sql2o db = new Sql2o(_dbAccess.getDataSource());

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT
                            id, player_id, type, extra_info
                        FROM collection
                        WHERE collection_id = :collectionID
                        LIMIT 1;
                        """;
                List<DBDefs.Collection> result = conn.createQuery(sql)
                        .addParameter("collectionID", collectionID)
                        .executeAndFetch(DBDefs.Collection.class);

                return result.stream().findFirst().orElse(null);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve collection info", ex);
        }
    }

    @Override
    public List<DBDefs.Collection> getCollectionInfosByType(String type) {

        try {
            Sql2o db = new Sql2o(_dbAccess.getDataSource());

            try (org.sql2o.Connection conn = db.open()) {
                String sql = """
                        SELECT
                            id, player_id, type, extra_info
                        FROM collection
                        WHERE type = :type;
                        """;

                return conn.createQuery(sql)
                        .addParameter("type", type)
                        .executeAndFetch(DBDefs.Collection.class);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve collection database definitions", ex);
        }
    }

    public int getCollectionID(int playerId, String type)  {
        var coll = getCollectionInfo(playerId,  type);
        if(coll == null)
            return -1;

        return coll.id;
    }


    public void overwriteCollectionContents(int playerId, String type, CardCollection collection, String source) {
        String sql = """
                        INSERT INTO collection_entries(collection_id, quantity, product_type, product, source)
                        VALUES (:collid, :quantity, :type, :product, :source)
                        ON DUPLICATE KEY UPDATE quantity = :quantity, source = :source;
                        """;
        String error = "Unable to update product via upsert into collection_entries.";
        updateCollectionContents(playerId, type, collection, source, sql, error);
    }

    public void addToCollectionContents(int playerId, String type, CardCollection collection, String source) {
        String sql = """
                        INSERT INTO collection_entries(collection_id, quantity, product_type, product, source)
                        VALUES (:collid, :quantity, :type, :product, :source)
                        ON DUPLICATE KEY UPDATE quantity = quantity + :quantity, source = :source;
                        """;
        String error = "Unable to add product via upsert into collection_entries.";
        updateCollectionContents(playerId, type, collection, source, sql, error);
    }

    public void removeFromCollectionContents(int playerId, String type, CardCollection collection, String source) {
        String sql = """
                        INSERT INTO collection_entries(collection_id, quantity, product_type, product, source)
                        VALUES (:collid, :quantity, :type, :product, :source)
                        ON DUPLICATE KEY UPDATE quantity = GREATEST(quantity - :quantity, 0), source = :source;
                        """;
        String error = "Unable to remove product via upsert into collection_entries.";
        updateCollectionContents(playerId, type, collection, source, sql, error);
    }

    public void convertCollection(int playerId, String type) throws SQLException, IOException {
        MutableCardCollection oldCollection = getOldPlayerCollection(playerId, type);
        var oldInfo = new HashMap<>(oldCollection.getExtraInformation());
        oldInfo.put(DefaultCardCollection.CurrencyKey, oldCollection.getCurrency());
        oldCollection.setExtraInformation(oldInfo);
        overwriteCollectionContents(playerId, type, oldCollection, "Initial Convert");
    }

    public MutableCardCollection getOldPlayerCollection(int playerId, String type) throws SQLException, IOException {
        try (Connection connection = _dbAccess.getDataSource().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("select collection from collection where player_id=? and type=?")) {
                statement.setInt(1, playerId);
                statement.setString(2, type);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        Blob blob = rs.getBlob(1);
                        return extractCollectionAndClose(blob);
                    } else {
                        return null;
                    }
                }
            }
        }
    }

    private MutableCardCollection extractCollectionAndClose(Blob blob) throws SQLException, IOException {
        try {
            try (InputStream inputStream = blob.getBinaryStream()) {
                return _collectionSerializer.deserializeCollection(inputStream);
            }
        } finally {
            blob.free();
        }
    }

    @Override
    public void updateCollectionInfo(int playerId, String type, Map<String, Object> extraInformation) {
        upsertCollectionInfo(playerId, type, extraInformation);
    }


    //TODO:
    // + Convert currency to an extra info entry
    // + add data field to the original collection table to hold the extra info as json
    // + create player-looping function to convert all collections and see if it blows up via test
    // + add CollectionsManager mirror functions to read/writing into the new table
    // + write unit tests to convert and compare a bajillion collections
    // + sunset the old collection handling functions
    // ~ test a: draft, new art card reward, my cards reward, pack openings
    // - write script to back up db and delete the old binary blob column
    // - write script to convert all leagues to use IDs instead of names
    private void updateCollectionContents(int playerId, String type, CardCollection collection, String source, String sql, String error) {
        if(getCollectionID(playerId, type) <= 0) {
            upsertCollectionInfo(playerId, type, collection.getExtraInformation());
        }
        int collID = getCollectionID(playerId, type);

        try {
            Sql2o db = new Sql2o(_dbAccess.getDataSource());

            try (org.sql2o.Connection conn = db.beginTransaction()) {
                Query query = conn.createQuery(sql, true);
                int i = 0;
                //TODO: maybe detect when the batch is 1000 entries long and execute then to prevent errors
                for(var card : collection.getAll()) {
                    query.addParameter("collid", collID)
                            .addParameter("quantity", card.getCount())
                            .addParameter("type", card.getType())
                            .addParameter("product", card.getBlueprintId())
                            .addParameter("source", source)
                            .addToBatch();
                }
                query.executeBatch();
                conn.commit();
            }
        } catch (Exception ex) {
            throw new RuntimeException(error, ex);
        }
    }

    private void upsertCollectionInfo(int playerId, String type, Map<String, Object> extraInformation) {
        String sql = """
                        INSERT INTO collection(player_id, type, extra_info)
                        VALUES (:playerId, :type, :extraInfo)
                        ON DUPLICATE KEY UPDATE extra_info = :extraInfo;
                        """;
        String json;
        try {
            Sql2o db = new Sql2o(_dbAccess.getDataSource());

            var jsonObj = new JSONObject();
            jsonObj.putAll(extraInformation);
            json = jsonObj.toJSONString();

            try (org.sql2o.Connection conn = db.beginTransaction()) {
                Query query = conn.createQuery(sql, true);
                query.addParameter("playerId", playerId)
                        .addParameter("type", type)
                        .addParameter("extraInfo", json);
                query.executeUpdate();
                conn.commit();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to upsert collection", ex);
        }
    }


}
