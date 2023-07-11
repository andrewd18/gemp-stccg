package com.gempukku.lotro.db;

import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.cards.LotroDeck;

import java.util.Map;
import java.util.Set;

public interface DeckDAO {
    public LotroDeck getDeckForPlayer(Player player, String name);

    public void saveDeckForPlayer(Player player, String name, String target_format, String notes, LotroDeck deck);

    public void deleteDeckForPlayer(Player player, String name);

    public LotroDeck renameDeck(Player player, String oldName, String newName);

    public Set<Map.Entry<String, String>> getPlayerDeckNames(Player player);

    public LotroDeck buildDeckFromContents(String deckName, String contents, String target_format, String notes);
}
