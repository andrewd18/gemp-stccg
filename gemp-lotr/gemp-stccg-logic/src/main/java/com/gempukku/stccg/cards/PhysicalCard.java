package com.gempukku.stccg.cards;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.sources.ActionSource;
import com.gempukku.stccg.common.filterable.TriggerTiming;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.ST1ELocation;
import com.gempukku.stccg.modifiers.ExtraPlayCost;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.ModifierEffect;
import com.gempukku.stccg.modifiers.ModifierHook;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.results.EffectResult;
import com.gempukku.stccg.rules.GameUtils;

import java.util.*;

public abstract class PhysicalCard implements Filterable {
    protected Zone _zone;
    protected final CardBlueprint _blueprint;
    protected final Player _owner;
    protected final String _ownerName;
    protected String _cardController;
    protected int _cardId;
    protected PhysicalCard _attachedTo;
    protected PhysicalCard _stackedOn;
    protected List<ModifierHook> _modifierHooks;
    protected Map<Zone, List<ModifierHook>> _modifierHooksInZone; // modifier hooks specific to stacked and discard
    private Map<Zone, List<ModifierSource>> _modifiers; // modifiers specific to stacked and discard
    protected Object _whileInZoneData;
    protected int _locationZoneIndex;
    protected ST1ELocation _currentLocation;
    private ActionSource _playEventAction;
    public PhysicalCard(int cardId, Player owner, CardBlueprint blueprint) {
        _cardId = cardId;
        _owner = owner;
        _ownerName = owner.getPlayerId();
        _blueprint = blueprint;
        _cardController = _ownerName; // TODO - This is likely not 100% accurate, as it is probably setting the controller before the card enters play.
    }

    public abstract DefaultGame getGame();
    
    public Zone getZone() { return _zone; }

    public void setZone(Zone zone) { _zone = zone; }
    
    public String getBlueprintId() { return _blueprint.getBlueprintId(); }
    public String getImageUrl() { return _blueprint.getImageUrl(); }

    public String getCardController() { return _cardController; }

    public void setCardId(int cardId) {
        _cardId = cardId;
    }
    
    public int getCardId() { return _cardId; }
    public Player getOwner() { return _owner; }

    public String getOwnerName() {
        return _ownerName;
    }

    public void startAffectingGame() {
        List<? extends Modifier> modifiers = getInPlayModifiers();
        if (modifiers != null) {
            _modifierHooks = new LinkedList<>();
            for (Modifier modifier : modifiers)
                _modifierHooks.add(getGame().getModifiersEnvironment().addAlwaysOnModifier(modifier));
        }
    }

    public void startAffectingGameInZone(Zone zone) {
        List<? extends Modifier> modifiers = null;
        if (zone == Zone.STACKED || zone == Zone.DISCARD)
            modifiers = getModifiers(_modifiers.get(zone));
        if (modifiers != null) {
            _modifierHooksInZone.put(zone, new LinkedList<>());
            for (Modifier modifier : modifiers)
                _modifierHooksInZone.get(zone).add(getGame().getModifiersEnvironment().addAlwaysOnModifier(modifier));
        }
    }

    public void stopAffectingGame() {
        if (_modifierHooks != null) {
            for (ModifierHook modifierHook : _modifierHooks)
                modifierHook.stop();
            _modifierHooks = null;
        }
    }

    public void appendModifierInZone(ModifierSource modifierSource, Zone zone) {
        _modifiers.computeIfAbsent(zone, k -> new LinkedList<>());
        _modifiers.get(zone).add(modifierSource);
    }

    public void stopAffectingGameInZone(Zone zone) {
        if (_modifierHooksInZone != null)
            if (_modifierHooksInZone.get(zone) != null) {
                for (ModifierHook modifierHook : _modifierHooksInZone.get(zone))
                    modifierHook.stop();
                _modifierHooksInZone.remove(zone);
            }
    }

    public CardBlueprint getBlueprint() {
        return _blueprint;
    }

    public void attachTo(PhysicalCard physicalCard) {
        _attachedTo = physicalCard;
    }

    public void detach() {
        _attachedTo = null;
    }

    public PhysicalCard getAttachedTo() {
        return _attachedTo;
    }

    public void stackOn(PhysicalCard physicalCard) {
        _stackedOn = physicalCard;
    }

    
    public PhysicalCard getStackedOn() {
        return _stackedOn;
    }

    
    public Object getWhileInZoneData() {
        return _whileInZoneData;
    }

    
    public void setWhileInZoneData(Object object) {
        _whileInZoneData = object;
    }


    public String getTitle() {
        return _blueprint.getTitle();
    }

    public boolean canInsertIntoSpaceline() { return _blueprint.canInsertIntoSpaceline(); }

    public void setLocationZoneIndex(int index) { _locationZoneIndex = index; }

    public int getLocationZoneIndex() { return _locationZoneIndex;  }

    public Quadrant getQuadrant() { return _blueprint.getQuadrant(); }

    public boolean isAffectingGame() { return getGame().getGameState().getCurrentPlayerId().equals(_ownerName); }
    public boolean canBeSeeded() { return false; }
    public boolean canBePlayed() {
        // Cannot play if unique and a copy is already in play
        if (isUnique() && _owner.hasACopyOfCardInPlay(this))
            return false;

        ActionContext context = createActionContext();
        List<Requirement> requirements = _blueprint.getPlayRequirements();

        if (requirements != null && !context.acceptsAllRequirements(requirements))
            return false;
        if (_blueprint.getPlayEventAction() != null && !_blueprint.getPlayEventAction().isValid(context))
            return false;

        // Check if the card's playability has been modified in the current game state
        return !getGame().getModifiersQuerying().canNotPlayCard(getOwnerName(), this);
    }

    public boolean isControlledBy(String playerId) {
        return Objects.equals(_cardController, playerId);
    }

    public String getCardLink() { return _blueprint.getCardLink(); }
    public ST1ELocation getLocation() { return null; }
    public void setLocation(ST1ELocation location) { _currentLocation = location; }

    public String getFullName() { return _blueprint.getFullName(); }

    public Filter getFullValidTargetFilter() {
        return Filters.and(getBlueprint().getValidTargetFilter());
    }

    public CostToEffectAction getPlayCardAction(int twilightModifier, Filterable additionalAttachmentFilter,
                                                boolean ignoreRoamingPenalty) {

        if (_blueprint.getCardType() != CardType.EVENT) {
            final Filterable validTargetFilter = _blueprint.getValidTargetFilter();
            if (validTargetFilter == null) {
                Zone playToZone = switch (_blueprint.getCardType()) {
                    case COMPANION -> Zone.FREE_CHARACTERS;
                    case MINION -> Zone.SHADOW_CHARACTERS;
                    default -> Zone.SUPPORT;
                };
                PlayPermanentAction action = new PlayPermanentAction(this, playToZone, twilightModifier,
                        ignoreRoamingPenalty);

                getGame().getModifiersQuerying().appendExtraCosts(action, this);
                getGame().getModifiersQuerying().appendPotentialDiscounts(action, this);

                return action;
            } else {
                Filter fullAttachValidTargetFilter = Filters.and(_blueprint.getValidTargetFilter(),
                        (Filter) (game1, physicalCard) -> game1.getModifiersQuerying().canHavePlayedOn(
                                game1, this, physicalCard),
                        (Filter) (game12, physicalCard) -> {
                            if (_blueprint.getSide() == Side.SHADOW) {
                                final int twilightCostOnTarget = game12.getModifiersQuerying().getTwilightCost(
                                        game12, this, physicalCard, twilightModifier, false);
                                int potentialDiscount =
                                        game12.getModifiersQuerying().getPotentialDiscount(this);
                                return twilightCostOnTarget - potentialDiscount <= game12.getGameState().getTwilightPool();
                            } else {
                                return true;
                            }
                        });

                final AttachPermanentAction action = new AttachPermanentAction(this,
                        Filters.and(fullAttachValidTargetFilter, additionalAttachmentFilter), twilightModifier);

                getGame().getModifiersQuerying().appendPotentialDiscounts(action, this);
                getGame().getModifiersQuerying().appendExtraCosts(action, this);

                return action;
            }
        } else {
                // Play event action
            PlayEventAction action = new PlayEventAction(this, _blueprint.getPlayEventAction().requiresRanger());
            _blueprint.getPlayEventAction().appendActionToContext(action, createActionContext());

            getGame().getModifiersQuerying().appendPotentialDiscounts(action, this);
            getGame().getModifiersQuerying().appendExtraCosts(action, this);

            return action;
        }
    }

    public List<Modifier> getModifiers(List<ModifierSource> sources) {
        if (sources == null)
            return null;
        List<Modifier> result = new LinkedList<>();
        sources.forEach(inPlayModifier -> result.add(inPlayModifier.getModifier(createActionContext())));
        return result;
    }

    public boolean hasTextRemoved() {
        for (Modifier modifier : getGame().getModifiersQuerying().getModifiersAffectingCard(ModifierEffect.TEXT_MODIFIER, this)) {
            if (modifier.hasRemovedText(getGame(), this))
                return true;
        }
        return false;
    }

    public boolean hasTransporters() {
        return false;
    }

    public CardType getCardType() { return _blueprint.getCardType(); }

    public List<? extends Action> getPhaseActionsInPlay(Player player) { return new LinkedList<>(); }

    public void attachToCardAtLocation(PhysicalCard destinationCard) {
        getGame().getGameState().transferCard(this, destinationCard);
        setLocation(destinationCard.getLocation());
    }

    public String getCardInfoHTML() {
        if (getZone().isInPlay() || getZone() == Zone.HAND) {
            StringBuilder sb = new StringBuilder();

            if (getZone() == Zone.HAND)
                sb.append("<b>Card is in hand - stats are only provisional</b><br><br>");
            else if (Filters.filterActive(getGame(), this).isEmpty())
                sb.append("<b>Card is inactive - current stats may be inaccurate</b><br><br>");

            Collection<Modifier> modifiers =
                    getGame().getModifiersQuerying().getModifiersAffecting(this);
            if (!modifiers.isEmpty()) {
                sb.append("<b>Affecting card:</b>");
                for (Modifier modifier : modifiers) {
                    String sourceText;
                    PhysicalCard source = modifier.getSource();
                    if (source != null) {
                        sourceText = source.getCardLink();
                    } else {
                        sourceText = "<i>System</i>";
                    }
                    sb.append("<br><b>").append(sourceText).append(":</b> ");
                    sb.append(modifier.getText(this));
                }
            }

            List<PhysicalCard> stackedCards = getStackedCards();
            if (!stackedCards.isEmpty()) {
                sb.append("<br><b>Stacked cards:</b>");
                sb.append("<br>").append(GameUtils.getConcatenatedCardLinks(stackedCards));
            }

            sb.append("<br><br><b>Effective stats:</b>");

            StringBuilder keywords = new StringBuilder();
            for (Keyword keyword : Keyword.values()) {
                if (keyword.isInfoDisplayable()) {
                    if (keyword.isMultiples()) {
                        int count = getGame().getModifiersQuerying().getKeywordCount(this, keyword);
                        if (count > 0)
                            keywords.append(keyword.getHumanReadable()).append(" +").append(count).append(", ");
                    } else {
                        if (getGame().getModifiersQuerying().hasKeyword(this, keyword))
                            keywords.append(keyword.getHumanReadable()).append(", ");
                    }
                }
            }
            if (!keywords.isEmpty())
                sb.append("<br><b>Keywords:</b> ").append(keywords.substring(0, keywords.length() - 2));

            return sb.append(getTypeSpecificCardInfoHTML()).toString();
        } else {
            return null;
        }
    }

    public List<PhysicalCard> getStackedCards() {
        List<PhysicalCard> result = new LinkedList<>();
        for (List<PhysicalCard> physicalCardList : getGame().getGameState().getStackedCards().values()) {
            for (PhysicalCard physicalCard : physicalCardList) {
                if (physicalCard.getStackedOn() == this)
                    result.add(physicalCard);
            }
        }
        return result;
    }
    public Collection<PhysicalCard> getAttachedCards() { return getGame().getGameState().getAttachedCards(this); }

    public String getTypeSpecificCardInfoHTML() {
        StringBuilder sb = new StringBuilder();
        Collection<PhysicalCard> attachedCards = getAttachedCards();
        if (!attachedCards.isEmpty()) {
            sb.append("<br><b>Attached cards:</b>");
            sb.append("<br>").append(GameUtils.getConcatenatedCardLinks(attachedCards));
        }
        return sb.toString();
    }

    public List<? extends Action> getPhaseActionsInPlay(String playerId) {
        return getActivatedActions(playerId, _blueprint.getInPlayPhaseActions());
    }

    public List<? extends Action> getPhaseActionsFromZone(String playerId, Zone zone) {
        if (zone == Zone.DISCARD) {
            return getActivatedActions(playerId, _blueprint.getInDiscardPhaseActions());
        }
        else if (zone == Zone.HAND) {
            if (_blueprint.getPlayInOtherPhaseConditions() == null)
                return null;
            ActionContext actionContext = createActionContext(playerId);
            List<Action> playCardActions = new LinkedList<>();

            if (canBePlayed()) {
                for (Requirement playInOtherPhaseCondition : _blueprint.getPlayInOtherPhaseConditions()) {
                    if (playInOtherPhaseCondition.accepts(actionContext))
                        playCardActions.add(getPlayCardAction(0, Filters.any, false));
                }
            }
            return playCardActions;
        }
        else return null;
    }

    public List<? extends Modifier> getInPlayModifiers() {
        return getModifiers(_blueprint.getInPlayModifiers());
    }

    public List<? extends ExtraPlayCost> getExtraCostToPlay() {
        if (_blueprint.getExtraPlayCosts() == null)
            return null;

        List<ExtraPlayCost> result = new LinkedList<>();
        for (ExtraPlayCostSource extraPlayCost : _blueprint.getExtraPlayCosts()) {
            result.add(extraPlayCost.getExtraPlayCost(createActionContext()));
        }

        return result;
    }

    public List<Action> getOptionalInPlayActions(Effect effect, TriggerTiming timing) {
        List<Action> result = new LinkedList<>();
        List<ActionSource> triggers = _blueprint.getActivatedTriggers(timing);

        if (triggers != null) {
            for (ActionSource trigger : triggers) {
                Action action = getActionFromActionSource(trigger, getOwnerName(), effect);
                if (action != null)
                    result.add(action);
            }
        }
        return result;
    }

    public List<Action> getOptionalInPlayActions(EffectResult effectResult, TriggerTiming timing) {
        List<Action> result = new LinkedList<>();
        List<ActionSource> triggers = _blueprint.getActivatedTriggers(timing);

        if (triggers != null) {
            for (ActionSource trigger : triggers) {
                Action action = getActionFromActionSource(trigger, getOwnerName(), effectResult);
                if (action != null)
                    result.add(action);
            }
        }
        return result;
    }



    public Action getDiscardedFromPlayTriggerAction(RequiredType requiredType) {
        ActionSource actionSource = _blueprint.getDiscardedFromPlayTrigger(requiredType);
        if (actionSource == null)
            return null;
        else
            return getActionFromActionSource(actionSource);
    }

    private Action getActionFromActionSource(ActionSource actionSource) {
        return actionSource.createActionAndAppendToContext(this, createActionContext());
    }

    private Action getActionFromActionSource(ActionSource actionSource, String playerId, EffectResult effectResult) {
        return actionSource.createActionAndAppendToContext(this, createActionContext(playerId, effectResult));
    }

    private Action getActionFromActionSource(ActionSource actionSource, String playerId, Effect effect) {
        return actionSource.createActionAndAppendToContext(this, createActionContext(playerId, effect));
    }

    private Action getActionFromActionSource(ActionSource actionSource, String playerId, Effect effect, EffectResult effectResult) {
        return actionSource.createActionAndAppendToContext(this, createActionContext(playerId, effect, effectResult));
    }


    private List<Action> getActionsFromActionSources(String playerId, List<ActionSource> actionSources) {
        return getActionsFromActionSources(playerId, null, null, actionSources);
    }

    private List<Action> getActionsFromActionSources(String playerId, Effect effect, List<ActionSource> actionSources) {
        return getActionsFromActionSources(playerId, effect, null, actionSources);
    }

    private List<Action> getActionsFromActionSources(String playerId, EffectResult effectResult, List<ActionSource> actionSources) {
        return getActionsFromActionSources(playerId, null, effectResult, actionSources);
    }

    private List<Action> getActionsFromActionSources(String playerId, Effect effect, EffectResult effectResult,
                                                    List<ActionSource> actionSources) {
        List<Action> result = new LinkedList<>();
        actionSources.forEach(actionSource -> {
            if (actionSource != null) {
                Action action = getActionFromActionSource(actionSource, playerId, effect, effectResult);
                if (action != null)
                    result.add(action);
            }
        });
        return result;
    }

    private List<Action> getActivatedActions(String playerId, List<ActionSource> sources) {
        return getActionsFromActionSources(playerId, sources);
    }

    public List<Action> getOptionalAfterTriggerActions(String playerId, EffectResult effectResult) {
        return getActionsFromActionSources(
                playerId, effectResult, getBlueprint().getBeforeOrAfterTriggers(RequiredType.OPTIONAL, TriggerTiming.AFTER));
    }

    public List<Action> getBeforeTriggerActions(Effect effect, RequiredType requiredType) {
        return getActionsFromActionSources(getOwnerName(), effect, _blueprint.getBeforeOrAfterTriggers(requiredType, TriggerTiming.BEFORE));
    }

    public List<Action> getBeforeTriggerActions(String playerId, Effect effect, RequiredType requiredType) {
        return getActionsFromActionSources(
                playerId, effect, _blueprint.getBeforeOrAfterTriggers(requiredType, TriggerTiming.BEFORE));
    }

    public List<Action> getAfterTriggerActions(EffectResult effectResult, RequiredType requiredType) {
        return getActionsFromActionSources(getOwnerName(), effectResult, _blueprint.getBeforeOrAfterTriggers(requiredType, TriggerTiming.AFTER));
    }

    public List<Action> getOptionalInHandAfterTriggerActions(String playerId, EffectResult effectResult) {
        return getActionsFromActionSources(playerId, effectResult, getBlueprint().getOptionalInHandTriggers(TriggerTiming.AFTER));
    }

    public ActionContext createActionContext() { return createActionContext(getOwnerName(), null, null); }
    public ActionContext createActionContext(String playerId) { return createActionContext(playerId, null, null); }

    public ActionContext createActionContext(String playerId, EffectResult effectResult) { return createActionContext(playerId, null, effectResult); }
    public ActionContext createActionContext(String playerId, Effect effect) { return createActionContext(playerId, effect, null); }
    public ActionContext createActionContext(String playerId, Effect effect, EffectResult effectResult) {
        return new DefaultActionContext(playerId, getGame(), this, effect, effectResult);
    }

    public boolean playRequirementsNotMet() {
        ActionContext context = createActionContext();
        List<Requirement> requirements = _blueprint.getPlayRequirements();

        if (requirements != null)
            if (!context.acceptsAllRequirements(requirements)) return true;

        return !(_blueprint.getPlayEventAction() == null || _blueprint.getPlayEventAction().isValid(context));
    }

    public int getPotentialDiscount() {
        if (_blueprint.getDiscountSources() == null)
            return 0;

        int result = 0;
        for (DiscountSource discountSource : _blueprint.getDiscountSources())
            result += discountSource.getPotentialDiscount(createActionContext());

        return result;
    }

    public void appendPotentialDiscountEffectsToAction(CostToEffectAction action) {
        if (_blueprint.getDiscountSources() != null) {
            ActionContext actionContext = createActionContext();
            for (DiscountSource discountSource : _blueprint.getDiscountSources()) {
                action.appendPotentialDiscount(discountSource.getDiscountEffect(action, actionContext));
            }
        }
    }

    public boolean isUnique() {
        return _blueprint.isUnique();
    }
}