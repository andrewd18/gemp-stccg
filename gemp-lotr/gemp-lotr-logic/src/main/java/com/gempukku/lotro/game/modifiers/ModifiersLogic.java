package com.gempukku.lotro.game.modifiers;

import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.game.modifiers.evaluator.Evaluator;
import com.gempukku.lotro.game.actions.lotronly.CostToEffectAction;
import com.gempukku.lotro.game.actions.Action;

import java.util.*;

public class ModifiersLogic implements ModifiersEnvironment, ModifiersQuerying {
    private final Map<ModifierEffect, List<Modifier>> _modifiers = new HashMap<>();

    private final Map<Phase, List<Modifier>> _untilStartOfPhaseModifiers = new HashMap<>();
    private final Map<Phase, List<Modifier>> _untilEndOfPhaseModifiers = new HashMap<>();
    private final List<Modifier> _untilEndOfTurnModifiers = new LinkedList<>();

    private final Set<Modifier> _skipSet = new HashSet<>();

    private final Map<Phase, Map<String, LimitCounter>> _endOfPhaseLimitCounters = new HashMap<>();
    private final Map<Phase, Map<String, LimitCounter>> _startOfPhaseLimitCounters = new HashMap<>();
    private final Map<String, LimitCounter> _turnLimitCounters = new HashMap<>();

    private int _drawnThisPhaseCount = 0;
    private final Map<Integer, Integer> _woundsPerPhaseMap = new HashMap<>();

    @Override
    public LimitCounter getUntilEndOfPhaseLimitCounter(LotroPhysicalCard card, Phase phase) {
        return getUntilEndOfPhaseLimitCounter(card, "", phase);
    }

    @Override
    public LimitCounter getUntilStartOfPhaseLimitCounter(LotroPhysicalCard card, Phase phase) {
        return getUntilStartOfPhaseLimitCounter(card, "", phase);
    }

    @Override
    public LimitCounter getUntilEndOfPhaseLimitCounter(LotroPhysicalCard card, String prefix, Phase phase) {
        Map<String, LimitCounter> limitCounterMap = _endOfPhaseLimitCounters.get(phase);
        if (limitCounterMap == null) {
            limitCounterMap = new HashMap<>();
            _endOfPhaseLimitCounters.put(phase, limitCounterMap);
        }
        LimitCounter limitCounter = limitCounterMap.get(prefix + card.getCardId());
        if (limitCounter == null) {
            limitCounter = new DefaultLimitCounter();
            limitCounterMap.put(prefix + card.getCardId(), limitCounter);
        }
        return limitCounter;
    }

    @Override
    public LimitCounter getUntilStartOfPhaseLimitCounter(LotroPhysicalCard card, String prefix, Phase phase) {
        Map<String, LimitCounter> limitCounterMap = _startOfPhaseLimitCounters.get(phase);
        if (limitCounterMap == null) {
            limitCounterMap = new HashMap<>();
            _startOfPhaseLimitCounters.put(phase, limitCounterMap);
        }
        LimitCounter limitCounter = limitCounterMap.get(prefix + card.getCardId());
        if (limitCounter == null) {
            limitCounter = new DefaultLimitCounter();
            limitCounterMap.put(prefix + card.getCardId(), limitCounter);
        }
        return limitCounter;
    }

    @Override
    public LimitCounter getUntilEndOfTurnLimitCounter(LotroPhysicalCard card) {
        return getUntilEndOfTurnLimitCounter(card, "");
    }

    @Override
    public LimitCounter getUntilEndOfTurnLimitCounter(LotroPhysicalCard card, String prefix) {
        LimitCounter limitCounter = _turnLimitCounters.get(prefix + card.getCardId());
        if (limitCounter == null) {
            limitCounter = new DefaultLimitCounter();
            _turnLimitCounters.put(prefix + card.getCardId(), limitCounter);
        }
        return limitCounter;
    }

    @Override
    public void addedWound(LotroPhysicalCard card) {
        final int cardId = card.getCardId();
        final Integer previousWounds = _woundsPerPhaseMap.get(cardId);
        if (previousWounds == null)
            _woundsPerPhaseMap.put(cardId, 1);
        else
            _woundsPerPhaseMap.put(cardId, previousWounds + 1);
    }

    private List<Modifier> getEffectModifiers(ModifierEffect modifierEffect) {
        List<Modifier> modifiers = _modifiers.get(modifierEffect);
        if (modifiers == null) {
            modifiers = new LinkedList<>();
            _modifiers.put(modifierEffect, modifiers);
        }
        return modifiers;
    }

    private void removeModifiers(List<Modifier> modifiers) {
        for (List<Modifier> list : _modifiers.values())
            list.removeAll(modifiers);
    }

    private void removeModifier(Modifier modifier) {
        for (List<Modifier> list : _modifiers.values())
            list.remove(modifier);
    }

    @Override
    public ModifierHook addAlwaysOnModifier(Modifier modifier) {
        addModifier(modifier);
        return new ModifierHookImpl(modifier);
    }

    private void addModifier(Modifier modifier) {
        ModifierEffect modifierEffect = modifier.getModifierEffect();
        getEffectModifiers(modifierEffect).add(modifier);
    }

    private List<Modifier> getModifiers(DefaultGame game, ModifierEffect modifierEffect) {
        return getKeywordModifiersAffectingCard(game, modifierEffect, null, null);
    }

    private List<Modifier> getModifiersAffectingCard(DefaultGame game, ModifierEffect modifierEffect, LotroPhysicalCard card) {
        return getKeywordModifiersAffectingCard(game, modifierEffect, null, card);
    }

    private List<Modifier> getKeywordModifiersAffectingCard(DefaultGame game, ModifierEffect modifierEffect, Keyword keyword, LotroPhysicalCard card) {
        List<Modifier> modifiers = _modifiers.get(modifierEffect);
        if (modifiers == null)
            return Collections.emptyList();
        else {
            LinkedList<Modifier> liveModifiers = new LinkedList<>();
            for (Modifier modifier : modifiers) {
                if (keyword == null || ((KeywordAffectingModifier) modifier).getKeyword() == keyword) {
                    if (!_skipSet.contains(modifier)) {
                        _skipSet.add(modifier);
                        Condition condition = modifier.getCondition();
                        if (condition == null || condition.isFullfilled(game))
                            if (modifierEffect == ModifierEffect.TEXT_MODIFIER || modifier.getSource() == null ||
                                    modifier.isNonCardTextModifier() ||
                                    !hasTextRemoved(game, modifier.getSource())) {
                                if (card == null || modifier.affectsCard(game, card))
                                    liveModifiers.add(modifier);
                            }
                        _skipSet.remove(modifier);
                    }
                }
            }

            return liveModifiers;
        }
    }

    public void signalEndOfPhase(Phase phase) {
        List<Modifier> list = _untilEndOfPhaseModifiers.get(phase);
        if (list != null) {
            removeModifiers(list);
            list.clear();
        }
        Map<String, LimitCounter> counterMap = _endOfPhaseLimitCounters.get(phase);
        if (counterMap != null)
            counterMap.clear();

        _drawnThisPhaseCount = 0;
        _woundsPerPhaseMap.clear();
    }

    public void signalStartOfPhase(Phase phase) {
        List<Modifier> list = _untilStartOfPhaseModifiers.get(phase);
        if (list != null) {
            removeModifiers(list);
            list.clear();
        }

        Map<String, LimitCounter> counterMap = _startOfPhaseLimitCounters.get(phase);
        if (counterMap != null)
            counterMap.clear();
    }

    public void signalEndOfTurn() {
        removeModifiers(_untilEndOfTurnModifiers);
        _untilEndOfTurnModifiers.clear();

        for (List<Modifier> modifiers : _untilStartOfPhaseModifiers.values())
            removeModifiers(modifiers);
        _untilStartOfPhaseModifiers.clear();

        for (List<Modifier> modifiers : _untilEndOfPhaseModifiers.values())
            removeModifiers(modifiers);
        _untilEndOfPhaseModifiers.clear();

        _turnLimitCounters.clear();
        _startOfPhaseLimitCounters.clear();
        _endOfPhaseLimitCounters.clear();
    }

    @Override
    public void addUntilEndOfPhaseModifier(Modifier modifier, Phase phase) {
        addModifier(modifier);
        List<Modifier> list = _untilEndOfPhaseModifiers.get(phase);
        if (list == null) {
            list = new LinkedList<>();
            _untilEndOfPhaseModifiers.put(phase, list);
        }
        list.add(modifier);
    }

    @Override
    public void addUntilStartOfPhaseModifier(Modifier modifier, Phase phase) {
        addModifier(modifier);
        List<Modifier> list = _untilStartOfPhaseModifiers.get(phase);
        if (list == null) {
            list = new LinkedList<>();
            _untilStartOfPhaseModifiers.put(phase, list);
        }
        list.add(modifier);
    }

    @Override
    public void addUntilEndOfTurnModifier(Modifier modifier) {
        addModifier(modifier);
        _untilEndOfTurnModifiers.add(modifier);
    }

    @Override
    public Collection<Modifier> getModifiersAffecting(DefaultGame game, LotroPhysicalCard card) {
        Set<Modifier> result = new HashSet<>();
        for (List<Modifier> modifiers : _modifiers.values()) {
            for (Modifier modifier : modifiers) {
                Condition condition = modifier.getCondition();
                if (condition == null || condition.isFullfilled(game))
                    if (affectsCardWithSkipSet(game, card, modifier))
                        result.add(modifier);
            }
        }
        return result;
    }

    @Override
    public Evaluator getFPStrengthOverrideEvaluator(DefaultGame game, LotroPhysicalCard fpCharacter) {
        for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.SKIRMISH_STRENGTH_EVALUATOR_MODIFIER, fpCharacter)) {
            Evaluator evaluator = modifier.getFpSkirmishStrengthOverrideEvaluator(game, fpCharacter);
            if (evaluator != null)
                return evaluator;
        }
        return null;
    }

    @Override
    public Evaluator getShadowStrengthOverrideEvaluator(DefaultGame game, LotroPhysicalCard shadowCharacter) {
        for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.SKIRMISH_STRENGTH_EVALUATOR_MODIFIER, shadowCharacter)) {
            Evaluator evaluator = modifier.getShadowSkirmishStrengthOverrideEvaluator(game, shadowCharacter);
            if (evaluator != null)
                return evaluator;
        }
        return null;
    }

    private boolean affectsCardWithSkipSet(DefaultGame game, LotroPhysicalCard physicalCard, Modifier modifier) {
        if (!_skipSet.contains(modifier) && physicalCard != null) {
            _skipSet.add(modifier);
            boolean result = modifier.affectsCard(game, physicalCard);
            _skipSet.remove(modifier);
            return result;
        } else {
            return false;
        }
    }

    @Override
    public boolean hasTextRemoved(DefaultGame game, LotroPhysicalCard card) {
        for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.TEXT_MODIFIER, card)) {
            if (modifier.hasRemovedText(game, card))
                return true;
        }
        return false;
    }

    private boolean hasAllKeywordsRemoved(DefaultGame game, LotroPhysicalCard card) {
        for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.LOSE_ALL_KEYWORDS_MODIFIER, card)) {
            if (modifier.lostAllKeywords(game, card))
                return true;
        }
        return false;
    }

    @Override
    public boolean hasKeyword(DefaultGame game, LotroPhysicalCard physicalCard, Keyword keyword) {
        LoggingThreadLocal.logMethodStart(physicalCard, "hasKeyword " + keyword.getHumanReadable());
        try {
            if (isCandidateForKeywordRemovalWithTextRemoval(game, physicalCard, keyword) &&
                    (hasTextRemoved(game, physicalCard) || hasAllKeywordsRemoved(game, physicalCard)))
                return false;

            for (Modifier modifier : getKeywordModifiersAffectingCard(game, ModifierEffect.REMOVE_KEYWORD_MODIFIER, keyword, physicalCard)) {
                if (modifier.isKeywordRemoved(game, physicalCard, keyword))
                    return false;
            }

            if (physicalCard.getBlueprint().hasKeyword(keyword))
                return true;

            for (Modifier modifier : getKeywordModifiersAffectingCard(game, ModifierEffect.GIVE_KEYWORD_MODIFIER, keyword, physicalCard)) {
                if (appliesKeywordModifier(game, physicalCard, modifier.getSource(), keyword))
                    if (modifier.hasKeyword(game, physicalCard, keyword))
                        return true;
            }
            return false;
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    @Override
    public int getKeywordCount(DefaultGame game, LotroPhysicalCard physicalCard, Keyword keyword) {
        LoggingThreadLocal.logMethodStart(physicalCard, "getKeywordCount " + keyword.getHumanReadable());
        try {
            if (isCandidateForKeywordRemovalWithTextRemoval(game, physicalCard, keyword)
                    && (hasTextRemoved(game, physicalCard) || hasAllKeywordsRemoved(game, physicalCard)))
                return 0;

            for (Modifier modifier : getKeywordModifiersAffectingCard(game, ModifierEffect.REMOVE_KEYWORD_MODIFIER, keyword, physicalCard)) {
                if (modifier.isKeywordRemoved(game, physicalCard, keyword))
                    return 0;
            }

            int result = physicalCard.getBlueprint().getKeywordCount(keyword);
            for (Modifier modifier : getKeywordModifiersAffectingCard(game, ModifierEffect.GIVE_KEYWORD_MODIFIER, keyword, physicalCard)) {
                if (appliesKeywordModifier(game, physicalCard, modifier.getSource(), keyword))
                    result += modifier.getKeywordCountModifier(game, physicalCard, keyword);
            }
            return Math.max(0, result);
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    private boolean isCandidateForKeywordRemovalWithTextRemoval(DefaultGame game, LotroPhysicalCard physicalCard, Keyword keyword) {
        if (keyword == Keyword.ROAMING)
            return false;
        if (keyword == Keyword.RING_BOUND)
            if (game.getGameState().getRingBearer(physicalCard.getOwner()) == physicalCard)
                return false;
        return true;
    }

    private boolean appliesKeywordModifier(DefaultGame game, LotroPhysicalCard affecting, LotroPhysicalCard modifierSource, Keyword keyword) {
        if (modifierSource == null)
            return true;
        for (Modifier modifier : getKeywordModifiersAffectingCard(game, ModifierEffect.CANCEL_KEYWORD_BONUS_TARGET_MODIFIER, keyword, affecting)) {
            if (!modifier.appliesKeywordModifier(game, modifierSource, keyword))
                return false;
        }
        return true;
    }

    @Override
    public boolean hasSignet(DefaultGame game, LotroPhysicalCard physicalCard, Signet signet) {
        LoggingThreadLocal.logMethodStart(physicalCard, "hasSignet " + signet);
        try {
            if (physicalCard.getBlueprint().getSignet() == signet)
                return true;
            for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.SIGNET_MODIFIER, physicalCard)) {
                if (modifier.hasSignet(game, physicalCard, signet))
                    return true;
            }
            return false;
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    @Override
    public int getArcheryTotal(DefaultGame game, Side side, int baseArcheryTotal) {
        int result = baseArcheryTotal;
        for (Modifier modifier : getModifiers(game, ModifierEffect.ARCHERY_MODIFIER))
            result += modifier.getArcheryTotalModifier(game, side);
        return Math.max(0, result);
    }

    @Override
    public int getMoveLimit(DefaultGame game, int baseMoveLimit) {
        int result = baseMoveLimit;
        for (Modifier modifier : getModifiers(game, ModifierEffect.MOVE_LIMIT_MODIFIER))
            result += modifier.getMoveLimitModifier(game);
        return Math.max(1, result);
    }

    @Override
    public boolean addsTwilightForCompanionMove(DefaultGame game, LotroPhysicalCard companion) {
        for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.MOVE_TWILIGHT_MODIFIER, companion)) {
            if (!modifier.addsTwilightForCompanionMove(game, companion))
                return false;
        }
        return true;
    }

    @Override
    public int getStrength(DefaultGame game, LotroPhysicalCard physicalCard) {
        LoggingThreadLocal.logMethodStart(physicalCard, "getStrength");
        try {
            int result = physicalCard.getBlueprint().getStrength();
            for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.STRENGTH_MODIFIER, physicalCard)) {
                final int strengthModifier = modifier.getStrengthModifier(game, physicalCard);
                if (strengthModifier <= 0 || appliesStrengthBonusModifier(game, modifier.getSource(), physicalCard))
                    result += strengthModifier;
            }
            return Math.max(0, result);
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    @Override
    public boolean appliesStrengthBonusModifier(DefaultGame game, LotroPhysicalCard modifierSource, LotroPhysicalCard modifierTarget) {
        if (modifierSource != null)
            for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.STRENGTH_BONUS_SOURCE_MODIFIER, modifierSource)) {
                if (!modifier.appliesStrengthBonusModifier(game, modifierSource, modifierTarget))
                    return false;
            }
        if (modifierTarget != null)
            for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.STRENGTH_BONUS_TARGET_MODIFIER, modifierTarget)) {
                if (!modifier.appliesStrengthBonusModifier(game, modifierSource, modifierTarget))
                    return false;
            }
        return true;
    }

    @Override
    public int getVitality(DefaultGame game, LotroPhysicalCard physicalCard) {
        LoggingThreadLocal.logMethodStart(physicalCard, "getVitality");
        try {
            int result = physicalCard.getBlueprint().getVitality() - game.getGameState().getWounds(physicalCard);
            for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.VITALITY_MODIFIER, physicalCard)) {
                result += modifier.getVitalityModifier(game, physicalCard);
            }
            return Math.max(0, result);
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    @Override
    public int getResistance(DefaultGame game, LotroPhysicalCard physicalCard) {
        LoggingThreadLocal.logMethodStart(physicalCard, "getResistance");
        try {
            int result = physicalCard.getBlueprint().getResistance();
            // Companions resistance is reduced by the number of burdens
            if (physicalCard.getBlueprint().getCardType() == CardType.COMPANION)
                result -= game.getGameState().getBurdens();
            for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.RESISTANCE_MODIFIER, physicalCard)) {
                result += modifier.getResistanceModifier(game, physicalCard);
            }
            return Math.max(0, result);
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    @Override
    public int getMinionSiteNumber(DefaultGame game, LotroPhysicalCard physicalCard) {
        LoggingThreadLocal.logMethodStart(physicalCard, "getMinionSiteNumber");
        try {
            int result = physicalCard.getBlueprint().getSiteNumber();
            for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.SITE_NUMBER_MODIFIER, physicalCard)) {
                result += modifier.getMinionSiteNumberModifier(game, physicalCard);
            }
            return Math.max(0, result);
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    @Override
    public int getTwilightCost(DefaultGame game, LotroPhysicalCard physicalCard, LotroPhysicalCard target, int twilightCostModifier, boolean ignoreRoamingPenalty) {
        LoggingThreadLocal.logMethodStart(physicalCard, "getTwilightCost");
        try {
            int result = physicalCard.getBlueprint().getTwilightCost() + twilightCostModifier;
            result += physicalCard.getBlueprint().getTwilightCostModifier(game, physicalCard, target);
            for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.TWILIGHT_COST_MODIFIER, physicalCard)) {
                result += modifier.getTwilightCostModifier(game, physicalCard, target, ignoreRoamingPenalty);
            }
            result = Math.max(0, result);

            if (!ignoreRoamingPenalty && hasKeyword(game, physicalCard, Keyword.ROAMING)) {
                int roamingPenalty = getRoamingPenalty(game, physicalCard);
                result += Math.max(0, roamingPenalty);
            }
            return result;
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    @Override
    public int getRoamingPenalty(DefaultGame game, LotroPhysicalCard physicalCard) {
        LoggingThreadLocal.logMethodStart(physicalCard, "getRoamingPenalty");
        try {
            int result = 2;
            for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.TWILIGHT_COST_MODIFIER, physicalCard)) {
                result += modifier.getRoamingPenaltyModifier(game, physicalCard);
            }
            return Math.max(0, result);
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    @Override
    public int getOverwhelmMultiplier(DefaultGame game, LotroPhysicalCard card) {
        LoggingThreadLocal.logMethodStart(card, "getOverwhelmMultiplier");
        try {
            int overwhelmMultiplier = 2;
            for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.OVERWHELM_MODIFIER, card)) {
                overwhelmMultiplier = Math.max(overwhelmMultiplier, modifier.getOverwhelmMultiplier(game, card));
            }
            return overwhelmMultiplier;
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    @Override
    public boolean isAdditionalCardType(DefaultGame game, LotroPhysicalCard card, CardType cardType) {
        for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.ADDITIONAL_CARD_TYPE, card))
            if (modifier.isAdditionalCardTypeModifier(game, card, cardType))
                return true;
        return false;
    }

    @Override
    public int getWoundsTakenInCurrentPhase(LotroPhysicalCard card) {
        Integer wounds = _woundsPerPhaseMap.get(card.getCardId());
        if (wounds == null)
            return 0;
        return wounds;
    }

    @Override
    public boolean canTakeWounds(DefaultGame game, Collection<LotroPhysicalCard> woundSources, LotroPhysicalCard card, int woundsToTake) {
        LoggingThreadLocal.logMethodStart(card, "canTakeWound");
        try {
            for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.WOUND_MODIFIER, card)) {
                Integer woundsTaken = _woundsPerPhaseMap.get(card.getCardId());
                if (woundsTaken == null)
                    woundsTaken = 0;
                if (!modifier.canTakeWounds(game, woundSources, card, woundsTaken, woundsToTake))
                    return false;
            }
            return true;
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    @Override
    public boolean canTakeWoundsFromLosingSkirmish(DefaultGame game, LotroPhysicalCard card, Set<LotroPhysicalCard> winners) {
        LoggingThreadLocal.logMethodStart(card, "canTakeWound");
        try {
            for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.WOUND_MODIFIER, card)) {
                if (!modifier.canTakeWoundsFromLosingSkirmish(game, card, winners))
                    return false;
            }
            return true;
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    @Override
    public boolean canTakeArcheryWound(DefaultGame game, LotroPhysicalCard card) {
        LoggingThreadLocal.logMethodStart(card, "canTakeArcheryWound");
        try {
            for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.WOUND_MODIFIER, card)) {
                if (!modifier.canTakeArcheryWound(game, card))
                    return false;
            }
            return true;
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    @Override
    public boolean canBeExerted(DefaultGame game, LotroPhysicalCard exertionSource, LotroPhysicalCard exertedCard) {
        LoggingThreadLocal.logMethodStart(exertedCard, "canBeExerted");
        try {
            for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.WOUND_MODIFIER, exertedCard)) {
                if (!modifier.canBeExerted(game, exertionSource, exertedCard))
                    return false;
            }
            return true;
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    @Override
    public boolean isUnhastyCompanionAllowedToParticipateInSkirmishes(DefaultGame game, LotroPhysicalCard card) {
        LoggingThreadLocal.logMethodStart(card, "isAllyAllowedToParticipateInSkirmishes");
        try {
            for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.PRESENCE_MODIFIER, card)) {
                if (modifier.isUnhastyCompanionAllowedToParticipateInSkirmishes(game, card))
                    return true;
            }
            return false;
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    @Override
    public boolean isAllyAllowedToParticipateInArcheryFire(DefaultGame game, LotroPhysicalCard card) {
        LoggingThreadLocal.logMethodStart(card, "isAllyAllowedToParticipateInArcheryFire");
        try {
            for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.PRESENCE_MODIFIER, card)) {
                if (modifier.isAllyParticipateInArcheryFire(game, card))
                    return true;
            }
            return false;
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    @Override
    public boolean isAllyAllowedToParticipateInSkirmishes(DefaultGame game, Side sidePlayer, LotroPhysicalCard card) {
        LoggingThreadLocal.logMethodStart(card, "isAllyAllowedToParticipateInSkirmishes");
        try {
            for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.PRESENCE_MODIFIER, card)) {
                if (modifier.isAllyParticipateInSkirmishes(game, sidePlayer, card))
                    return true;
            }
            return false;
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    @Override
    public boolean isAllyPreventedFromParticipatingInArcheryFire(DefaultGame game, LotroPhysicalCard card) {
        LoggingThreadLocal.logMethodStart(card, "isAllyPreventedFromParticipatingInArcheryFire");
        try {
            for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.PRESENCE_MODIFIER, card)) {
                if (modifier.isAllyPreventedFromParticipatingInArcheryFire(game, card))
                    return true;
            }
            return false;
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    @Override
    public boolean isAllyPreventedFromParticipatingInSkirmishes(DefaultGame game, Side sidePlayer, LotroPhysicalCard card) {
        LoggingThreadLocal.logMethodStart(card, "isAllyPreventedFromParticipatingInSkirmishes");
        try {
            for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.PRESENCE_MODIFIER, card)) {
                if (modifier.isAllyPreventedFromParticipatingInSkirmishes(game, sidePlayer, card))
                    return true;
            }
            return false;
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    @Override
    public boolean addsToArcheryTotal(DefaultGame game, LotroPhysicalCard card) {
        LoggingThreadLocal.logMethodStart(card, "addsToArcheryTotal");
        try {
            for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.ARCHERY_MODIFIER, card))
                if (!modifier.addsToArcheryTotal(game, card))
                    return false;

            return true;
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    @Override
    public boolean canPlayAction(DefaultGame game, String performingPlayer, Action action) {
        for (Modifier modifier : getModifiers(game, ModifierEffect.ACTION_MODIFIER))
            if (!modifier.canPlayAction(game, performingPlayer, action))
                return false;
        return true;
    }

    @Override
    public boolean canPlayCard(DefaultGame game, String performingPlayer, LotroPhysicalCard card) {
        for (Modifier modifier : getModifiers(game, ModifierEffect.ACTION_MODIFIER))
            if (!modifier.canPlayCard(game, performingPlayer, card))
                return false;
        return true;
    }

    @Override
    public boolean canCancelSkirmish(DefaultGame game, LotroPhysicalCard card) {
        for (Modifier modifier : getModifiers(game, ModifierEffect.CANCEL_SKIRMISH_MODIFIER))
            if (!modifier.canCancelSkirmish(game, card))
                return false;
        return true;
    }

    @Override
    public boolean canHavePlayedOn(DefaultGame game, LotroPhysicalCard playedCard, LotroPhysicalCard target) {
        for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.TARGET_MODIFIER, target))
            if (!modifier.canHavePlayedOn(game, playedCard, target))
                return false;
        return true;
    }

    @Override
    public boolean canHaveTransferredOn(DefaultGame game, LotroPhysicalCard playedCard, LotroPhysicalCard target) {
        for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.TARGET_MODIFIER, target))
            if (!modifier.canHaveTransferredOn(game, playedCard, target))
                return false;
        return true;
    }

    @Override
    public boolean canBeTransferred(DefaultGame game, LotroPhysicalCard attachment) {
        for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.TRANSFER_MODIFIER, attachment))
            if (!modifier.canBeTransferred(game, attachment))
                return false;
        return true;
    }

    @Override
    public boolean shouldSkipPhase(DefaultGame game, Phase phase, String playerId) {
        for (Modifier modifier : getModifiers(game, ModifierEffect.ACTION_MODIFIER))
            if (modifier.shouldSkipPhase(game, phase, playerId))
                return true;
        return false;
    }

    @Override
    public List<? extends Action> getExtraPhaseActions(DefaultGame game, LotroPhysicalCard target) {
        List<Action> activateCardActions = new LinkedList<>();

        for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.EXTRA_ACTION_MODIFIER, target)) {
            List<? extends Action> actions = modifier.getExtraPhaseAction(game, target);
            if (actions != null)
                activateCardActions.addAll(actions);
        }

        return activateCardActions;
    }

    @Override
    public List<? extends Action> getExtraPhaseActionsFromStacked(DefaultGame game, LotroPhysicalCard target) {
        List<Action> activateCardActions = new LinkedList<>();

        for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.EXTRA_ACTION_MODIFIER, target)) {
            List<? extends Action> actions = modifier.getExtraPhaseActionFromStacked(game, target);
            if (actions != null)
                activateCardActions.addAll(actions);
        }

        return activateCardActions;
    }

    @Override
    public boolean canPayExtraCostsToPlay(DefaultGame game, LotroPhysicalCard target) {
        final List<? extends ExtraPlayCost> playCosts = target.getBlueprint().getExtraCostToPlay(game, target);
        if (playCosts != null)
            for (ExtraPlayCost playCost : playCosts) {
                final Condition condition = playCost.getCondition();
                if ((condition == null || condition.isFullfilled(game)) && !playCost.canPayExtraCostsToPlay(game, target))
                    return false;
            }

        for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.EXTRA_COST_MODIFIER, target)) {
            if (!modifier.canPayExtraCostsToPlay(game, target))
                return false;
        }

        return true;
    }

    @Override
    public void appendExtraCosts(DefaultGame game, CostToEffectAction action, LotroPhysicalCard target) {
        final List<? extends ExtraPlayCost> playCosts = target.getBlueprint().getExtraCostToPlay(game, target);
        if (playCosts != null)
            for (ExtraPlayCost playCost : playCosts) {
                final Condition condition = playCost.getCondition();
                if (condition == null || condition.isFullfilled(game))
                    playCost.appendExtraCosts(game, action, target);
            }

        for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.EXTRA_COST_MODIFIER, target)) {
            modifier.appendExtraCosts(game, action, target);
        }
    }

    @Override
    public boolean isValidAssignments(DefaultGame game, Side side, Map<LotroPhysicalCard, Set<LotroPhysicalCard>> assignments) {
        for (Modifier modifier : getModifiers(game, ModifierEffect.ASSIGNMENT_MODIFIER)) {
            if (!modifier.isValidAssignments(game, side, assignments))
                return false;
            for (Map.Entry<LotroPhysicalCard, Set<LotroPhysicalCard>> assignment : assignments.entrySet()) {
                if (affectsCardWithSkipSet(game, assignment.getKey(), modifier))
                    if (!modifier.isValidAssignments(game, side, assignment.getKey(), assignment.getValue()))
                        return false;
            }
        }
        return true;
    }

    @Override
    public boolean canBeAssignedToSkirmish(DefaultGame game, Side sidePlayer, LotroPhysicalCard card) {
        LoggingThreadLocal.logMethodStart(card, "canBeAssignedToSkirmish");
        try {
            for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.ASSIGNMENT_MODIFIER, card))
                if (modifier.isPreventedFromBeingAssignedToSkirmish(game, sidePlayer, card))
                    return false;
            return true;
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    @Override
    public boolean canBeDiscardedFromPlay(DefaultGame game, String performingPlayer, LotroPhysicalCard card, LotroPhysicalCard source) {
        LoggingThreadLocal.logMethodStart(card, "canBeDiscardedFromPlay");
        try {
            for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.DISCARD_FROM_PLAY_MODIFIER, card))
                if (!modifier.canBeDiscardedFromPlay(game, performingPlayer, card, source))
                    return false;
            return true;
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    @Override
    public boolean canBeReturnedToHand(DefaultGame game, LotroPhysicalCard card, LotroPhysicalCard source) {
        LoggingThreadLocal.logMethodStart(card, "canBeReturnedToHand");
        try {
            for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.RETURN_TO_HAND_MODIFIER, card))
                if (!modifier.canBeReturnedToHand(game, card, source))
                    return false;
            return true;
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    @Override
    public boolean canBeHealed(DefaultGame game, LotroPhysicalCard card) {
        LoggingThreadLocal.logMethodStart(card, "canBeHealed");
        try {
            for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.WOUND_MODIFIER, card))
                if (!modifier.canBeHealed(game, card))
                    return false;
            return true;
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    @Override
    public boolean canAddBurden(DefaultGame game, String performingPlayer, LotroPhysicalCard source) {
        for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.BURDEN_MODIFIER, source)) {
            if (!modifier.canAddBurden(game, performingPlayer, source))
                return false;
        }
        return true;
    }

    @Override
    public boolean canRemoveBurden(DefaultGame game, LotroPhysicalCard source) {
        for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.BURDEN_MODIFIER, source)) {
            if (!modifier.canRemoveBurden(game, source))
                return false;
        }
        return true;
    }

    @Override
    public boolean canRemoveThreat(DefaultGame game, LotroPhysicalCard source) {
        for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.THREAT_MODIFIER, source)) {
            if (!modifier.canRemoveThreat(game, source))
                return false;
        }
        return true;
    }

    /**
     * Rule of 4. "You cannot draw (or take into hand) more than 4 cards during your fellowship phase."
     *
     * @param game
     * @param playerId
     * @return
     */
    @Override
    public boolean canDrawCardNoIncrement(DefaultGame game, String playerId) {
        if (game.getGameState().getCurrentPlayerId().equals(playerId)) {
            if (game.getGameState().getCurrentPhase() != Phase.FELLOWSHIP)
                return true;
            if (game.getGameState().getCurrentPhase() == Phase.FELLOWSHIP && _drawnThisPhaseCount < 4) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Rule of 4. "You cannot draw (or take into hand) more than 4 cards during your fellowship phase."
     *
     * @param game
     * @param playerId
     * @return
     */
    @Override
    public boolean canDrawCardAndIncrementForRuleOfFour(DefaultGame game, String playerId) {
        if (game.getGameState().getCurrentPlayerId().equals(playerId)) {
            if (game.getGameState().getCurrentPhase() != Phase.FELLOWSHIP)
                return true;
            if (game.getGameState().getCurrentPhase() == Phase.FELLOWSHIP && _drawnThisPhaseCount < 4) {
                _drawnThisPhaseCount++;
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canLookOrRevealCardsInHand(DefaultGame game, String revealingPlayerId, String performingPlayerId) {
        for (Modifier modifier : getModifiers(game, ModifierEffect.LOOK_OR_REVEAL_MODIFIER))
            if (!modifier.canLookOrRevealCardsInHand(game, revealingPlayerId, performingPlayerId))
                return false;
        return true;
    }

    @Override
    public boolean canDiscardCardsFromHand(DefaultGame game, String playerId, LotroPhysicalCard source) {
        for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.DISCARD_NOT_FROM_PLAY, source))
            if (!modifier.canDiscardCardsFromHand(game, playerId, source))
                return false;
        return true;
    }

    @Override
    public boolean canDiscardCardsFromTopOfDeck(DefaultGame game, String playerId, LotroPhysicalCard source) {
        for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.DISCARD_NOT_FROM_PLAY, source))
            if (!modifier.canDiscardCardsFromTopOfDeck(game, playerId, source))
                return false;
        return true;
    }

    @Override
    public boolean canBeLiberated(DefaultGame game, String playerId, LotroPhysicalCard card, LotroPhysicalCard source) {
        LoggingThreadLocal.logMethodStart(card, "canBeLiberated");
        try {
            for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.LIBERATION_MODIFIER, card))
                if (!modifier.canBeLiberated(game, playerId, card, source))
                    return false;
            return true;
        } finally {
            LoggingThreadLocal.logMethodEnd();
        }
    }

    @Override
    public Side hasInitiative(DefaultGame game) {
        for (Modifier modifier : getModifiers(game, ModifierEffect.INITIATIVE_MODIFIER)) {
            if (!modifier.shadowCanHaveInitiative(game))
                return Side.FREE_PEOPLE;
        }

        for (Modifier modifier : getModifiers(game, ModifierEffect.INITIATIVE_MODIFIER)) {
            Side initiative = modifier.hasInitiative(game);
            if (initiative != null)
                return initiative;
        }

        int freePeopleInitiativeHandSize = game.getGameState().getHand(game.getGameState().getCurrentPlayerId()).size()
                + game.getGameState().getVoidFromHand(game.getGameState().getCurrentPlayerId()).size();

        int initiativeHandSize = 4;
        for (Modifier modifier : getModifiers(game, ModifierEffect.INITIATIVE_MODIFIER))
            initiativeHandSize += modifier.getInitiativeHandSizeModifier(game);

        if (freePeopleInitiativeHandSize < initiativeHandSize)
            return Side.SHADOW;
        else
            return Side.FREE_PEOPLE;
    }

    @Override
    public int getNumberOfSpottableFPCultures(DefaultGame game, String playerId) {
        Set<Culture> spottableCulturesBasedOnCards = new HashSet<>();
        for (LotroPhysicalCard spottableFPCard : Filters.filterActive(game, Side.FREE_PEOPLE, Filters.spottable)) {
            final Culture fpCulture = spottableFPCard.getBlueprint().getCulture();
            if (fpCulture != null)
                spottableCulturesBasedOnCards.add(fpCulture);
        }

        int result = 0;
        for (Culture spottableCulturesBasedOnCardsOnCard : spottableCulturesBasedOnCards) {
            if (canPlayerSpotCulture(game, playerId, spottableCulturesBasedOnCardsOnCard))
                result++;
        }

        for (Modifier modifier : getModifiers(game, ModifierEffect.SPOT_MODIFIER))
            result += modifier.getFPCulturesSpotCountModifier(game, playerId);

        return result;
    }

    private boolean canPlayerSpotCulture(DefaultGame game, String playerId, Culture culture) {
        for (Modifier modifier : getModifiers(game, ModifierEffect.SPOT_MODIFIER))
            if (!modifier.canSpotCulture(game, culture, playerId))
                return false;
        return true;
    }

    @Override
    public int getNumberOfSpottableShadowCultures(DefaultGame game, String playerId) {
        Set<Culture> spottableCulturesBasedOnCards = new HashSet<>();
        for (LotroPhysicalCard spottableFPCard : Filters.filterActive(game, Side.SHADOW, Filters.spottable)) {
            final Culture fpCulture = spottableFPCard.getBlueprint().getCulture();
            if (fpCulture != null)
                spottableCulturesBasedOnCards.add(fpCulture);
        }

        int result = 0;
        for (Culture spottableCulturesBasedOnCardsOnCard : spottableCulturesBasedOnCards) {
            if (canPlayerSpotCulture(game, playerId, spottableCulturesBasedOnCardsOnCard))
                result++;
        }

        return result;
    }

    @Override
    public int getSpotBonus(DefaultGame game, Filterable filter) {
        int result = 0;
        for (Modifier modifier : getModifiers(game, ModifierEffect.SPOT_MODIFIER))
            result += modifier.getSpotCountModifier(game, filter);
        return Math.max(0, result);
    }

    @Override
    public boolean hasFlagActive(DefaultGame game, ModifierFlag modifierFlag) {
        for (Modifier modifier : getModifiers(game, ModifierEffect.SPECIAL_FLAG_MODIFIER))
            if (modifier.hasFlagActive(game, modifierFlag))
                return true;

        return false;
    }

    @Override
    public boolean canReplaceSite(DefaultGame game, String playerId, LotroPhysicalCard siteToReplace) {
        for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.REPLACE_SITE_MODIFIER, siteToReplace))
            if (!modifier.isSiteReplaceable(game, playerId))
                return false;

        return true;
    }

    @Override
    public boolean canPlaySite(DefaultGame game, String playerId) {
        for (Modifier modifier : getModifiers(game, ModifierEffect.PLAY_SITE_MODIFIER))
            if (!modifier.canPlaySite(game, playerId))
                return false;

        return true;
    }

    @Override
    public int getSanctuaryHealModifier(DefaultGame game) {
        int result = 0;
        for (Modifier modifier : getModifiers(game, ModifierEffect.SANCTUARY_HEAL_MODIFIER))
            result += modifier.getSanctuaryHealModifier(game);

        return result;
    }

    @Override
    public int getPotentialDiscount(DefaultGame game, LotroPhysicalCard playedCard) {
        int result = playedCard.getBlueprint().getPotentialDiscount(game, playedCard.getOwner(), playedCard);

        for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.POTENTIAL_DISCOUNT_MODIFIER, playedCard)) {
            result += modifier.getPotentialDiscount(game, playedCard);
        }

        return result;
    }

    @Override
    public void appendPotentialDiscounts(DefaultGame game, CostToEffectAction action, LotroPhysicalCard playedCard) {
        playedCard.getBlueprint().appendPotentialDiscountEffects(game, action, playedCard.getOwner(), playedCard);

        for (Modifier modifier : getModifiersAffectingCard(game, ModifierEffect.POTENTIAL_DISCOUNT_MODIFIER, playedCard)) {
            modifier.appendPotentialDiscounts(game, action, playedCard);
        }
    }

    private class ModifierHookImpl implements ModifierHook {
        private final Modifier _modifier;

        private ModifierHookImpl(Modifier modifier) {
            _modifier = modifier;
        }

        @Override
        public void stop() {
            removeModifier(_modifier);
        }
    }
}
