package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.actions.lotronly.CostToEffectAction;
import com.gempukku.lotro.actions.lotronly.SubAction;
import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.PlayoutDecisionEffect;
import com.gempukku.lotro.effects.StackActionEffect;
import com.gempukku.lotro.game.DefaultGame;
import org.json.simple.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class Choice implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player", "effects", "texts", "memorize");

        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final JSONObject[] effectArray = FieldUtils.getObjectArray(effectObject.get("effects"), "effects");
        final String[] textArray = FieldUtils.getStringArray(effectObject.get("texts"), "texts");
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");

        if (effectArray.length != textArray.length)
            throw new InvalidCardDefinitionException("Number of texts and effects does not match in choice effect");

        EffectAppender[] possibleEffectAppenders = environment.getEffectAppenderFactory().getEffectAppenders(effectArray, environment);

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        return new DelayedAppender<>() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                final String choosingPlayer = playerSource.getPlayer(actionContext);
                DefaultActionContext delegateActionContext = new DelegateActionContext(actionContext,
                        choosingPlayer, actionContext.getGame(), actionContext.getSource(),
                        actionContext.getEffectResult(), actionContext.getEffect());

                int textIndex = 0;
                List<EffectAppender> playableEffectAppenders = new LinkedList<>();
                List<String> effectTexts = new LinkedList<>();
                for (EffectAppender possibleEffectAppender : possibleEffectAppenders) {
                    if (possibleEffectAppender.isPlayableInFull(delegateActionContext)) {
                        playableEffectAppenders.add(possibleEffectAppender);
                        effectTexts.add(textArray[textIndex]);
                    }
                    textIndex++;
                }

                if (playableEffectAppenders.size() == 0) {
                    actionContext.setValueToMemory(memorize, "");
                    return null;
                }

                if (playableEffectAppenders.size() == 1) {
                    SubAction subAction = new SubAction(action);
                    playableEffectAppenders.get(0).appendEffect(cost, subAction, delegateActionContext);
                    actionContext.setValueToMemory(memorize, textArray[0]);
                    return new StackActionEffect(subAction);
                }

                SubAction subAction = new SubAction(action);
                subAction.appendCost(
                        new PlayoutDecisionEffect(choosingPlayer,
                                new MultipleChoiceAwaitingDecision(1, "Choose action to perform", effectTexts.toArray(new String[0])) {
                                    @Override
                                    protected void validDecisionMade(int index, String result) {
                                        playableEffectAppenders.get(index).appendEffect(cost, subAction, delegateActionContext);
                                        actionContext.setValueToMemory(memorize, result);
                                    }
                                }));
                return new StackActionEffect(subAction);
            }

            @Override
            public boolean isPlayableInFull(DefaultActionContext<DefaultGame> actionContext) {
                final String choosingPlayer = playerSource.getPlayer(actionContext);
                DefaultActionContext delegateActionContext = new DelegateActionContext(actionContext,
                        choosingPlayer, actionContext.getGame(), actionContext.getSource(),
                        actionContext.getEffectResult(), actionContext.getEffect());

                for (EffectAppender possibleEffectAppender : possibleEffectAppenders) {
                    if (possibleEffectAppender.isPlayableInFull(delegateActionContext))
                        return true;
                }
                return false;
            }
        };
    }
}
