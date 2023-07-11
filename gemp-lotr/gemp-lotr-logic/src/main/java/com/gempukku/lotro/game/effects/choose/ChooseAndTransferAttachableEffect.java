package com.gempukku.lotro.game.effects.choose;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.game.effects.TransferPermanentEffect;
import com.gempukku.lotro.game.actions.SubAction;
import com.gempukku.lotro.game.decisions.CardsSelectionDecision;
import com.gempukku.lotro.game.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.game.timing.AbstractEffect;
import com.gempukku.lotro.game.actions.Action;
import com.gempukku.lotro.game.timing.rules.RuleUtils;

import java.util.Collection;
import java.util.Set;

public class ChooseAndTransferAttachableEffect extends AbstractEffect {
    private final Action _action;
    private final String _playerId;
    private final Filterable _attachedTo;
    private final Filterable _attachedCard;
    private final Filterable _transferTo;
    private final boolean _skipOriginalTargetCheck;

    public ChooseAndTransferAttachableEffect(Action action, String playerId, Filterable attachedCard, Filterable attachedTo, Filterable transferTo) {
        this(action, playerId, false, attachedCard, attachedTo, transferTo);
    }

    public ChooseAndTransferAttachableEffect(Action action, String playerId, boolean skipOriginalTargetCheck, Filterable attachedCard, Filterable attachedTo, Filterable transferTo) {
        _action = action;
        _playerId = playerId;
        _skipOriginalTargetCheck = skipOriginalTargetCheck;
        _attachedCard = attachedCard;
        _attachedTo = attachedTo;
        _transferTo = transferTo;
    }

    @Override
    public String getText(DefaultGame game) {
        return null;
    }

    @Override
    public Type getType() {
        return null;
    }

    private Filterable getValidTargetFilter(DefaultGame game, final PhysicalCard attachment) {
        if (_skipOriginalTargetCheck) {
            return Filters.and(
                    _transferTo,
                    Filters.not(attachment.getAttachedTo()),
                    new Filter() {
                        @Override
                        public boolean accepts(DefaultGame game, PhysicalCard target) {
                            return game.getModifiersQuerying().canHaveTransferredOn(game, attachment, target);
                        }
                    });
        } else {
            return Filters.and(
                    _transferTo,
                    RuleUtils.getFullValidTargetFilter(attachment.getOwner(), game, attachment),
                    Filters.not(attachment.getAttachedTo()),
                    new Filter() {
                        @Override
                        public boolean accepts(DefaultGame game, PhysicalCard target) {
                            return game.getModifiersQuerying().canHaveTransferredOn(game, attachment, target);
                        }
                    });
        }
    }

    private Collection<PhysicalCard> getPossibleAttachmentsToTransfer(final DefaultGame game) {
        return Filters.filterActive(game,
                _attachedCard,
                Filters.attachedTo(_attachedTo),
                new Filter() {
                    @Override
                    public boolean accepts(DefaultGame game, final PhysicalCard transferredCard) {
                        if (transferredCard.getBlueprint().getValidTargetFilter(transferredCard.getOwner(), game, transferredCard) == null)
                            return false;

                        if (!game.getModifiersQuerying().canBeTransferred(game, transferredCard))
                            return false;

                        return Filters.countActive(game, getValidTargetFilter(game, transferredCard))>0;
                    }
                });
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return getPossibleAttachmentsToTransfer(game).size() > 0;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(final DefaultGame game) {
        final Collection<PhysicalCard> possibleAttachmentsToTransfer = getPossibleAttachmentsToTransfer(game);
        if (possibleAttachmentsToTransfer.size() > 0) {
            game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new CardsSelectionDecision(1, "Choose card to transfer", possibleAttachmentsToTransfer, 1, 1) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            final Set<PhysicalCard> selectedAttachments = getSelectedCardsByResponse(result);
                            if (selectedAttachments.size() == 1) {
                                final PhysicalCard attachment = selectedAttachments.iterator().next();
                                final PhysicalCard transferredFrom = attachment.getAttachedTo();
                                final Collection<PhysicalCard> validTargets = Filters.filterActive(game, getValidTargetFilter(game, attachment));
                                game.getUserFeedback().sendAwaitingDecision(
                                        _playerId,
                                        new CardsSelectionDecision(1, "Choose transfer target", validTargets, 1, 1) {
                                            @Override
                                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                                final Set<PhysicalCard> selectedTargets = getSelectedCardsByResponse(result);
                                                if (selectedTargets.size() == 1) {
                                                    final PhysicalCard selectedTarget = selectedTargets.iterator().next();
                                                    SubAction subAction = new SubAction(_action);
                                                    subAction.appendEffect(
                                                            new TransferPermanentEffect(attachment, selectedTarget) {
                                                                @Override
                                                                protected void afterTransferredCallback() {
                                                                    afterTransferCallback(attachment, transferredFrom, selectedTarget);
                                                                }
                                                            });
                                                    game.getActionsEnvironment().addActionToStack(subAction);
                                                }
                                            }
                                        }
                                );
                            }
                        }
                    });
        }
        return new FullEffectResult(false);
    }

    protected void afterTransferCallback(PhysicalCard transferredCard, PhysicalCard transferredFrom, PhysicalCard transferredTo) {

    }
}
