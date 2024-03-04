package com.gempukku.stccg.common.filterable;

public enum PersonnelName implements Filterable {
    GOWRON("Gowron"),
    JAMES_T_KIRK("James T. Kirk"),
    JEAN_LUC_PICARD ("Jean-Luc Picard"),
    MAROUK("Marouk"),
    TEBOK("Tebok");

    private final String _humanReadable;

    PersonnelName(String humanReadable) {
        _humanReadable = humanReadable;
    }

    public String getHumanReadable() { return _humanReadable; }

}