package com.gempukku.lotro.decisions;

import java.util.List;

public abstract class MultipleChoiceAwaitingDecision extends AbstractAwaitingDecision {
    private final String[] _possibleResults;

    public MultipleChoiceAwaitingDecision(int id, String text, String[] possibleResults) {
        super(id, text, AwaitingDecisionType.MULTIPLE_CHOICE);
        _possibleResults = possibleResults;
        setParam("results", _possibleResults);
    }
    public MultipleChoiceAwaitingDecision(int id, String text, List<String> possibleResults) {
        super(id, text, AwaitingDecisionType.MULTIPLE_CHOICE);
        _possibleResults = possibleResults.toArray(new String[0]);
        setParam("results", _possibleResults);
    }

    protected abstract void validDecisionMade(int index, String result);

    @Override
    public final void decisionMade(String result) throws DecisionResultInvalidException {
        if (result == null)
            throw new DecisionResultInvalidException();

        int index;
        try {
            index = Integer.parseInt(result);
        } catch (NumberFormatException exp) {
            throw new DecisionResultInvalidException("Unknown response number");
        }
        validDecisionMade(index, _possibleResults[index]);
    }
}
