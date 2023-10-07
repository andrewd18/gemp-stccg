package com.gempukku.lotro.game;

import com.gempukku.lotro.cards.CardBlueprintLibrary;
import com.gempukku.lotro.cards.CardNotFoundException;
import com.gempukku.lotro.cards.LotroCardBlueprint;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.formats.FormatLibrary;
import com.gempukku.lotro.cards.SetDefinition;
import com.gempukku.lotro.rules.GameUtils;
import com.gempukku.util.MultipleComparator;

import java.text.Normalizer;
import java.util.*;

public class SortAndFilterCards {
    public <T extends CardItem> List<T> process(String filter, Iterable<T> items, CardBlueprintLibrary cardLibrary, FormatLibrary formatLibrary) {
        if (filter == null)
            filter = "";
        String[] filterParams = filter.split("\\|");

//        Side side = getSideFilter(filterParams);
//        String type = getTypeFilter(filterParams);
//        String[] rarity = getRarityFilter(filterParams);
        String[] sets = getSetFilter(filterParams);
        List<String> words = getWords(filterParams);
        Set<CardType> cardTypes = getEnumFilter(CardType.values(), CardType.class, "cardType", null, filterParams);
        Set<TribblePower> tribblePowers = getEnumFilter(TribblePower.values(), TribblePower.class, "tribblePower", null, filterParams);
//        Set<Culture> cultures = getEnumFilter(Culture.values(), Culture.class, "culture", null, filterParams);
//        Set<Keyword> keywords = getEnumFilter(Keyword.values(), Keyword.class, "keyword", Collections.emptySet(), filterParams);
//        Integer siteNumber = getSiteNumber(filterParams);
//        Set<Race> races = getEnumFilter(Race.values(), Race.class, "race", null, filterParams);
//        Set<PossessionClass> itemClasses = getEnumFilter(PossessionClass.values(), PossessionClass.class, "itemClass", Collections.emptySet(), filterParams);
        Set<Keyword> phases = getEnumFilter(Keyword.values(),Keyword.class, "phase", Collections.emptySet(), filterParams);

        List<T> result = new ArrayList<>();
        Map<String, LotroCardBlueprint> cardBlueprintMap = new HashMap<>();

        for (T item : items) {
            String blueprintId = item.getBlueprintId();
            if (isPack(blueprintId)) {
                if (acceptsFilters(cardLibrary, cardBlueprintMap, formatLibrary, blueprintId, sets, cardTypes,
                        tribblePowers, words, phases))
                    result.add(item);
            } else {
                try {
                    cardBlueprintMap.put(blueprintId, cardLibrary.getLotroCardBlueprint(blueprintId));
                    if (acceptsFilters(cardLibrary, cardBlueprintMap, formatLibrary, blueprintId, sets, cardTypes,
                            tribblePowers, words, phases))
                        result.add(item);
                } catch (CardNotFoundException e) {
                    // Ignore the card
                }
            }
        }

        String sort = getSort(filterParams);
        if (sort == null || sort.equals(""))
            sort = "name";

        final String[] sortSplit = sort.split(",");

        MultipleComparator<CardItem> comparators = new MultipleComparator<>();
        for (String oneSort : sortSplit) {
            switch (oneSort) {
                case "twilight" ->
                        comparators.addComparator(new PacksFirstComparator(new TwilightComparator(cardBlueprintMap)));
                case "siteNumber" ->
                        comparators.addComparator(new PacksFirstComparator(new SiteNumberComparator(cardBlueprintMap)));
                case "strength" ->
                        comparators.addComparator(new PacksFirstComparator(new StrengthComparator(cardBlueprintMap)));
                case "vitality" ->
                        comparators.addComparator(new PacksFirstComparator(new VitalityComparator(cardBlueprintMap)));
                case "cardType" ->
                        comparators.addComparator(new PacksFirstComparator(new CardTypeComparator(cardBlueprintMap)));
                case "culture" ->
                        comparators.addComparator(new PacksFirstComparator(new CultureComparator(cardBlueprintMap)));
                case "name" ->
                        comparators.addComparator(new PacksFirstComparator(new NameComparator(cardBlueprintMap)));
                case "tribbleValue" ->
                        comparators.addComparator(new PacksFirstComparator(new TribblesValueComparator(cardBlueprintMap)));
            }
        }

        result.sort(comparators);

        return result;
    }

    private boolean acceptsFilters(CardBlueprintLibrary library, Map<String, LotroCardBlueprint> cardBlueprint,
                                   FormatLibrary formatLibrary, String blueprintId, String[] sets, Set<CardType> cardTypes,
                                   Set<TribblePower> tribblePowers, List<String> words, Set<Keyword> phases) {
/*        if (isPack(blueprintId)) {
            return type == null || type.equals("pack");
        } else {
            if (type == null
                    || type.equals("card")
                    || (type.equals("foil") && blueprintId.endsWith("*"))
                    || (type.equals("nonFoil") && !blueprintId.endsWith("*"))
                    || (type.equals("tengwar") && (blueprintId.endsWith("T*") || blueprintId.endsWith("T")))) {*/
                final LotroCardBlueprint blueprint = cardBlueprint.get(blueprintId);
//                if (side == null || blueprint.getSide() == side)
//                    if (rarity == null || isRarity(blueprintId, rarity, library, library.getSetDefinitions()))
                        if (sets == null || isInSets(blueprintId, sets, library, formatLibrary, cardBlueprint))
                            if (cardTypes == null || cardTypes.contains(blueprint.getCardType()))
                                if (tribblePowers == null || tribblePowers.contains(blueprint.getTribblePower()))
//                                if (cultures == null || cultures.contains(blueprint.getCulture()))
//                                    if (containsAllKeywords(blueprint, keywords))
                                        if (containsAllWords(blueprint, words))
//                                            if (siteNumber == null || blueprint.getSiteNumber() == siteNumber)
//                                                if (races == null || races.contains(blueprint.getRace()))
//                                                    if (containsAllClasses(blueprint, itemClasses))
                                                        return containsAllKeywords(blueprint, phases);
//            }
//        }
        return false;
    }

    private Side getSideFilter(String[] filterParams) {
        for (String filterParam : filterParams) {
            if (filterParam.startsWith("side:"))
                return Side.valueOf(filterParam.substring("side:".length()));
        }
        return null;
    }

    private String getTypeFilter(String[] filterParams) {
        for (String filterParam : filterParams) {
            if (filterParam.startsWith("type:"))
                return filterParam.substring("type:".length());
        }
        return null;
    }

    private String[] getRarityFilter(String[] filterParams) {
        for (String filterParam : filterParams) {
            if (filterParam.startsWith("rarity:"))
                return filterParam.substring("rarity:".length()).split(",");
        }
        return null;
    }

    private String[] getSetFilter(String[] filterParams) {
        String setStr = getSetNumber(filterParams);
        String[] sets = null;
        if (setStr != null)
            sets = setStr.split(",");
        return sets;
    }

    private boolean isRarity(String blueprintId, String[] rarity, CardBlueprintLibrary library, Map<String, SetDefinition> rarities) {
        if (blueprintId.contains("_")) {
            SetDefinition setRarity = rarities.get(blueprintId.substring(0, blueprintId.indexOf("_")));
            if (setRarity != null) {
                String cardRarity = setRarity.getCardRarity(library.stripBlueprintModifiers(blueprintId));
                if(cardRarity == null) {
                    //TODO: log that the rarity was not set
                    //real TODO: put the rarity in the friggin json
                    return false;
                }
                for (String r : rarity) {
                    if (cardRarity.equals(r))
                        return true;
                }
            }
            return false;
        }
        return true;
    }

    private boolean isInSets(String blueprintId, String[] sets, CardBlueprintLibrary library, FormatLibrary formatLibrary, Map<String, LotroCardBlueprint> cardBlueprint) {
        for (String set : sets) {
            GameFormat format = formatLibrary.getFormat(set);

            if (format != null) {
                String valid = format.validateCard(blueprintId);
                if(valid != null && !valid.isEmpty())
                    return false;

                final LotroCardBlueprint blueprint = cardBlueprint.get(blueprintId);
                return true;
            } else {
                if (set.contains("-")) {
                    final String[] split = set.split("-", 2);
                    int min = Integer.parseInt(split[0]);
                    int max = Integer.parseInt(split[1]);
                    for (int setNo = min; setNo <= max; setNo++) {
                        if (blueprintId.startsWith(setNo + "_") || library.hasAlternateInSet(blueprintId, setNo))
                            return true;
                    }
                } else {
                    if (blueprintId.startsWith(set + "_") || library.hasAlternateInSet(blueprintId, Integer.parseInt(set)))
                        return true;
                }
            }
        }

        return false;
    }

    private String getSetNumber(String[] filterParams) {
        for (String filterParam : filterParams) {
            if (filterParam.startsWith("set:"))
                return filterParam.substring("set:".length());
        }
        return null;
    }

    private List<String> getWords(String[] filterParams) {
        List<String> result = new LinkedList<>();
        for (String filterParam : filterParams) {
            if (filterParam.startsWith("name:"))
                result.add(replaceSpecialCharacters(filterParam.substring("name:".length()).toLowerCase()));
        }
        return result;
    }

    private Integer getSiteNumber(String[] filterParams) {
        for (String filterParam : filterParams) {
            if (filterParam.startsWith("siteNumber:"))
                return Integer.parseInt(filterParam.substring("siteNumber:".length()));
        }
        return null;
    }

    private String getSort(String[] filterParams) {
        for (String filterParam : filterParams) {
            if (filterParam.startsWith("sort:"))
                return filterParam.substring("sort:".length());
        }
        return null;
    }

    private boolean containsAllKeywords(LotroCardBlueprint blueprint, Set<Keyword> keywords) {
        for (Keyword keyword : keywords) {
            if (blueprint == null || !blueprint.hasKeyword(keyword))
                return false;
        }
        return true;
    }

    private boolean containsAllClasses(LotroCardBlueprint blueprint, Set<PossessionClass> possessionClasses) {
        for (PossessionClass filterPossessionClass : possessionClasses) {
            if (!(blueprint == null)) {
                if (blueprint.getPossessionClasses() == null) {
                    return filterPossessionClass == PossessionClass.CLASSLESS;
                }            
                for (PossessionClass blueprintPossessionClass : blueprint.getPossessionClasses()) {
                    if (filterPossessionClass == blueprintPossessionClass)
                        return true;
                }
            }
            return false;
        }
        return true;
    }

    private boolean containsAllWords(LotroCardBlueprint blueprint, List<String> words) {
        for (String word : words) {
            if (blueprint == null || !replaceSpecialCharacters(GameUtils.getFullName(blueprint).toLowerCase()).contains(word))
                return false;
        }
        return true;
    }

    public static String replaceSpecialCharacters(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("’", "'")
                .replaceAll("‘", "'")
                .replaceAll("”", "\"")
                .replaceAll("“", "\"")
                .replaceAll("\\p{M}", "");
    }

    private <T extends Enum> Set<T> getEnumFilter(T[] enumValues, Class<T> enumType, String prefix, Set<T> defaultResult, String[] filterParams) {
        for (String filterParam : filterParams) {
            if (filterParam.startsWith(prefix + ":")) {
                String values = filterParam.substring((prefix + ":").length());
                if (values.startsWith("-")) {
                    values = values.substring(1);
                    Set<T> cardTypes = new HashSet<>(Arrays.asList(enumValues));
                    for (String v : values.split(",")) {
                        T t = (T) Enum.valueOf(enumType, v);
                        if (t != null)
                            cardTypes.remove(t);
                    }
                    return cardTypes;
                } else {
                    Set<T> cardTypes = new HashSet<>();
                    for (String v : values.split(","))
                        cardTypes.add((T) Enum.valueOf(enumType, v));
                    return cardTypes;
                }
            }
        }
        return defaultResult;
    }

    private static boolean isPack(String blueprintId) {
        return !blueprintId.contains("_");
    }

    private static class PacksFirstComparator implements Comparator<CardItem> {
        private final Comparator<CardItem> _cardComparator;

        private PacksFirstComparator(Comparator<CardItem> cardComparator) {
            _cardComparator = cardComparator;
        }

        @Override
        public int compare(CardItem o1, CardItem o2) {
            final boolean pack1 = isPack(o1.getBlueprintId());
            final boolean pack2 = isPack(o2.getBlueprintId());
            if (pack1 && pack2)
                return o1.getBlueprintId().compareTo(o2.getBlueprintId());
            else if (pack1)
                return -1;
            else if (pack2)
                return 1;
            else
                return _cardComparator.compare(o1, o2);
        }
    }

    private static class SiteNumberComparator implements Comparator<CardItem> {
        private final Map<String, LotroCardBlueprint> _cardBlueprintMap;

        private SiteNumberComparator(Map<String, LotroCardBlueprint> cardBlueprintMap) {
            _cardBlueprintMap = cardBlueprintMap;
        }

        @Override
        public int compare(CardItem o1, CardItem o2) {
            int siteNumberOne;
            try {
                siteNumberOne = _cardBlueprintMap.get(o1.getBlueprintId()).getSiteNumber();
            } catch (UnsupportedOperationException exp) {
                siteNumberOne = 10;
            }
            int siteNumberTwo;
            try {
                siteNumberTwo = _cardBlueprintMap.get(o2.getBlueprintId()).getSiteNumber();
            } catch (UnsupportedOperationException exp) {
                siteNumberTwo = 10;
            }

            return siteNumberOne - siteNumberTwo;
        }
    }

    private static class NameComparator implements Comparator<CardItem> {
        private final Map<String, LotroCardBlueprint> _cardBlueprintMap;

        private NameComparator(Map<String, LotroCardBlueprint> cardBlueprintMap) {
            _cardBlueprintMap = cardBlueprintMap;
        }

        @Override
        public int compare(CardItem o1, CardItem o2) {
            return GameUtils.getFullName(_cardBlueprintMap.get(o1.getBlueprintId())).compareTo(GameUtils.getFullName(_cardBlueprintMap.get(o2.getBlueprintId())));
        }
    }

    private static class TribblesValueComparator implements Comparator<CardItem> {
        private final Map<String, LotroCardBlueprint> _cardBlueprintMap;

        private TribblesValueComparator(Map<String, LotroCardBlueprint> cardBlueprintMap) {
            _cardBlueprintMap = cardBlueprintMap;
        }

        @Override
        public int compare(CardItem o1, CardItem o2) {
            return _cardBlueprintMap.get(o1.getBlueprintId()).getTribbleValue() - _cardBlueprintMap.get(o2.getBlueprintId()).getTribbleValue();
        }
    }

    private static class TwilightComparator implements Comparator<CardItem> {
        private final Map<String, LotroCardBlueprint> _cardBlueprintMap;

        private TwilightComparator(Map<String, LotroCardBlueprint> cardBlueprintMap) {
            _cardBlueprintMap = cardBlueprintMap;
        }

        @Override
        public int compare(CardItem o1, CardItem o2) {
            return _cardBlueprintMap.get(o1.getBlueprintId()).getTwilightCost() - _cardBlueprintMap.get(o2.getBlueprintId()).getTwilightCost();
        }
    }

    private static class StrengthComparator implements Comparator<CardItem> {
        private final Map<String, LotroCardBlueprint> _cardBlueprintMap;

        private StrengthComparator(Map<String, LotroCardBlueprint> cardBlueprintMap) {
            _cardBlueprintMap = cardBlueprintMap;
        }

        @Override
        public int compare(CardItem o1, CardItem o2) {
            return getStrengthSafely(_cardBlueprintMap.get(o1.getBlueprintId())) - getStrengthSafely(_cardBlueprintMap.get(o2.getBlueprintId()));
        }

        private int getStrengthSafely(LotroCardBlueprint blueprint) {
            try {
                return blueprint.getStrength();
            } catch (UnsupportedOperationException exp) {
                return Integer.MAX_VALUE;
            }
        }
    }

    private static class VitalityComparator implements Comparator<CardItem> {
        private final Map<String, LotroCardBlueprint> _cardBlueprintMap;

        private VitalityComparator(Map<String, LotroCardBlueprint> cardBlueprintMap) {
            _cardBlueprintMap = cardBlueprintMap;
        }

        @Override
        public int compare(CardItem o1, CardItem o2) {
            return getVitalitySafely(_cardBlueprintMap.get(o1.getBlueprintId())) - getVitalitySafely(_cardBlueprintMap.get(o2.getBlueprintId()));
        }

        private int getVitalitySafely(LotroCardBlueprint blueprint) {
            try {
                return blueprint.getVitality();
            } catch (UnsupportedOperationException exp) {
                return Integer.MAX_VALUE;
            }
        }
    }

    private static class CardTypeComparator implements Comparator<CardItem> {
        private final Map<String, LotroCardBlueprint> _cardBlueprintMap;

        private CardTypeComparator(Map<String, LotroCardBlueprint> cardBlueprintMap) {
            _cardBlueprintMap = cardBlueprintMap;
        }

        @Override
        public int compare(CardItem o1, CardItem o2) {
            CardType cardType1 = _cardBlueprintMap.get(o1.getBlueprintId()).getCardType();
            CardType cardType2 = _cardBlueprintMap.get(o2.getBlueprintId()).getCardType();
            return cardType1.ordinal() - cardType2.ordinal();
        }
    }

    private static class CultureComparator implements Comparator<CardItem> {
        private final Map<String, LotroCardBlueprint> _cardBlueprintMap;

        private CultureComparator(Map<String, LotroCardBlueprint> cardBlueprintMap) {
            _cardBlueprintMap = cardBlueprintMap;
        }

        @Override
        public int compare(CardItem o1, CardItem o2) {
            Culture culture1 = _cardBlueprintMap.get(o1.getBlueprintId()).getCulture();
            Culture culture2 = _cardBlueprintMap.get(o2.getBlueprintId()).getCulture();

            return getOrdinal(culture1) - getOrdinal(culture2);
        }

        private int getOrdinal(Culture culture) {
            if (culture == null)
                return -1;
            return culture.ordinal();
        }
    }
}
