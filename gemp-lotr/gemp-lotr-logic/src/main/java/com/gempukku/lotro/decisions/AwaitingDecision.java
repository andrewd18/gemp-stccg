package com.gempukku.lotro.decisions;

import java.util.Map;

public interface AwaitingDecision {
    int getAwaitingDecisionId();

    String getText();

    AwaitingDecisionType getDecisionType();

    Map<String, String[]> getDecisionParameters();

    void decisionMade(String result) throws DecisionResultInvalidException;
}
