package com.gempukku.lotro.cards;

import com.gempukku.lotro.actions.lotronly.CostToEffectAction;
import com.gempukku.lotro.actions.lotronly.PlayEventAction;
import com.gempukku.lotro.actions.lotronly.RequiredTriggerAction;
import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.actions.Action;
import com.gempukku.lotro.actions.lotronly.ActivateCardAction;
import com.gempukku.lotro.actions.OptionalTriggerAction;
import com.gempukku.lotro.modifiers.ExtraPlayCost;
import com.gempukku.lotro.rules.lotronly.LotroPlayUtils;
import com.gempukku.lotro.effects.DiscountEffect;
import com.gempukku.lotro.modifiers.Modifier;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.EffectResult;

import java.util.*;

public class BuiltLotroCardBlueprint implements LotroCardBlueprint {
    private String title;
    private String subtitle;
    private String imageUrl;
    private boolean unique;
    private Side side;
    private CardType cardType;
    private Culture culture;
    private Race race;
    private Signet signet;
    private Map<Keyword, Integer> keywords;
    private int cost = -1;
    private int strength;
    private int vitality;
    private int resistance;
    private int tribbleValue;
    private String tribblePower;
    private SitesBlock siteBlock;
    private int siteNumber;
    private Set<PossessionClass> possessionClasses;
    private Direction direction;
    private SitesBlock allyHomeBlock;
    private int[] allyHomeSites = new int[0];

    private List<Requirement> requirements;
    private List<FilterableSource> targetFilters;

    private List<ActionSource> requiredBeforeTriggers;
    private List<ActionSource> requiredAfterTriggers;
    private List<ActionSource> optionalBeforeTriggers;
    private List<ActionSource> optionalAfterTriggers;

    private List<ActionSource> beforeActivatedTriggers;
    private List<ActionSource> afterActivatedTriggers;

    private List<ActionSource> optionalInHandBeforeActions;
    private List<ActionSource> optionalInHandAfterActions;

    private List<ActionSource> optionalInHandAfterTriggers;

    private List<ActionSource> inPlayPhaseActions;
    private List<ActionSource> inDiscardPhaseActions;
    private List<ActionSource> fromStackedPhaseActions;

    private List<ModifierSource> inPlayModifiers;
    private List<ModifierSource> stackedOnModifiers;
    private List<ModifierSource> inDiscardModifiers;
    private List<ModifierSource> controlledSiteModifiers;

    private List<TwilightCostModifierSource> twilightCostModifiers;

    private List<ExtraPlayCostSource> extraPlayCosts;
    private List<DiscountSource> discountSources;

    private List<Requirement> playInOtherPhaseConditions;
    private List<FilterableSource> copiedFilters;

    private ActionSource playEventAction;
    private ActionSource killedRequiredTriggerAction;
    private ActionSource killedOptionalTriggerAction;
    private ActionSource discardedFromPlayRequiredTriggerAction;
    private ActionSource discardedFromPlayOptionalTriggerAction;

    private AidCostSource aidCostSource;

    private ExtraPossessionClassTest extraPossessionClassTest;

    // Building methods

    public void setAllyHomeSites(SitesBlock block, int[] numbers) {
        this.allyHomeBlock = block;
        this.allyHomeSites = numbers;
    }

    public void setExtraPossessionClassTest(ExtraPossessionClassTest extraPossessionClassTest) {
        this.extraPossessionClassTest = extraPossessionClassTest;
    }

    public void appendCopiedFilter(FilterableSource filterableSource) {
        if (copiedFilters == null)
            copiedFilters = new LinkedList<>();
        copiedFilters.add(filterableSource);
    }

    public void appendPlayInOtherPhaseCondition(Requirement requirement) {
        if (playInOtherPhaseConditions == null)
            playInOtherPhaseConditions = new LinkedList<>();
        playInOtherPhaseConditions.add(requirement);
    }

    public void appendDiscountSource(DiscountSource discountSource) {
        if (discountSources == null)
            discountSources = new LinkedList<>();
        discountSources.add(discountSource);
    }

    public void appendOptionalInHandAfterTrigger(ActionSource actionSource) {
        if (optionalInHandAfterTriggers == null)
            optionalInHandAfterTriggers = new LinkedList<>();
        optionalInHandAfterTriggers.add(actionSource);
    }

    public void appendOptionalInHandBeforeAction(ActionSource actionSource) {
        if (optionalInHandBeforeActions == null)
            optionalInHandBeforeActions = new LinkedList<>();
        optionalInHandBeforeActions.add(actionSource);
    }

    public void appendOptionalInHandAfterAction(ActionSource actionSource) {
        if (optionalInHandAfterActions == null)
            optionalInHandAfterActions = new LinkedList<>();
        optionalInHandAfterActions.add(actionSource);
    }

    public void appendExtraPlayCost(ExtraPlayCostSource extraPlayCostSource) {
        if (extraPlayCosts == null)
            extraPlayCosts = new LinkedList<>();
        extraPlayCosts.add(extraPlayCostSource);
    }

    public void appendBeforeActivatedTrigger(ActionSource actionSource) {
        if (beforeActivatedTriggers == null)
            beforeActivatedTriggers = new LinkedList<>();
        beforeActivatedTriggers.add(actionSource);
    }

    public void appendAfterActivatedTrigger(ActionSource actionSource) {
        if (afterActivatedTriggers == null)
            afterActivatedTriggers = new LinkedList<>();
        afterActivatedTriggers.add(actionSource);
    }

    public void appendRequiredBeforeTrigger(ActionSource actionSource) {
        if (requiredBeforeTriggers == null)
            requiredBeforeTriggers = new LinkedList<>();
        requiredBeforeTriggers.add(actionSource);
    }

    public void appendRequiredAfterTrigger(ActionSource actionSource) {
        if (requiredAfterTriggers == null)
            requiredAfterTriggers = new LinkedList<>();
        requiredAfterTriggers.add(actionSource);
    }

    public void appendOptionalBeforeTrigger(ActionSource actionSource) {
        if (optionalBeforeTriggers == null)
            optionalBeforeTriggers = new LinkedList<>();
        optionalBeforeTriggers.add(actionSource);
    }

    public void appendOptionalAfterTrigger(ActionSource actionSource) {
        if (optionalAfterTriggers == null)
            optionalAfterTriggers = new LinkedList<>();
        optionalAfterTriggers.add(actionSource);
    }

    public void appendPlayRequirement(Requirement requirement) {
        if (requirements == null)
            requirements = new LinkedList<>();
        requirements.add(requirement);
    }

    public void appendInPlayModifier(ModifierSource modifierSource) {
        if (inPlayModifiers == null)
            inPlayModifiers = new LinkedList<>();
        inPlayModifiers.add(modifierSource);
    }

    public void appendStackedOnModifier(ModifierSource modifierSource) {
        if (stackedOnModifiers == null)
            stackedOnModifiers = new LinkedList<>();
        stackedOnModifiers.add(modifierSource);
    }

    public void appendInDiscardModifier(ModifierSource modifierSource) {
        if (inDiscardModifiers == null)
            inDiscardModifiers = new LinkedList<>();
        inDiscardModifiers.add(modifierSource);
    }

    public void appendTargetFilter(FilterableSource targetFilter) {
        if (targetFilters == null)
            targetFilters = new LinkedList<>();
        targetFilters.add(targetFilter);
    }

    public void appendInPlayPhaseAction(ActionSource actionSource) {
        if (inPlayPhaseActions == null)
            inPlayPhaseActions = new LinkedList<>();
        inPlayPhaseActions.add(actionSource);
    }

    public void appendInDiscardPhaseAction(ActionSource actionSource) {
        if (inDiscardPhaseActions == null)
            inDiscardPhaseActions = new LinkedList<>();
        inDiscardPhaseActions.add(actionSource);
    }

    public void appendFromStackedPhaseAction(ActionSource actionSource) {
        if (fromStackedPhaseActions == null)
            fromStackedPhaseActions = new LinkedList<>();
        fromStackedPhaseActions.add(actionSource);
    }

    public void appendTwilightCostModifier(TwilightCostModifierSource twilightCostModifierSource) {
        if (twilightCostModifiers == null)
            twilightCostModifiers = new LinkedList<>();
        twilightCostModifiers.add(twilightCostModifierSource);
    }

    public void setPlayEventAction(ActionSource playEventAction) {
        this.playEventAction = playEventAction;
    }

    public void setAidCostSource(AidCostSource aidCostSource) {
        this.aidCostSource = aidCostSource;
    }

    public void setKilledRequiredTriggerAction(ActionSource killedRequiredTriggerAction) {
        this.killedRequiredTriggerAction = killedRequiredTriggerAction;
    }

    public void setKilledOptionalTriggerAction(ActionSource killedOptionalTriggerAction) {
        this.killedOptionalTriggerAction = killedOptionalTriggerAction;
    }

    public void setDiscardedFromPlayRequiredTriggerAction(ActionSource discardedFromPlayRequiredTriggerAction) {
        this.discardedFromPlayRequiredTriggerAction = discardedFromPlayRequiredTriggerAction;
    }

    public void setDiscardedFromPlayOptionalTriggerAction(ActionSource discardedFromPlayOptionalTriggerAction) {
        this.discardedFromPlayOptionalTriggerAction = discardedFromPlayOptionalTriggerAction;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public void setCulture(Culture culture) {
        this.culture = culture;
    }

    public void setRace(Race race) {
        this.race = race;
    }

    public void setSignet(Signet signet) {
        this.signet = signet;
    }

    public void setKeywords(Map<Keyword, Integer> keywords) {
        this.keywords = keywords;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public void setVitality(int vitality) {
        this.vitality = vitality;
    }

    public void setResistance(int resistance) {
        this.resistance = resistance;
    }

    public void setTribbleValue(int tribbleValue) {
        this.tribbleValue = tribbleValue;
    }

    public void setTribblePower(String tribblePower) {
        this.tribblePower = tribblePower;
    }

    public void setSiteBlock(SitesBlock siteBlock) {
        this.siteBlock = siteBlock;
    }

    public void setSiteNumber(int siteNumber) {
        this.siteNumber = siteNumber;
    }

    public void setPossessionClasses(Set<PossessionClass> possessionClasses) {
        this.possessionClasses = possessionClasses;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    // Implemented methods

    @Override
    public Side getSide() {
        return side;
    }

    @Override
    public CardType getCardType() {
        return cardType;
    }

    @Override
    public Culture getCulture() {
        return culture;
    }

    @Override
    public Race getRace() {
        return race;
    }

    @Override
    public boolean isUnique() {
        return unique;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSubtitle() {
        return subtitle;
    }
    @Override
    public String getImageUrl() { return imageUrl; }

    @Override
    public Signet getSignet() {
        return signet;
    }

    @Override
    public int getTwilightCost() {
        return cost;
    }

    @Override
    public int getStrength() {
        return strength;
    }

    @Override
    public int getVitality() {
        return vitality;
    }

    @Override
    public int getResistance() {
        return resistance;
    }

    @Override
    public String getTribblePower() {
        return tribblePower;
    }

    @Override
    public int getTribbleValue() {
        return tribbleValue;
    }

    @Override
    public SitesBlock getSiteBlock() {
        return siteBlock;
    }

    @Override
    public int getSiteNumber() {
        return siteNumber;
    }

    @Override
    public int[] getAllyHomeSiteNumbers() {
        return allyHomeSites;
    }

    @Override
    public SitesBlock getAllyHomeSiteBlock() {
        return allyHomeBlock;
    }

    @Override
    public Set<PossessionClass> getPossessionClasses() {
        return possessionClasses;
    }

    @Override
    public Direction getSiteDirection() {
        return direction;
    }

    @Override
    public boolean hasKeyword(Keyword keyword) {
        return keywords != null && keywords.containsKey(keyword);
    }

    @Override
    public int getKeywordCount(Keyword keyword) {
        if (keywords == null)
            return 0;
        Integer count = keywords.get(keyword);
        if (count == null)
            return 0;
        else
            return count;
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, DefaultGame game, LotroPhysicalCard self) {
        if (targetFilters == null)
            return null;

        Filterable[] result = new Filterable[targetFilters.size()];
        for (int i = 0; i < result.length; i++) {
            final FilterableSource filterableSource = targetFilters.get(i);
            result[i] = filterableSource.getFilterable(null);
        }

        return Filters.and(result);
    }

    @Override
    public List<? extends Action> getPhaseActionsFromDiscard(String playerId, DefaultGame game, LotroPhysicalCard self) {
        return getActivatedActions(playerId, game, self, inDiscardPhaseActions);
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, DefaultGame game, LotroPhysicalCard self) {
        List<ActivateCardAction> activatedActions = getActivatedActions(playerId, game, self, inPlayPhaseActions);
        if (copiedFilters != null) {
            if (activatedActions == null)
                activatedActions = new LinkedList<>();
            for (FilterableSource copiedFilter : copiedFilters) {
                DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, null);
                final LotroPhysicalCard firstActive = Filters.findFirstActive(game, copiedFilter.getFilterable(actionContext));
                if (firstActive != null)
                    addAllNotNull(activatedActions, firstActive.getBlueprint().getPhaseActionsInPlay(playerId, game, self));
            }
        }
        return activatedActions;
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsFromStacked(String playerId, DefaultGame game, LotroPhysicalCard self) {
        return getActivatedActions(playerId, game, self, fromStackedPhaseActions);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(DefaultGame game, LotroPhysicalCard self) {
        List<Modifier> modifiers = getModifiers(game, self, inPlayModifiers);
        if (copiedFilters != null) {
            if (modifiers == null)
                modifiers = new LinkedList<>();
            for (FilterableSource copiedFilter : copiedFilters) {
                DefaultActionContext actionContext = new DefaultActionContext(self.getOwner(), game, self, null, null);
                final LotroPhysicalCard firstActive = Filters.findFirstActive(game, copiedFilter.getFilterable(actionContext));
                if (firstActive != null) {
                    addAllNotNull(modifiers, firstActive.getBlueprint().getInPlayModifiers(game, self));
                }
            }
        }
        return modifiers;
    }

    private <T> void addAllNotNull(List<T> list, List<? extends T> possiblyNullList) {
        if (possiblyNullList != null)
            list.addAll(possiblyNullList);
    }

    @Override
    public List<? extends Modifier> getStackedOnModifiers(DefaultGame game, LotroPhysicalCard self) {
        return getModifiers(game, self, stackedOnModifiers);
    }

    @Override
    public List<? extends Modifier> getInDiscardModifiers(DefaultGame game, LotroPhysicalCard self) {
        return getModifiers(game, self, inDiscardModifiers);
    }

    @Override
    public List<? extends Modifier> getControlledSiteModifiers(DefaultGame game, LotroPhysicalCard self) {
        return getModifiers(game, self, controlledSiteModifiers);
    }

    @Override
    public boolean checkPlayRequirements(DefaultGame game, LotroPhysicalCard self) {
        DefaultActionContext dummy = new DefaultActionContext(self.getOwner(), game, self, null, null);

        if (requirements != null)
            for (Requirement requirement : requirements) {
                if (!requirement.accepts(dummy))
                    return false;
            }

        if (playEventAction != null && !playEventAction.isValid(dummy))
            return false;

        return true;
    }

    @Override
    public int getTwilightCostModifier(DefaultGame game, LotroPhysicalCard self, LotroPhysicalCard target) {
        if (twilightCostModifiers == null)
            return 0;

        DefaultActionContext actionContext = new DefaultActionContext(self.getOwner(), game, self, null, null);

        int result = 0;
        for (TwilightCostModifierSource twilightCostModifier : twilightCostModifiers)
            result += twilightCostModifier.getTwilightCostModifier(actionContext, target);

        return result;
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, DefaultGame game, LotroPhysicalCard self) {
        DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, null);
        PlayEventAction action = new PlayEventAction(self, playEventAction.requiresRanger());
        playEventAction.createAction(action, actionContext);
        return action;
    }

    @Override
    public List<RequiredTriggerAction> getRequiredBeforeTriggers(DefaultGame game, Effect effect, LotroPhysicalCard self) {
        List<RequiredTriggerAction> result = null;

        if (requiredBeforeTriggers != null) {
            result = new LinkedList<>();
            for (ActionSource requiredBeforeTrigger : requiredBeforeTriggers) {
                DefaultActionContext actionContext = new DefaultActionContext(self.getOwner(), game, self, null, effect);
                if (requiredBeforeTrigger.isValid(actionContext)) {
                    RequiredTriggerAction action = new RequiredTriggerAction(self);
                    requiredBeforeTrigger.createAction(action, actionContext);
                    result.add(action);
                }
            }
        }

        if (copiedFilters != null) {
            if (result == null)
                result = new LinkedList<>();
            for (FilterableSource copiedFilter : copiedFilters) {
                DefaultActionContext actionContext = new DefaultActionContext(self.getOwner(), game, self, null, effect);
                final LotroPhysicalCard firstActive = Filters.findFirstActive(game, copiedFilter.getFilterable(actionContext));
                if (firstActive != null)
                    addAllNotNull(result, firstActive.getBlueprint().getRequiredBeforeTriggers(game, effect, self));
            }
        }

        return result;
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(DefaultGame game, EffectResult effectResult, LotroPhysicalCard self) {
        List<RequiredTriggerAction> result = null;

        if (requiredAfterTriggers != null) {
            result = new LinkedList<>();
            for (ActionSource requiredAfterTrigger : requiredAfterTriggers) {
                DefaultActionContext actionContext = new DefaultActionContext(self.getOwner(), game, self, effectResult, null);
                if (requiredAfterTrigger.isValid(actionContext)) {
                    RequiredTriggerAction action = new RequiredTriggerAction(self);
                    requiredAfterTrigger.createAction(action, actionContext);
                    result.add(action);
                }
            }
        }

        if (copiedFilters != null) {
            if (result == null)
                result = new LinkedList<>();
            for (FilterableSource copiedFilter : copiedFilters) {
                DefaultActionContext actionContext = new DefaultActionContext(self.getOwner(), game, self, effectResult, null);
                final LotroPhysicalCard firstActive = Filters.findFirstActive(game, copiedFilter.getFilterable(actionContext));
                if (firstActive != null)
                    addAllNotNull(result, firstActive.getBlueprint().getRequiredAfterTriggers(game, effectResult, self));
            }
        }

        return result;
    }

    @Override
    public List<OptionalTriggerAction> getOptionalBeforeTriggers(String playerId, DefaultGame game, Effect effect, LotroPhysicalCard self) {
        List<OptionalTriggerAction> result = null;

        if (optionalBeforeTriggers != null) {
            result = new LinkedList<>();
            for (ActionSource optionalBeforeTrigger : optionalBeforeTriggers) {
                DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, effect);
                if (optionalBeforeTrigger.isValid(actionContext)) {
                    OptionalTriggerAction action = new OptionalTriggerAction(self);
                    optionalBeforeTrigger.createAction(action, actionContext);
                    result.add(action);
                }
            }
        }

        if (copiedFilters != null) {
            if (result == null)
                result = new LinkedList<>();
            for (FilterableSource copiedFilter : copiedFilters) {
                DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, effect);
                final LotroPhysicalCard firstActive = Filters.findFirstActive(game, copiedFilter.getFilterable(actionContext));
                if (firstActive != null)
                    addAllNotNull(result, firstActive.getBlueprint().getOptionalBeforeTriggers(playerId, game, effect, self));
            }
        }

        return result;
    }

    public List<ActionSource> getOptionalAfterTriggers() {
        return optionalAfterTriggers;
    }

/*    @Override
    public List<OptionalTriggerAction> getOptionalAfterTriggerActions(String playerId, DefaultGame game,
                                                                      EffectResult effectResult,
                                                                      LotroPhysicalCard self) {
        List<OptionalTriggerAction> result = null;

        if (optionalAfterTriggers != null) {
            result = new LinkedList<>();
            for (ActionSource optionalAfterTrigger : optionalAfterTriggers) {
                DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, effectResult,
                        null);
                if (optionalAfterTrigger.isValid(actionContext)) {
                    OptionalTriggerAction action = new OptionalTriggerAction(self);
                    optionalAfterTrigger.createAction(action, actionContext);
                    result.add(action);
                }
            }
        }

        if (copiedFilters != null) {
            if (result == null)
                result = new LinkedList<>();
            for (FilterableSource copiedFilter : copiedFilters) {
                DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, effectResult,
                        null);
                final LotroPhysicalCard firstActive = Filters.findFirstActive(game, copiedFilter.getFilterable(actionContext));
                if (firstActive != null)
                    addAllNotNull(result, firstActive.getOptionalAfterTriggerActions(playerId, game, effectResult, self));
            }
        }

        return result;
    }
*/
    @Override
    public List<? extends ActivateCardAction> getOptionalInPlayBeforeActions(String playerId, DefaultGame game, Effect effect, LotroPhysicalCard self) {
        List<ActivateCardAction> result = null;

        if (beforeActivatedTriggers != null) {
            result = new LinkedList<>();
            for (ActionSource beforeActivatedTrigger : beforeActivatedTriggers) {
                DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, effect);
                if (beforeActivatedTrigger.isValid(actionContext)) {
                    ActivateCardAction action = new ActivateCardAction(self);
                    beforeActivatedTrigger.createAction(action, actionContext);
                    result.add(action);
                }
            }
        }

        if (copiedFilters != null) {
            if (result == null)
                result = new LinkedList<>();
            for (FilterableSource copiedFilter : copiedFilters) {
                DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, effect);
                final LotroPhysicalCard firstActive = Filters.findFirstActive(game, copiedFilter.getFilterable(actionContext));
                if (firstActive != null)
                    addAllNotNull(result, firstActive.getBlueprint().getOptionalInPlayBeforeActions(playerId, game, effect, self));
            }
        }

        return result;
    }

    @Override
    public List<? extends ActivateCardAction> getOptionalInPlayAfterActions(String playerId, DefaultGame game, EffectResult effectResult, LotroPhysicalCard self) {
        List<ActivateCardAction> result = null;

        if (afterActivatedTriggers != null) {
            result = new LinkedList<>();
            for (ActionSource afterActivatedTrigger : afterActivatedTriggers) {
                DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, effectResult, null);
                if (afterActivatedTrigger.isValid(actionContext)) {
                    ActivateCardAction action = new ActivateCardAction(self);
                    afterActivatedTrigger.createAction(action, actionContext);
                    result.add(action);
                }
            }
        }

        if (copiedFilters != null) {
            if (result == null)
                result = new LinkedList<>();
            for (FilterableSource copiedFilter : copiedFilters) {
                DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, effectResult, null);
                final LotroPhysicalCard firstActive = Filters.findFirstActive(game, copiedFilter.getFilterable(actionContext));
                if (firstActive != null)
                    addAllNotNull(result, firstActive.getBlueprint().getOptionalInPlayAfterActions(playerId, game, effectResult, self));
            }
        }

        return result;
    }

    @Override
    public List<PlayEventAction> getPlayResponseEventBeforeActions(String playerId, DefaultGame game, Effect effect, LotroPhysicalCard self) {
        if (optionalInHandBeforeActions == null)
            return null;

        List<PlayEventAction> result = new LinkedList<>();
        for (ActionSource optionalInHandBeforeAction : optionalInHandBeforeActions) {
            DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, effect);
            if (optionalInHandBeforeAction.isValid(actionContext)) {
                PlayEventAction action = new PlayEventAction(self);
                optionalInHandBeforeAction.createAction(action, actionContext);
                result.add(action);
            }
        }

        return result;
    }

    @Override
    public List<PlayEventAction> getPlayResponseEventAfterActions(String playerId, DefaultGame game, EffectResult effectResult, LotroPhysicalCard self) {
        if (optionalInHandAfterActions == null)
            return null;

        List<PlayEventAction> result = new LinkedList<>();
        for (ActionSource optionalInHandAfterAction : optionalInHandAfterActions) {
            DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, effectResult, null);
            if (optionalInHandAfterAction.isValid(actionContext)) {
                PlayEventAction action = new PlayEventAction(self);
                optionalInHandAfterAction.createAction(action, actionContext);
                result.add(action);
            }
        }

        return result;
    }

    @Override
    public List<OptionalTriggerAction> getOptionalInHandAfterTriggers(String playerId, DefaultGame game, EffectResult effectResult, LotroPhysicalCard self) {
        if (optionalInHandAfterTriggers == null)
            return null;

        List<OptionalTriggerAction> result = new LinkedList<>();
        for (ActionSource optionalInHandAfterTrigger : optionalInHandAfterTriggers) {
            DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, effectResult, null);
            if (optionalInHandAfterTrigger.isValid(actionContext)) {
                OptionalTriggerAction action = new OptionalTriggerAction(self);
                optionalInHandAfterTrigger.createAction(action, actionContext);
                result.add(action);
            }
        }

        return result;
    }

    @Override
    public List<? extends ExtraPlayCost> getExtraCostToPlay(DefaultGame game, LotroPhysicalCard self) {
        if (extraPlayCosts == null)
            return null;

        DefaultActionContext actionContext = new DefaultActionContext(self.getOwner(), game, self, null, null);

        List<ExtraPlayCost> result = new LinkedList<>();
        for (ExtraPlayCostSource extraPlayCost : extraPlayCosts) {
            result.add(extraPlayCost.getExtraPlayCost(actionContext));
        }

        return result;
    }

    @Override
    public List<? extends Action> getPhaseActionsInHand(String playerId, DefaultGame game, LotroPhysicalCard self) {
        if (playInOtherPhaseConditions == null)
            return null;

        List<Action> playCardActions = new LinkedList<>();
        for (Requirement playInOtherPhaseCondition : playInOtherPhaseConditions) {
            DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, null);
            if (playInOtherPhaseCondition.accepts(actionContext)
                    && LotroPlayUtils.checkPlayRequirements(game, self, Filters.any, 0, 0, false, false, false))
                playCardActions.add(LotroPlayUtils.getPlayCardAction(game, self, 0, Filters.any, false));
        }

        return playCardActions;
    }

    // Default implementations - not needed (for now)

    @Override
    public boolean isExtraPossessionClass(DefaultGame game, LotroPhysicalCard self, LotroPhysicalCard attachedTo) {
        if (extraPossessionClassTest != null)
            return extraPossessionClassTest.isExtraPossessionClass(game, self, attachedTo);
        return false;
    }

    @Override
    public int getPotentialDiscount(DefaultGame game, String playerId, LotroPhysicalCard self) {
        if (discountSources == null)
            return 0;

        int result = 0;
        DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, null);
        for (DiscountSource discountSource : discountSources)
            result += discountSource.getPotentialDiscount(actionContext);

        return result;
    }

    @Override
    public void appendPotentialDiscountEffects(DefaultGame game, CostToEffectAction action, String playerId, LotroPhysicalCard self) {
        if (discountSources != null) {
            DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, null);
            for (DiscountSource discountSource : discountSources) {
                final DiscountEffect discountEffect = discountSource.getDiscountEffect(action, actionContext);
                action.appendPotentialDiscount(discountEffect);
            }
        }
    }

    @Override
    public RequiredTriggerAction getDiscardedFromPlayRequiredTrigger(DefaultGame game, LotroPhysicalCard self) {
        if (discardedFromPlayRequiredTriggerAction == null)
            return null;

        DefaultActionContext actionContext = new DefaultActionContext(self.getOwner(), game, self, null, null);
        if (discardedFromPlayRequiredTriggerAction.isValid(actionContext)) {
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            discardedFromPlayRequiredTriggerAction.createAction(action, actionContext);
            return action;
        }
        return null;
    }

    @Override
    public OptionalTriggerAction getDiscardedFromPlayOptionalTrigger(String playerId, DefaultGame game, LotroPhysicalCard self) {
        if (discardedFromPlayOptionalTriggerAction == null)
            return null;

        DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, null);
        if (discardedFromPlayOptionalTriggerAction.isValid(actionContext)) {
            OptionalTriggerAction action = new OptionalTriggerAction(self);
            discardedFromPlayOptionalTriggerAction.createAction(action, actionContext);
            return action;
        }
        return null;
    }

    @Override
    public RequiredTriggerAction getKilledRequiredTrigger(DefaultGame game, LotroPhysicalCard self) {
        if (killedRequiredTriggerAction == null)
            return null;

        DefaultActionContext actionContext = new DefaultActionContext(self.getOwner(), game, self, null, null);
        if (killedRequiredTriggerAction.isValid(actionContext)) {
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            killedRequiredTriggerAction.createAction(action, actionContext);
            return action;
        }
        return null;
    }

    @Override
    public OptionalTriggerAction getKilledOptionalTrigger(String playerId, DefaultGame game, LotroPhysicalCard self) {
        if (killedOptionalTriggerAction == null)
            return null;

        DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, null);
        if (killedOptionalTriggerAction.isValid(actionContext)) {
            OptionalTriggerAction action = new OptionalTriggerAction(self);
            killedOptionalTriggerAction.createAction(action, actionContext);
            return action;
        }
        return null;
    }

    @Override
    public String getDisplayableInformation(LotroPhysicalCard self) {
        return null;
    }

    @Override
    public boolean canPayAidCost(DefaultGame game, LotroPhysicalCard self) {
        if (aidCostSource == null)
            return false;

        DefaultActionContext actionContext = new DefaultActionContext(self.getOwner(), game, self, null, null);
        return aidCostSource.canPayAidCost(actionContext);
    }

    @Override
    public void appendAidCosts(DefaultGame game, CostToEffectAction action, LotroPhysicalCard self) {
        if (aidCostSource != null) {
            DefaultActionContext actionContext = new DefaultActionContext(self.getOwner(), game, self, null, null);
            aidCostSource.appendAidCost(action, actionContext);
        }
    }

    @Override
    public boolean skipUniquenessCheck() {
        return false;
    }

    // Helper methods

    private List<Modifier> getModifiers(DefaultGame game, LotroPhysicalCard self, List<ModifierSource> sources) {
        if (sources == null)
            return null;

        List<Modifier> result = new LinkedList<>();
        for (ModifierSource inPlayModifier : sources) {
            ActionContext actionContext = new DefaultActionContext(self.getOwner(), game, self, null, null);
            result.add(inPlayModifier.getModifier(actionContext));
        }
        return result;
    }

    private List<ActivateCardAction> getActivatedActions(String playerId, DefaultGame game, LotroPhysicalCard self, List<ActionSource> sources) {
        if (sources == null)
            return null;

        List<ActivateCardAction> result = new LinkedList<>();
        for (ActionSource inPlayPhaseAction : sources) {
            DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, null);
            if (inPlayPhaseAction.isValid(actionContext)) {
                ActivateCardAction action = new ActivateCardAction(self);
                inPlayPhaseAction.createAction(action, actionContext);
                result.add(action);
            }
        }
        return result;
    }

    public void validateConsistency() throws InvalidCardDefinitionException {
        if (title == null)
            throw new InvalidCardDefinitionException("Card has to have a title");
        if (cardType == null)
            throw new InvalidCardDefinitionException("Card has to have a type");
/*        if (cardType != CardType.THE_ONE_RING && cardType != CardType.SITE && side == null)
            throw new InvalidCardDefinitionException("Only The One Ring does not have a side defined");
        if (cardType != CardType.THE_ONE_RING && cardType != CardType.SITE && culture == null)
            throw new InvalidCardDefinitionException("Only The One Ring does not have a culture defined");*/
        if (siteNumber != 0
                && cardType != CardType.SITE
                && cardType != CardType.MINION)
            throw new InvalidCardDefinitionException("Only minions and sites have a site number, use siteHome for allies");
        if (cardType == CardType.EVENT) {
            List<Keyword> requiredKeywords = Arrays.asList(
                    Keyword.RESPONSE, Keyword.FELLOWSHIP, Keyword.SHADOW, Keyword.MANEUVER, Keyword.ARCHERY, Keyword.ASSIGNMENT,
                    Keyword.SKIRMISH, Keyword.REGROUP);
            if (keywords == null || Collections.disjoint(keywords.keySet(), requiredKeywords))
                throw new InvalidCardDefinitionException("Events have to have a response or phase keyword");

            if (keywords.containsKey(Keyword.RESPONSE)) {
                if (optionalInHandBeforeActions == null && optionalInHandAfterActions == null)
                    throw new InvalidCardDefinitionException("Response events have to have responseEvent type effect");
            } else {
                if (playEventAction == null)
                    throw new InvalidCardDefinitionException("Events have to have an event type effect");
            }
        }
        if (cardType != CardType.EVENT && playEventAction != null)
            throw new InvalidCardDefinitionException("Only events should have an event type effect");
/*        if (cost == -1)
            throw new InvalidCardDefinitionException("Cost was not assigned to card");*/
        if (Arrays.asList(CardType.MINION, CardType.COMPANION, CardType.ALLY).contains(cardType)) {
            if (vitality == 0)
                throw new InvalidCardDefinitionException("Character has 0 vitality");
            if (strength == 0)
                throw new InvalidCardDefinitionException("Character has 0 strength");
        }
        if (cardType == CardType.SITE && siteBlock == null)
            throw new InvalidCardDefinitionException("Site has to have a block defined");
        if (siteBlock != null && cardType != CardType.SITE)
            throw new InvalidCardDefinitionException("Block defined for card, that is not site");
        if (targetFilters != null && keywords != null) {
            if (keywords.size() > 1 && keywords.containsKey(Keyword.TALE))
                throw new InvalidCardDefinitionException("Attachment should not have keywords");
        }
/*        if (Arrays.asList(CardType.POSSESSION, CardType.CONDITION, CardType.ARTIFACT).contains(cardType)
                && targetFilters == null && (keywords == null || !keywords.containsKey(Keyword.SUPPORT_AREA)))
            throw new InvalidCardDefinitionException("Possession, condition or artifact without a filter needs a SUPPORT_AREA keyword");*/
        if (cardType == CardType.FOLLOWER && aidCostSource == null)
            throw new InvalidCardDefinitionException("Follower requires an aid cost");
    }

    public List<FilterableSource> getCopiedFilters() {
        return copiedFilters;
    }

}
