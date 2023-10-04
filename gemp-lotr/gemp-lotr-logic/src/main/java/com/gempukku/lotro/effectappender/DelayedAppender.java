package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.DefaultActionContext;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.UnrespondableEffect;
import com.gempukku.lotro.game.DefaultGame;

import java.util.Collections;
import java.util.List;

public abstract class DelayedAppender<AbstractGame extends DefaultGame> implements EffectAppender<AbstractGame> {
    private String text;

    public DelayedAppender() {

    }

    public DelayedAppender(String text) {
        this.text = text;
    }

    @Override
    public final void appendEffect(boolean cost, CostToEffectAction action, DefaultActionContext<AbstractGame> actionContext) {
        final UnrespondableEffect effect = new UnrespondableEffect() {
            @Override
            protected void doPlayEffect(DefaultGame game) {
                // Need to insert them, but in the reverse order
                final List<? extends Effect> effects = createEffects(cost, action, actionContext);
                if (effects != null) {
                    final Effect[] effectsArray = effects.toArray(new Effect[0]);
                    for (int i = effectsArray.length - 1; i >= 0; i--)
                        if (cost)
                            action.insertCost(effectsArray[i]);
                        else
                            action.insertEffect(effectsArray[i]);
                }
            }

            @Override
            public String getText(DefaultGame game) {
                return text;
            }
        };

        if (cost) {
            action.appendCost(effect);
        } else {
            action.appendEffect(effect);
        }
    }

    protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext<AbstractGame> actionContext) {
        throw new UnsupportedOperationException("One of createEffect or createEffects has to be overwritten");
    }

    protected List<? extends Effect> createEffects(boolean cost, CostToEffectAction<AbstractGame> action,
                                                   DefaultActionContext<AbstractGame> actionContext) {
        final Effect effect = createEffect(cost, action, actionContext);
        if (effect == null)
            return null;
        return Collections.singletonList(effect);
    }

    @Override
    public boolean isPlayableInFull(DefaultActionContext<AbstractGame> actionContext) {
        return true;
    }
}
