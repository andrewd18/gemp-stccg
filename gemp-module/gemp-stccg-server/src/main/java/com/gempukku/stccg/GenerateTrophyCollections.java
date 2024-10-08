package com.gempukku.stccg;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.collection.CollectionSerializer;
import com.gempukku.stccg.db.DbAccess;
import com.gempukku.stccg.db.DbCollectionDAO;
import com.gempukku.stccg.db.vo.CollectionType;
import com.gempukku.stccg.cards.CardCollection;
import com.gempukku.stccg.collection.DefaultCardCollection;
import com.gempukku.stccg.cards.SetDefinition;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class GenerateTrophyCollections {
    public static void main(String[] args) throws SQLException, IOException {
        CardBlueprintLibrary library = new CardBlueprintLibrary();
        DbAccess dbAccess = new DbAccess();

        DbCollectionDAO collections = new DbCollectionDAO(dbAccess, new CollectionSerializer());
        System.out.println("Getting collections");
        Map<Integer, CardCollection> collectionsByType = collections.getPlayerCollectionsByType(CollectionType.MY_CARDS.getCode());
        System.out.println("Got all collections");
        for (Map.Entry<Integer, CardCollection> playerIdToCollection : collectionsByType.entrySet()) {
            int tengwarCount = getTengwarCount(playerIdToCollection, library);
            if (tengwarCount>0) {
                System.out.println("Player id: "+playerIdToCollection.getKey()+", Tengwar count: "+tengwarCount);
                DefaultCardCollection trophies = new DefaultCardCollection();
                trophies.addItem("(S)Tengwar", tengwarCount);
                collections.overwriteCollectionContents(playerIdToCollection.getKey(), CollectionType.TROPHY.getCode(), trophies, "Trophy Generation");
            }
        }
    }

    private static int getTengwarCount(Map.Entry<Integer, CardCollection> playerIdToCollection, CardBlueprintLibrary library) {
        CardCollection collection = playerIdToCollection.getValue();
        int tengwarCount =0;
        tengwarCount+=collection.getItemCount("(S)FotR - Tengwar");
        tengwarCount+=collection.getItemCount("(S)TTT - Tengwar");
        tengwarCount+=collection.getItemCount("(S)RotK - Tengwar");
        tengwarCount+=collection.getItemCount("(S)SH - Tengwar");
        tengwarCount+=collection.getItemCount("(S)Tengwar");
        for (SetDefinition setDefinition : library.getSetDefinitions().values()) {
            for (String tengwarCard : setDefinition.getTengwarCards()) {
                tengwarCount+=collection.getItemCount(tengwarCard);
                tengwarCount+=4*collection.getItemCount(tengwarCard+"*");
            }
        }
        return tengwarCount;
    }
}
