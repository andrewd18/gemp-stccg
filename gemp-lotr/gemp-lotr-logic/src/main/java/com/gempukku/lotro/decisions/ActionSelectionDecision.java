package com.gempukku.lotro.decisions;

import com.gempukku.lotro.actions.Action;
import com.gempukku.lotro.cards.CardNotFoundException;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.game.DefaultGame;

import java.util.List;
import java.util.Objects;

public abstract class ActionSelectionDecision extends AbstractAwaitingDecision {
    private final DefaultGame _game;
    private final List<? extends Action> _actions;

    public ActionSelectionDecision(DefaultGame game, int decisionId, String text, List<? extends Action> actions) {
        super(decisionId, text, AwaitingDecisionType.ACTION_CHOICE);
        _game = game;
        _actions = actions;

        setParam("actionId", getActionIds(actions));
        setParam("blueprintId", getBlueprintIds(actions));
        setParam("imageUrl", getImageUrls(actions));
        setParam("actionText", getActionTexts(actions));
    }

    private String[] getActionIds(List<? extends Action> actions) {
        String[] result = new String[actions.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = String.valueOf(i);
        return result;
    }

    private String[] getBlueprintIds(List<? extends Action> actions) {
        String[] result = new String[actions.size()];
        for (int i = 0; i < result.length; i++) {
            LotroPhysicalCard physicalCard = actions.get(i).getActionAttachedToCard();
            if (physicalCard != null)
                result[i] = String.valueOf(physicalCard.getBlueprintId());
            else
                result[i] = "rules";
        }
        return result;
    }

    private String[] getImageUrls(List<? extends Action> actions) {
        String[] blueprints = getBlueprintIds(actions);
        String[] images = new String[blueprints.length];
        for (int i = 0; i < blueprints.length; i++) {
            if (Objects.equals(blueprints[i], "rules")) {
                images[i] = "rules";
            } else {
                try {
                    images[i] = _game.getBlueprintLibrary().getLotroCardBlueprint(blueprints[i]).getImageUrl();
                } catch (CardNotFoundException exp) {
                    throw new RuntimeException("ActionSelectionDecision unable to find image URLs for all card blueprints", exp);
                }
            }
        }
        return images;
    }

    private String[] getActionTexts(List<? extends Action> actions) {
        String[] result = new String[actions.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = actions.get(i).getText(_game);
        return result;
    }

    protected Action getSelectedAction(String result) throws DecisionResultInvalidException {
        if (result.equals(""))
            throw new DecisionResultInvalidException();

        try {
            int actionIndex = Integer.parseInt(result);
            if (actionIndex < 0 || actionIndex >= _actions.size())
                throw new DecisionResultInvalidException();

            return _actions.get(actionIndex);
        } catch (NumberFormatException exp) {
            throw new DecisionResultInvalidException();
        }
    }
}
