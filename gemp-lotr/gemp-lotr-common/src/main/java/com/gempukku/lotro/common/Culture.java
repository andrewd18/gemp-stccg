package com.gempukku.lotro.common;

public enum Culture implements Filterable {
    DWARVEN("Dwarven", true), ELVEN("Elven", true), GANDALF("Gandalf", true), GOLLUM("Gollum", true), GONDOR("Gondor", true), ROHAN("Rohan", true), SHIRE("Shire", true),
    DUNLAND("Dunland", false), ISENGARD("Isengard", false), MEN("Men", false), MORIA("Moria", false), ORC("Orc", false), RAIDER("Raider", false),
    SAURON("Sauron", false), URUK_HAI("Uruk-hai", false), WRAITH("Wraith", false),
    FALLEN_REALMS("Fallen Realms", false, false),
	
	//Additional Hobbit Draft cultures
	ESGAROTH("Esgaroth", true), GUNDABAD("Gundabad", false), MIRKWOOD("Mirkwood", false), SMAUG("Smaug", false), SPIDER("Spider", false), TROLL("Troll", false);

    private final String _humanReadable;
    private final boolean _fp;
    private final boolean _official;

    Culture(String humanReadable, boolean fp) {
        this(humanReadable, fp, true);
    }

    Culture(String humanReadable, boolean fp, boolean official) {
        _humanReadable = humanReadable;
        _fp = fp;
        _official = official;
    }

    public boolean isOfficial() {
        return _official;
    }

    public String getHumanReadable() {
        return _humanReadable;
    }

    public boolean isFP() {
        return _fp;
    }

    public static Culture findCultureByHumanReadable(String humanReadable) {
        for (Culture culture : values()) {
            if (culture.getHumanReadable().equals(humanReadable))
                return culture;
        }
        return null;
    }

    public static Culture findCulture(String name) {
        String nameCaps = name.toUpperCase().replace(' ', '_').replace('-', '_');
        String nameLower = name.toLowerCase();
        if(nameLower.equals("ringwraith"))
            return WRAITH;

        for (Culture culture : values()) {
            if (culture.getHumanReadable().toLowerCase().equals(nameLower) || culture.toString().equals(nameCaps))
                return culture;
        }
        return null;
    }
}
