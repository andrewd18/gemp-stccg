package com.gempukku.lotro.cards;

public class InvalidCardDefinitionException extends Exception {
    public InvalidCardDefinitionException(String message) {
        super(message);
    }

    public InvalidCardDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
