package com.gempukku.lotro.filters;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.cards.CompletePhysicalCardVisitor;
import com.gempukku.lotro.cards.LotroCardBlueprint;
import com.gempukku.lotro.cards.PhysicalCardVisitor;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.processes.lotronly.skirmish.Skirmish;
import com.gempukku.lotro.rules.lotronly.LotroGameUtils;
import com.gempukku.lotro.rules.lotronly.LotroPlayUtils;
import com.gempukku.lotro.condition.Condition;
import com.gempukku.lotro.evaluator.Evaluator;
import com.gempukku.lotro.rules.RuleUtils;

import java.util.*;

public class Filters {
    private static final Map<CardType, Filter> _typeFilterMap = new HashMap<>();
    private static final Map<PossessionClass, Filter> _possessionClassFilterMap = new HashMap<>();
    private static final Map<Signet, Filter> _signetFilterMap = new HashMap<>();
    private static final Map<Race, Filter> _raceFilterMap = new HashMap<>();
    private static final Map<Zone, Filter> _zoneFilterMap = new HashMap<>();
    private static final Map<Side, Filter> _sideFilterMap = new HashMap<>();
    private static final Map<Culture, Filter> _cultureFilterMap = new HashMap<>();
    private static final Map<Keyword, Filter> _keywordFilterMap = new HashMap<>();

    static {
        for (Culture culture : Culture.values())
            _cultureFilterMap.put(culture, culture(culture));
        for (Side side : Side.values())
            _sideFilterMap.put(side, side(side));
        for (Zone zone : Zone.values())
            _zoneFilterMap.put(zone, zone(zone));
        for (CardType cardType : CardType.values())
            _typeFilterMap.put(cardType, type(cardType));
        for (Race race : Race.values())
            _raceFilterMap.put(race, race(race));
        for (Signet signet : Signet.values())
            _signetFilterMap.put(signet, signet(signet));
        for (PossessionClass possessionClass : PossessionClass.values())
            _possessionClassFilterMap.put(possessionClass, possessionClass(possessionClass));
        for (Keyword keyword : Keyword.values())
            _keywordFilterMap.put(keyword, keyword(keyword));

        // Some simple shortcuts for filters
        // Only companions can be rangers
        _keywordFilterMap.put(Keyword.RANGER, Filters.and(CardType.COMPANION, keyword(Keyword.RANGER)));
        // Only allies can be villagers
        _keywordFilterMap.put(Keyword.VILLAGER, Filters.and(CardType.ALLY, keyword(Keyword.VILLAGER)));

        // Minion groups
        _keywordFilterMap.put(Keyword.SOUTHRON, Filters.and(CardType.MINION, keyword(Keyword.SOUTHRON)));
        _keywordFilterMap.put(Keyword.EASTERLING, Filters.and(CardType.MINION, keyword(Keyword.EASTERLING)));
        _keywordFilterMap.put(Keyword.CORSAIR, Filters.and(CardType.MINION, keyword(Keyword.CORSAIR)));
        _keywordFilterMap.put(Keyword.TRACKER, Filters.and(CardType.MINION, keyword(Keyword.TRACKER)));
        _keywordFilterMap.put(Keyword.WARG_RIDER, Filters.and(CardType.MINION, keyword(Keyword.WARG_RIDER)));
        _keywordFilterMap.put(Keyword.BESIEGER, Filters.and(CardType.MINION, keyword(Keyword.BESIEGER)));
    }

    public static boolean canSpot(DefaultGame game, Filterable... filters) {
        return canSpot(game, 1, filters);
    }

    public static boolean canSpot(DefaultGame game, int count, Filterable... filters) {
        return countSpottable(game, filters)>=count;
    }

    public static Collection<LotroPhysicalCard> filterActive(DefaultGame game, Filterable... filters) {
        Filter filter = Filters.and(filters);
        GetCardsMatchingFilterVisitor getCardsMatchingFilter = new GetCardsMatchingFilterVisitor(game, filter);
        game.getGameState().iterateActiveCards(getCardsMatchingFilter);
        return getCardsMatchingFilter.getPhysicalCards();
    }

    public static Collection<LotroPhysicalCard> filter(Iterable<? extends LotroPhysicalCard> cards, DefaultGame game, Filterable... filters) {
        Filter filter = Filters.and(filters);
        List<LotroPhysicalCard> result = new LinkedList<>();
        for (LotroPhysicalCard card : cards) {
            if (filter.accepts(game, card))
                result.add(card);
        }
        return result;
    }

    public static LotroPhysicalCard findFirstActive(DefaultGame game, Filterable... filters) {
        FindFirstActiveCardInPlayVisitor visitor = new FindFirstActiveCardInPlayVisitor(game, Filters.and(filters));
        game.getGameState().iterateActiveCards(visitor);
        return visitor.getCard();
    }

    public static int countSpottable(DefaultGame game, Filterable... filters) {
        GetCardsMatchingFilterVisitor matchingFilterVisitor = new GetCardsMatchingFilterVisitor(game, Filters.and(filters, Filters.spottable));
        game.getGameState().iterateActiveCards(matchingFilterVisitor);
        int result = matchingFilterVisitor.getCounter();
        if (filters.length==1)
            result+=game.getModifiersQuerying().getSpotBonus(game, filters[0]);
        return result;
    }

    public static int countActive(DefaultGame game, Filterable... filters) {
        GetCardsMatchingFilterVisitor matchingFilterVisitor = new GetCardsMatchingFilterVisitor(game, Filters.and(filters));
        game.getGameState().iterateActiveCards(matchingFilterVisitor);
        return matchingFilterVisitor.getCounter();
    }

    // Filters available

    public static Filter conditionFilter(final Filterable defaultFilters, final Condition condition, final Filterable conditionMetFilter) {
        final Filter filter1 = changeToFilter(defaultFilters);
        final Filter filter2 = changeToFilter(conditionMetFilter);
        return (game, physicalCard) -> {
            if (condition.isFullfilled(game))
                return filter2.accepts(game, physicalCard);
            else
                return filter1.accepts(game, physicalCard);
        };
    }

    public static Filter canSpotCompanionWithStrengthAtLeast(final int strength) {
        return (game, physicalCard) -> Filters.canSpot(game, CardType.COMPANION, Filters.not(Filters.lessStrengthThan(strength)));
    }

    public static Filter lessVitalityThan(final int vitality) {
        return (game, physicalCard) -> game.getModifiersQuerying().getVitality(game, physicalCard) < vitality;
    }

    public static Filter moreVitalityThan(final int vitality) {
        return (game, physicalCard) -> game.getModifiersQuerying().getVitality(game, physicalCard) > vitality;
    }

    public static Filter maxResistance(final int resistance) {
        return (game, physicalCard) -> game.getModifiersQuerying().getResistance(game, physicalCard) <= resistance;
    }

    public static Filter minResistance(final int resistance) {
        return (game, physicalCard) -> game.getModifiersQuerying().getResistance(game, physicalCard) >= resistance;
    }

    public static Filter minVitality(final int vitality) {
        return (game, physicalCard) -> game.getModifiersQuerying().getVitality(game, physicalCard) >= vitality;
    }

    public static Filter strengthEqual(final Evaluator evaluator) {
        return (game, physicalCard) -> game.getModifiersQuerying().getStrength(game, physicalCard) == evaluator.evaluateExpression(game, null);
    }

    public static Filter moreStrengthThan(final int strength) {
        return (game, physicalCard) -> game.getModifiersQuerying().getStrength(game, physicalCard) > strength;
    }

    public static Filter lessStrengthThan(final int strength) {
        return (game, physicalCard) -> game.getModifiersQuerying().getStrength(game, physicalCard) < strength;
    }

    public static Filter lessStrengthThan(final LotroPhysicalCard card) {
        return (game, physicalCard) -> game.getModifiersQuerying().getStrength(game, physicalCard) < game.getModifiersQuerying().getStrength(game, card);
    }


    private static Filter possessionClass(final PossessionClass possessionClass) {
        return (game, physicalCard) -> {
            final Set<PossessionClass> possessionClasses = physicalCard.getBlueprint().getPossessionClasses();
            return possessionClasses != null && possessionClasses.contains(possessionClass);
        };
    }

    public static Filter hasAnyCultureTokens() {
        return hasAnyCultureTokens(1);
    }

    public static Filter hasAnyCultureTokens(final int count) {
        return (game, physicalCard) -> {
            Map<Token, Integer> tokens = game.getGameState().getTokens(physicalCard);
            for (Map.Entry<Token, Integer> tokenCount : tokens.entrySet()) {
                if (tokenCount.getKey().getCulture() != null)
                    if (tokenCount.getValue() >= count)
                        return true;
            }
            return false;
        };
    }

    public static Filter printedTwilightCost(final int printedTwilightCost) {
        return (game, physicalCard) -> physicalCard.getBlueprint().getTwilightCost() == printedTwilightCost;
    }

    public static Filter maxPrintedTwilightCost(final int printedTwilightCost) {
        return (game, physicalCard) -> physicalCard.getBlueprint().getTwilightCost() <= printedTwilightCost;
    }

    public static Filter minPrintedTwilightCost(final int printedTwilightCost) {
        return (game, physicalCard) -> physicalCard.getBlueprint().getTwilightCost() >= printedTwilightCost;
    }

    public static Filter hasToken(final Token token) {
        return hasToken(token, 1);
    }

    public static Filter hasToken(final Token token, final int count) {
        return (game, physicalCard) -> game.getGameState().getTokenCount(physicalCard, token) >= count;
    }

    public static Filter assignableToSkirmishAgainst(final Side assignedBySide, final Filterable againstFilter) {
        return assignableToSkirmishAgainst(assignedBySide, againstFilter, false, false);
    }

    public static Filter notPreventedByEffectToAssign(final Side assignedBySide, final LotroPhysicalCard againstCard) {
        return (game, physicalCard) -> {
            if (againstCard.getBlueprint().getSide() == Side.FREE_PEOPLE) {
                Map<LotroPhysicalCard, Set<LotroPhysicalCard>> assignment = new HashMap<>();
                assignment.put(againstCard, Collections.singleton(physicalCard));
                return game.getModifiersQuerying().isValidAssignments(game, assignedBySide, assignment);
            } else {
                Map<LotroPhysicalCard, Set<LotroPhysicalCard>> assignment = new HashMap<>();
                assignment.put(physicalCard, Collections.singleton(againstCard));
                return game.getModifiersQuerying().isValidAssignments(game, assignedBySide, assignment);
            }
        };
    }

    public static Filter assignableToSkirmishAgainst(final Side assignedBySide, final Filterable againstFilter, final boolean ignoreUnassigned, final boolean allowAllyToSkirmish) {
        return Filters.and(
                assignableToSkirmish(assignedBySide, ignoreUnassigned, allowAllyToSkirmish),
                (Filter) (game, physicalCard) -> {
                    for (LotroPhysicalCard card : Filters.filterActive(game, againstFilter)) {
                        if (card.getBlueprint().getSide() != physicalCard.getBlueprint().getSide()
                                && Filters.assignableToSkirmish(assignedBySide, ignoreUnassigned, allowAllyToSkirmish).accepts(game, card)) {
                            Map<LotroPhysicalCard, Set<LotroPhysicalCard>> thisAssignment = new HashMap<>();
                            if (card.getBlueprint().getSide() == Side.FREE_PEOPLE) {
                                if (thisAssignment.containsKey(card))
                                    thisAssignment.get(card).add(physicalCard);
                                else
                                    thisAssignment.put(card, Collections.singleton(physicalCard));
                            } else {
                                if (thisAssignment.containsKey(physicalCard))
                                    thisAssignment.get(physicalCard).add(card);
                                else
                                    thisAssignment.put(physicalCard, Collections.singleton(card));
                            }
                            if (game.getModifiersQuerying().isValidAssignments(game, assignedBySide, thisAssignment))
                                return true;
                        }
                    }

                    return false;
                });
    }

    public static Filter assignableToSkirmish(final Side assignedBySide, final boolean ignoreUnassigned, final boolean allowAllyToSkirmish) {
        Filter assignableFilter = Filters.or(
                Filters.and(
                        CardType.ALLY,
                        (Filter) (game, physicalCard) -> {
                            if (allowAllyToSkirmish)
                                return true;
                            boolean allowedToSkirmish = game.getModifiersQuerying().isAllyAllowedToParticipateInSkirmishes(game, assignedBySide, physicalCard);
                            if (allowedToSkirmish)
                                return true;
                            boolean preventedByCard = game.getModifiersQuerying().isAllyPreventedFromParticipatingInSkirmishes(game, assignedBySide, physicalCard);
                            if (preventedByCard)
                                return false;
                            return RuleUtils.isAllyAtHome(physicalCard, game.getGameState().getCurrentSiteNumber(), game.getGameState().getCurrentSiteBlock());
                        }),
                Filters.and(
                        CardType.COMPANION,
                        (Filter) (game, physicalCard) -> assignedBySide == Side.SHADOW || !game.getModifiersQuerying().hasKeyword(game, physicalCard, Keyword.UNHASTY)
                                || game.getModifiersQuerying().isUnhastyCompanionAllowedToParticipateInSkirmishes(game, physicalCard)),
                Filters.and(
                        CardType.MINION,
                        (Filter) (game, physicalCard) -> (!game.getGameState().isFierceSkirmishes()) || game.getModifiersQuerying().hasKeyword(game, physicalCard, Keyword.FIERCE)));

        return Filters.and(
                assignableFilter,
                (Filter) (game, physicalCard) -> {
                    if (!ignoreUnassigned) {
                        boolean notAssignedToSkirmish = true;
                        if (!notAssignedToSkirmish)
                            return false;
                    }
                    return game.getModifiersQuerying().canBeAssignedToSkirmish(game, assignedBySide, physicalCard);
                });
    }

    public static Filter siteBlock(final SitesBlock block) {
        return (game, physicalCard) -> physicalCard.getBlueprint().getSiteBlock() == block;
    }

    public static final Filter saruman = Filters.name("Saruman");
    public static final Filter witchKing = Filters.name(Names.witchKing);
    public static final Filter balrog = Filters.name("The Balrog");

    public static final Filter gollum = Filters.name("Gollum");
    public static final Filter smeagol = Filters.name("Smeagol");
    public static final Filter gollumOrSmeagol = Filters.or(gollum, smeagol);

    public static final Filter aragorn = Filters.name("Aragorn");
    public static final Filter gandalf = Filters.name("Gandalf");
    public static final Filter gimli = Filters.name("Gimli");
    public static final Filter arwen = Filters.name("Arwen");
    public static final Filter legolas = Filters.name("Legolas");
    public static final Filter boromir = Filters.name("Boromir");
    public static final Filter frodo = Filters.name("Frodo");
    public static final Filter sam = Filters.name("Sam");

    public static final Filter weapon = Filters.or(PossessionClass.HAND_WEAPON, PossessionClass.RANGED_WEAPON);
    public static final Filter item = Filters.or(CardType.ARTIFACT, CardType.POSSESSION);
    public static final Filter character = Filters.or(CardType.ALLY, CardType.COMPANION, CardType.MINION);

    public static final Filter inSkirmish = (game, physicalCard) -> {
        Skirmish skirmish = game.getGameState().getSkirmish();
        if (skirmish != null) {
            return (skirmish.getFellowshipCharacter() == physicalCard)
                    || skirmish.getShadowCharacters().contains(physicalCard);
        }
        return false;
    };

    public static final Filter inFierceSkirmish = (game, physicalCard) -> {
        Skirmish skirmish = game.getGameState().getSkirmish();
        if (skirmish != null && game.getGameState().isFierceSkirmishes()) {
            return (skirmish.getFellowshipCharacter() == physicalCard)
                    || skirmish.getShadowCharacters().contains(physicalCard);
        }
        return false;
    };

    public static final Filter inPlay = (game, physicalCard) -> physicalCard.getZone().isInPlay();

    public static final Filter active = (game, physicalCard) -> game.getGameState().isCardInPlayActive(physicalCard);

    public static Filter canTakeWounds(final LotroPhysicalCard woundSource, final int count) {
        return (game, physicalCard) -> game.getModifiersQuerying().canTakeWounds(game, (woundSource != null)?Collections.singleton(woundSource):Collections.emptySet(), physicalCard, count) && game.getModifiersQuerying().getVitality(game, physicalCard) >= count;
    }

    public static Filter canTakeWounds(final Collection<LotroPhysicalCard> woundSources, final int count) {
        return (game, physicalCard) -> game.getModifiersQuerying().canTakeWounds(game, woundSources, physicalCard, count) && game.getModifiersQuerying().getVitality(game, physicalCard) >= count;
    }

    public static Filter canBeDiscarded(final LotroPhysicalCard source) {
        return (game, physicalCard) -> game.getModifiersQuerying().canBeDiscardedFromPlay(game, source.getOwner(), physicalCard, source);
    }

    public static Filter canBeDiscarded(final String performingPlayer, final LotroPhysicalCard source) {
        return (game, physicalCard) -> game.getModifiersQuerying().canBeDiscardedFromPlay(game, performingPlayer, physicalCard, source);
    }

    public static final Filter exhausted = (game, physicalCard) -> game.getModifiersQuerying().getVitality(game, physicalCard) == 1;

    public static Filter inSkirmishAgainst(final Filterable... againstFilter) {
        return (game, physicalCard) -> {
            Skirmish skirmish = game.getGameState().getSkirmish();
            if (skirmish != null && skirmish.getFellowshipCharacter() != null) {
                return (skirmish.getFellowshipCharacter() == physicalCard && Filters.filter(skirmish.getShadowCharacters(), game, againstFilter).size() > 0)
                        || (skirmish.getShadowCharacters().contains(physicalCard) && Filters.and(againstFilter).accepts(game, skirmish.getFellowshipCharacter()));
            }
            return false;
        };
    }

    public static Filter canExert(final LotroPhysicalCard source) {
        return canExert(source, 1);
    }

    public static Filter canExert(final LotroPhysicalCard source, final int count) {
        return (game, physicalCard) -> game.getModifiersQuerying().getVitality(game, physicalCard) > count
                && game.getModifiersQuerying().canBeExerted(game, source, physicalCard);
    }

    public static Filter playable(final DefaultGame game) {
        return playable(game, 0);
    }

    public static Filter playable(final DefaultGame game, final int twilightModifier) {
        return playable(game, twilightModifier, false);
    }

    public static Filter playable(final DefaultGame game, final int twilightModifier, final boolean ignoreRoamingPenalty) {
        return playable(game, twilightModifier, ignoreRoamingPenalty, false);
    }

    public static Filter playable(final DefaultGame game, final int twilightModifier, final boolean ignoreRoamingPenalty, final boolean ignoreCheckingDeadPile) {
        return playable(game, 0, twilightModifier, ignoreRoamingPenalty, ignoreCheckingDeadPile, false);
    }

    public static Filter playable(final DefaultGame game, final int twilightModifier, final boolean ignoreRoamingPenalty, final boolean ignoreCheckingDeadPile, final boolean ignoreResponseEvents) {
        return playable(game, 0, twilightModifier, ignoreRoamingPenalty, ignoreCheckingDeadPile, ignoreResponseEvents);
    }

    public static Filter playable(final DefaultGame game, final int withTwilightRemoved, final int twilightModifier, final boolean ignoreRoamingPenalty, final boolean ignoreCheckingDeadPile, final boolean ignoreResponseEvents) {
        return (game1, physicalCard) -> {
            Side expectedSide = (physicalCard.getOwner().equals(game1.getGameState().getCurrentPlayerId()) ? Side.FREE_PEOPLE : Side.SHADOW);
            final LotroCardBlueprint blueprint = physicalCard.getBlueprint();
            if (blueprint.getSide() != expectedSide)
                return false;

            return LotroPlayUtils.checkPlayRequirements(game1, physicalCard, Filters.any, withTwilightRemoved, twilightModifier, ignoreRoamingPenalty, ignoreCheckingDeadPile, ignoreResponseEvents);
        };
    }

    public static final Filter any = (game, physicalCard) -> true;

    public static final Filter none = (game, physicalCard) -> false;

    public static final Filter unique = (game, physicalCard) -> physicalCard.getBlueprint().isUnique();

    private static Filter signet(final Signet signet) {
        return (game, physicalCard) -> game.getModifiersQuerying().hasSignet(game, physicalCard, signet);
    }

    private static Filter race(final Race race) {
        return Filters.and(
                Filters.or(CardType.COMPANION, CardType.ALLY, CardType.MINION, CardType.FOLLOWER),
                (Filter) (game, physicalCard) -> {
                    LotroCardBlueprint blueprint = physicalCard.getBlueprint();
                    return blueprint.getRace() == race;
                });
    }


    private static Filter side(final Side side) {
        return (game, physicalCard) -> physicalCard.getBlueprint().getSide() == side;
    }

    public static Filter owner(final String playerId) {
        return (game, physicalCard) -> physicalCard.getOwner() != null && physicalCard.getOwner().equals(playerId);
    }

    public static Filter isAllyHome(final int siteNumber, final SitesBlock siteBlock) {
        return Filters.and(
                CardType.ALLY,
                (Filter) (game, physicalCard) -> RuleUtils.isAllyAtHome(physicalCard, siteNumber, siteBlock));
    }

    public static final Filter allyAtHome = Filters.and(
            CardType.ALLY,
            (Filter) (game, physicalCard) -> RuleUtils.isAllyAtHome(physicalCard, game.getGameState().getCurrentSiteNumber(), game.getGameState().getCurrentSiteBlock()));

    public static Filter allyWithSameHome(final LotroPhysicalCard card) {
        return Filters.and(
                CardType.ALLY,
                (Filter) (game, physicalCard) -> {
                    LotroCardBlueprint blueprint = card.getBlueprint();
                    if (blueprint.getCardType() == CardType.ALLY) {
                        SitesBlock homeBlock = blueprint.getAllyHomeSiteBlock();
                        int[] homeSites = blueprint.getAllyHomeSiteNumbers();
                        for (int homeSite : homeSites) {
                            if (RuleUtils.isAllyAtHome(physicalCard, homeSite, homeBlock)) {
                                return true;
                            }
                        }
                    }
                    return false;
                });
    }

    public static final Filter currentSite = (game, physicalCard) -> game.getGameState().getCurrentSite() == physicalCard;

    public static final Filter currentRegion = (game, physicalCard) -> LotroGameUtils.getRegion(game) == LotroGameUtils.getRegion(physicalCard.getSiteNumber());

    public static Filter siteNumber(final int siteNumber) {
        return siteNumberBetweenInclusive(siteNumber, siteNumber);
    }
    public static final Filter siteHasSiteNumber = Filters.and(CardType.SITE,
            (Filter) (game, physicalCard) -> {
                int bpNumber = physicalCard.getBlueprint().getSiteNumber();
                Integer siteNumber = physicalCard.getSiteNumber();
                return Objects.requireNonNullElse(siteNumber, bpNumber) != 0;
            });

    public static Filter siteNumberBetweenInclusive(final int minSiteNumber, final int maxSiteNumber) {
        return (game, physicalCard) -> {
            if(physicalCard.getBlueprint().getCardType() == CardType.MINION)
            {
                int sitenum = game.getModifiersQuerying().getMinionSiteNumber(game, physicalCard);
                return sitenum >= minSiteNumber && sitenum <= maxSiteNumber;
            }

            return (physicalCard.getSiteNumber()!=null)
                    && (physicalCard.getSiteNumber()>=minSiteNumber) && (physicalCard.getSiteNumber()<=maxSiteNumber);
        };
    }

    public static final Filter siteInCurrentRegion = Filters.and(CardType.SITE,
            (Filter) (game, physicalCard) -> {
                int siteNumber = physicalCard.getSiteNumber();
                return LotroGameUtils.getRegion(game) == LotroGameUtils.getRegion(siteNumber);
            });

    public static Filter region(final int region) { return regionNumberBetweenInclusive(region, region); }

    public static Filter regionNumberBetweenInclusive(final int minRegionNumber, final int maxRegionNumber) {
        return (game, physicalCard) -> {

            if(physicalCard.getSiteNumber() == null)
                return false;

            int regionNumber = LotroGameUtils.getRegion(physicalCard.getSiteNumber());

            return regionNumber >= minRegionNumber && regionNumber <= maxRegionNumber;
        };
    }

    public static Filter hasAttached(final Filterable... filters) {
        return hasAttached(1, filters);
    }

    public static Filter hasAttached(int count, final Filterable... filters) {
        return (game, physicalCard) -> {
            List<LotroPhysicalCard> physicalCardList = game.getGameState().getAttachedCards(physicalCard);
            return (Filters.filter(physicalCardList, game, filters).size() >= count);
        };
    }

    public static Filter hasStacked(final Filterable... filter) {
        return hasStacked(1, filter);
    }

    public static Filter hasStacked(final int count, final Filterable... filter) {
        return (game, physicalCard) -> {
            List<LotroPhysicalCard> physicalCardList = game.getGameState().getStackedCards(physicalCard);
            if (filter.length == 1 && filter[0] == Filters.any)
                return physicalCardList.size() >= count;
            return (Filters.filter(physicalCardList, game, Filters.and(filter, activeSide)).size() >= count);
        };
    }

    public static Filter not(final Filterable... filters) {
        return (game, physicalCard) -> !Filters.and(filters).accepts(game, physicalCard);
    }

    public static Filter sameCard(final LotroPhysicalCard card) {
        final int cardId = card.getCardId();
        return (game, physicalCard) -> (physicalCard.getCardId() == cardId);
    }

    public static Filter in(final Collection<? extends LotroPhysicalCard> cards) {
        final Set<Integer> cardIds = new HashSet<>();
        for (LotroPhysicalCard card : cards)
            cardIds.add(card.getCardId());
        return (game, physicalCard) -> cardIds.contains(physicalCard.getCardId());
    }

    public static Filter zone(final Zone zone) {
        return (game, physicalCard) -> physicalCard.getZone() == zone;
    }

    public static Filter hasWounds(final int wounds) {
        return (game, physicalCard) -> game.getGameState().getWounds(physicalCard) >= wounds;
    }

    public static final Filter unwounded = (game, physicalCard) -> game.getGameState().getWounds(physicalCard) == 0;

    public static final Filter wounded = Filters.hasWounds(1);

    public static Filter name(final String name) {
        return (game, physicalCard) -> name != null && physicalCard.getBlueprint().getTitle() != null && physicalCard.getBlueprint().getTitle().equals(name);
    }

    private static Filter type(final CardType cardType) {
        return (game, physicalCard) -> (physicalCard.getBlueprint().getCardType() == cardType)
                || game.getModifiersQuerying().isAdditionalCardType(game, physicalCard, cardType);
    }

    public static Filter attachedTo(final Filterable... filters) {
        return (game, physicalCard) -> physicalCard.getAttachedTo() != null && Filters.and(filters).accepts(game, physicalCard.getAttachedTo());
    }

    public static Filter stackedOn(final Filterable... filters) {
        return (game, physicalCard) -> physicalCard.getStackedOn() != null && Filters.and(filters).accepts(game, physicalCard.getStackedOn());
    }

    public static Filter siteControlledByShadowPlayer(final String fellowshipPlayer) {
        return (game, physicalCard) -> physicalCard.getBlueprint().getCardType() == CardType.SITE && physicalCard.getCardController() != null && !physicalCard.getCardController().equals(fellowshipPlayer);
    }

    public static Filter siteControlledByAnyPlayer = (game, physicalCard) -> physicalCard.getBlueprint().getCardType() == CardType.SITE && physicalCard.getCardController() != null;

    public static Filter siteControlled(final String playerId) {
        return (game, physicalCard) -> physicalCard.getBlueprint().getCardType() == CardType.SITE && playerId.equals(physicalCard.getCardController());
    }

    public static Filter uncontrolledSite = (game, physicalCard) -> physicalCard.getBlueprint().getCardType() == CardType.SITE && physicalCard.getCardController() == null;


    private static Filter culture(final Culture culture) {
        return (game, physicalCard) -> (physicalCard.getBlueprint().getCulture() == culture);
    }

    private static Filter keyword(final Keyword keyword) {
        return (game, physicalCard) -> game.getModifiersQuerying().hasKeyword(game, physicalCard, keyword);
    }

    public static Filter and(final Filterable... filters) {
        Filter[] filtersInt = convertToFilters(filters);
        if (filtersInt.length == 1)
            return filtersInt[0];
        return andInternal(filtersInt);
    }

    public static Filter or(final Filterable... filters) {
        Filter[] filtersInt = convertToFilters(filters);
        if (filtersInt.length == 1)
            return filtersInt[0];
        return orInternal(filtersInt);
    }

    private static Filter[] convertToFilters(Filterable... filters) {
        Filter[] filtersInt = new Filter[filters.length];
        for (int i = 0; i < filtersInt.length; i++)
            filtersInt[i] = changeToFilter(filters[i]);
        return filtersInt;
    }

    private static Filter changeToFilter(Filterable filter) {
        if (filter instanceof Filter)
            return (Filter) filter;
        else if (filter instanceof LotroPhysicalCard)
            return Filters.sameCard((LotroPhysicalCard) filter);
        else if (filter instanceof CardType)
            return _typeFilterMap.get((CardType) filter);
        else if (filter instanceof Culture)
            return _cultureFilterMap.get((Culture) filter);
        else if (filter instanceof Keyword)
            return _keywordFilterMap.get((Keyword) filter);
        else if (filter instanceof PossessionClass)
            return _possessionClassFilterMap.get((PossessionClass) filter);
        else if (filter instanceof Race)
            return _raceFilterMap.get((Race) filter);
        else if (filter instanceof Side)
            return _sideFilterMap.get((Side) filter);
        else if (filter instanceof Signet)
            return _signetFilterMap.get((Signet) filter);
        else if (filter instanceof Zone)
            return _zoneFilterMap.get((Zone) filter);
        else
            throw new IllegalArgumentException("Unknown type of filterable: " + filter);
    }

    public static final Filter activeSide = (game, physicalCard) -> {
        boolean shadow = physicalCard.getBlueprint().getSide() == Side.SHADOW;
        if (shadow)
            return !physicalCard.getOwner().equals(game.getGameState().getCurrentPlayerId());
        else
            return physicalCard.getOwner().equals(game.getGameState().getCurrentPlayerId());
    };

    private static Filter andInternal(final Filter... filters) {
        return (game, physicalCard) -> {
            for (Filter filter : filters) {
                if (!filter.accepts(game, physicalCard))
                    return false;
            }
            return true;
        };
    }

    public static Filter and(final Filterable[] filters1, final Filterable... filters2) {
        final Filter[] newFilters1 = convertToFilters(filters1);
        final Filter[] newFilters2 = convertToFilters(filters2);
        if (newFilters1.length == 1 && newFilters2.length == 0)
            return newFilters1[0];
        if (newFilters1.length == 0 && newFilters2.length == 1)
            return newFilters2[0];
        return (game, physicalCard) -> {
            for (Filter filter : newFilters1) {
                if (!filter.accepts(game, physicalCard))
                    return false;
            }
            for (Filter filter : newFilters2) {
                if (!filter.accepts(game, physicalCard))
                    return false;
            }
            return true;
        };
    }

    private static Filter orInternal(final Filter... filters) {
        return (game, physicalCard) -> {
            for (Filter filter : filters) {
                if (filter.accepts(game, physicalCard))
                    return true;
            }
            return false;
        };
    }

    public static final Filter ringBoundCompanion = Filters.and(CardType.COMPANION, Keyword.RING_BOUND);
    public static final Filter unboundCompanion = Filters.and(CardType.COMPANION, Filters.not(Keyword.RING_BOUND));
    public static final Filter roamingMinion = Filters.and(CardType.MINION, Keyword.ROAMING);
    public static final Filter mounted = Filters.or(Filters.hasAttached(PossessionClass.MOUNT), Keyword.MOUNTED);

    public static final Filter spottable = (game, physicalCard) -> true;

    private static class FindFirstActiveCardInPlayVisitor implements PhysicalCardVisitor {
        private final DefaultGame game;
        private final Filter _filter;
        private LotroPhysicalCard _card;

        private FindFirstActiveCardInPlayVisitor(DefaultGame game, Filter filter) {
            this.game = game;
            _filter = filter;
        }

        @Override
        public boolean visitPhysicalCard(LotroPhysicalCard physicalCard) {
            if (_filter.accepts(game, physicalCard)) {
                _card = physicalCard;
                return true;
            }
            return false;
        }

        public LotroPhysicalCard getCard() {
            return _card;
        }
    }

    private static class GetCardsMatchingFilterVisitor extends CompletePhysicalCardVisitor {
        private final DefaultGame game;
        private final Filter _filter;

        private final Set<LotroPhysicalCard> _physicalCards = new HashSet<>();

        private GetCardsMatchingFilterVisitor(DefaultGame game, Filter filter) {
            this.game = game;
            _filter = filter;
        }

        @Override
        protected void doVisitPhysicalCard(LotroPhysicalCard physicalCard) {
            if (_filter.accepts(game, physicalCard))
                _physicalCards.add(physicalCard);
        }

        public int getCounter() {
            return _physicalCards.size();
        }

        public Set<LotroPhysicalCard> getPhysicalCards() {
            return _physicalCards;
        }
    }
}
