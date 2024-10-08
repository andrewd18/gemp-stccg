package com.gempukku.stccg.packs;

import com.gempukku.stccg.cards.PackBox;
import com.gempukku.stccg.cards.CardCollection;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class LeagueStarterBox implements PackBox {
    @Override
    public List<CardCollection.Item> openPack() {
        int starter = ThreadLocalRandom.current().nextInt(6);
        return openPack(starter);
    }

    public List<CardCollection.Item> openPack(int starter) {
        List<CardCollection.Item> result = new LinkedList<>();
        if (starter == 0) {
            result.add(CardCollection.Item.createItem("FotR - Gandalf Starter", 1));
        } else if (starter == 1) {
            result.add(CardCollection.Item.createItem("FotR - Aragorn Starter", 1));
        } else if (starter == 2) {
            result.add(CardCollection.Item.createItem("MoM - Gandalf Starter", 1));
        } else if (starter == 3) {
            result.add(CardCollection.Item.createItem("MoM - Gimli Starter", 1));
        } else if (starter == 4) {
            result.add(CardCollection.Item.createItem("RotEL - Boromir Starter", 1));
        } else if (starter == 5) {
            result.add(CardCollection.Item.createItem("RotEL - Legolas Starter", 1));
        }

        //result.add(CardCollection.Item.createItem("FOTR League Random Starters", 1));
        result.add(CardCollection.Item.createItem("FotR - Booster", 2));
        result.add(CardCollection.Item.createItem("MoM - Booster", 2));
        result.add(CardCollection.Item.createItem("RotEL - Booster", 2));

        // Arwen
        result.add(CardCollection.Item.createItem("3_7", 1));
        // Boromir
        result.add(CardCollection.Item.createItem("1_97", 1));
        // Gimli
        result.add(CardCollection.Item.createItem("1_12", 1));
        // Legolas
        result.add(CardCollection.Item.createItem("1_51", 1));
        // Merry
        result.add(CardCollection.Item.createItem("1_303", 1));
        // Pippin
        result.add(CardCollection.Item.createItem("1_306", 1));
        // Sam
        result.add(CardCollection.Item.createItem("1_311", 1));

        // The Balrog
        result.add(CardCollection.Item.createItem("0_10", 1));

        return result;
    }

    @Override
    public List<String> GetAllOptions() {
        return openPack(1).stream().map(CardCollection.Item::getBlueprintId).toList();
    }
}
