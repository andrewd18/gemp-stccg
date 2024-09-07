package com.gempukku.stccg.common.filterable.lotr;

import com.gempukku.stccg.common.filterable.Filterable;

public enum Keyword implements Filterable {
    SUPPORT_AREA("Support Area", true),

    SKIRMISH("Skirmish"), FELLOWSHIP("Fellowship"), RESPONSE("Response"), MANEUVER("Maneuver"), ARCHERY("Archery"), SHADOW("Shadow"), ASSIGNMENT("Assignment"), REGROUP("Regroup"),

    CAN_START_WITH_RING("Can-bear-ring", false),

    RING_BOUND("Ring-Bound", true),

    ENDURING("Enduring", true), ROAMING("Roaming", true), MOUNTED("Mounted", false), TWILIGHT("Twilight", true), HUNTER("Hunter", true, true),

    WEATHER("Weather", true), TALE("Tale", true), SPELL("Spell", true), SEARCH("Search", true), STEALTH("Stealth", true), TENTACLE("Tentacle", true),

    RIVER("River", true, false, true), PLAINS("Plains", true, false, true), UNDERGROUND("Underground", true, false, true),
    SANCTUARY("Sanctuary", true, false, true), FOREST("Forest", true, false, true), MARSH("Marsh", true, false, true),
    MOUNTAIN("Mountain", true, false, true), BATTLEGROUND("Battleground", true, false, true), DWELLING("Dwelling", true, false, true),

    PIPEWEED("Pipeweed", true),

    LOTHLORIEN("Lothlorien", true, false, false), RIVENDELL("Rivendell", true, false, false), BREE("Bree", true, false, false),
    EDORAS("Edoras", true, false, false), SHIRE("Shire", true, false, false),

    DAMAGE("Damage", true, true), DEFENDER("Defender", true, true), AMBUSH("Ambush", true, true), FIERCE("Fierce", true), ARCHER("Archer", true),
    UNHASTY("Unhasty", true), MUSTER("Muster", true), TOIL("Toil", true, true), LURKER("Lurker", true), CUNNING("Cunning", true),

    RANGER("Ranger", true), TRACKER("Tracker", true), VILLAGER("Villager", true), MACHINE("Machine", true), ENGINE("Engine", true),
    SOUTHRON("Southron", true), EASTERLING("Easterling", true), VALIANT("Valiant", true), KNIGHT("Knight", true), FORTIFICATION("Fortification", true),
    WARG_RIDER("Warg-rider", true), BESIEGER("Besieger", true), CORSAIR("Corsair", true),
	
	//Additional Hobbit Draft keywords
	WISE("Wise", true), BURGLAR("Burglar", true),

    //PC Keywords
    CONCEALED("Concealed", true), EXPOSED("Exposed", true);

    private final String _humanReadable;
    private final boolean _infoDisplayable;
    private final boolean _multiples;

    Keyword(String humanReadable) {
        this(humanReadable, false);
    }

    Keyword(String humanReadable, boolean infoDisplayable) {
        this(humanReadable, infoDisplayable, false);
    }

    Keyword(String humanReadable, boolean infoDisplayable, boolean multiples) {
        this(humanReadable, infoDisplayable, multiples, false);
    }

    Keyword(String humanReadable, boolean infoDisplayable, boolean multiples, boolean terrain) {
        _humanReadable = humanReadable;
        _infoDisplayable = infoDisplayable;
        _multiples = multiples;
    }

    public String getHumanReadable() {
        return _humanReadable;
    }

    public String getHumanReadableGeneric() {
        if(_multiples)
            return _humanReadable + " bonus";

        return _humanReadable;
    }

    public boolean isInfoDisplayable() {
        return _infoDisplayable;
    }

    public boolean isMultiples() {
        return _multiples;
    }

}
