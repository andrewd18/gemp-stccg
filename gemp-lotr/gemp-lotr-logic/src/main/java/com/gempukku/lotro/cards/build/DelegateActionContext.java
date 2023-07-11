package com.gempukku.lotro.cards.build;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.game.timing.Effect;
import com.gempukku.lotro.game.timing.EffectResult;

import java.util.Collection;

public class DelegateActionContext implements ActionContext {
    private final ActionContext delegate;

    private final String performingPlayer;
    private final DefaultGame game;
    private final PhysicalCard source;
    private final EffectResult effectResult;
    private final Effect effect;

    public DelegateActionContext(ActionContext delegate, String performingPlayer, DefaultGame game, PhysicalCard source, EffectResult effectResult, Effect effect) {
        this.delegate = delegate;
        this.performingPlayer = performingPlayer;
        this.game = game;
        this.source = source;
        this.effectResult = effectResult;
        this.effect = effect;
    }

    @Override
    public void setValueToMemory(String memory, String value) {
        delegate.setValueToMemory(memory, value);
    }

    @Override
    public String getValueFromMemory(String memory) {
        return delegate.getValueFromMemory(memory);
    }

    @Override
    public void setCardMemory(String memory, PhysicalCard card) {
        delegate.setCardMemory(memory, card);
    }

    @Override
    public void setCardMemory(String memory, Collection<? extends PhysicalCard> cards) {
        delegate.setCardMemory(memory, cards);
    }

    @Override
    public Collection<? extends PhysicalCard> getCardsFromMemory(String memory) {
        return delegate.getCardsFromMemory(memory);
    }

    @Override
    public PhysicalCard getCardFromMemory(String memory) {
        return delegate.getCardFromMemory(memory);
    }

    @Override
    public String getPerformingPlayer() {
        return performingPlayer;
    }

    @Override
    public DefaultGame getGame() {
        return game;
    }

    @Override
    public PhysicalCard getSource() {
        return source;
    }

    @Override
    public EffectResult getEffectResult() {
        return effectResult;
    }

    @Override
    public Effect getEffect() {
        return effect;
    }
}
