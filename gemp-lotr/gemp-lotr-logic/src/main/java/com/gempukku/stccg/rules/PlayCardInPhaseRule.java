package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Keyword;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Side;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.rules.lotronly.LotroGameUtils;
import com.gempukku.stccg.rules.lotronly.LotroPlayUtils;

import java.util.LinkedList;
import java.util.List;

public class PlayCardInPhaseRule {
    private final DefaultActionsEnvironment actionsEnvironment;

    public PlayCardInPhaseRule(DefaultActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends Action> getPhaseActions(String playerId, DefaultGame game) {
                        final Phase phase = game.getGameState().getCurrentPhase();
                        Side side = LotroGameUtils.getSide(game, playerId);
                        if (phase == Phase.FELLOWSHIP) {
                            if (LotroGameUtils.isFP(game, playerId)) {
                                List<Action> result = new LinkedList<>();
                                for (PhysicalCard card : Filters.filter(game.getGameState().getHand(playerId), game, side,
                                        Filters.or(Filters.and(CardType.EVENT, Keyword.FELLOWSHIP), Filters.not(CardType.EVENT)))) {
                                    if (LotroPlayUtils.checkPlayRequirements(game, card, Filters.any, 0, 0, false, false, true))
                                        result.add(PlayUtils.getPlayCardAction(game, card, 0, Filters.any, false));
                                }
                                return result;
                            }
                        } else if (phase == Phase.SHADOW) {
                            if (LotroGameUtils.isShadow(game, playerId)) {
                                List<Action> result = new LinkedList<>();
                                for (PhysicalCard card : Filters.filter(game.getGameState().getHand(playerId), game, side,
                                        Filters.or(Filters.and(CardType.EVENT, Keyword.SHADOW), Filters.not(CardType.EVENT)))) {
                                    if (LotroPlayUtils.checkPlayRequirements(game, card, Filters.any, 0, 0, false, false, true))
                                        result.add(PlayUtils.getPlayCardAction(game, card, 0, Filters.any, false));
                                }
                                return result;
                            }
                        } else {
                            final Keyword phaseKeyword = PlayUtils.PhaseKeywordMap.get(game.getGameState().getCurrentPhase());
                            if (phaseKeyword != null) {
                                List<Action> result = new LinkedList<>();
                                for (PhysicalCard card : Filters.filter(game.getGameState().getHand(playerId), game, side,
                                        Filters.and(CardType.EVENT, phaseKeyword))) {
                                    if (LotroPlayUtils.checkPlayRequirements(game, card, Filters.any, 0, 0, false, false, true))
                                        result.add(PlayUtils.getPlayCardAction(game, card, 0, Filters.any, false));
                                }
                                return result;
                            }
                        }

                        return null;
                    }
                }
        );
    }
}
