package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.common.filterable.lotr.Culture;
import com.gempukku.stccg.common.filterable.lotr.Keyword;
import com.gempukku.stccg.common.filterable.lotr.Race;
import com.gempukku.stccg.common.filterable.lotr.Side;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.evaluator.SingleMemoryEvaluator;
import com.gempukku.stccg.game.DefaultGame;

import java.util.*;

public class FilterFactory {
    private final Map<String, FilterableSource> simpleFilters = new HashMap<>();
    private final Map<String, FilterableSourceProducer> parameterFilters = new HashMap<>();
    private final CardBlueprintFactory _environment;

    public FilterFactory(CardBlueprintFactory blueprintFactory) {
        _environment = blueprintFactory;
        for (CardIcon value : CardIcon.values())
            appendFilter(value);
        for (CardType value : CardType.values())
            appendFilter(value);
        for (Keyword value : Keyword.values())
            appendFilter(value);
/*        for (Species value : Species.values())
            appendFilter(value); */
        for (Affiliation value : Affiliation.values())
            appendFilter(value);
        for (Uniqueness value : Uniqueness.values())
            appendFilter(value);
        for (FacilityType value : FacilityType.values())
            appendFilter(value);
        for (Race value : Race.values())
            appendFilter(value);
        for (PropertyLogo value : PropertyLogo.values())
            appendFilter(value);

        simpleFilters.put("another", (actionContext) -> Filters.not(actionContext.getSource()));
        simpleFilters.put("any", (actionContext) -> Filters.any);
        simpleFilters.put("character", (actionContext) -> Filters.character);
        simpleFilters.put("idinstored",
                (actionContext -> (Filter) (game, physicalCard) -> {
                    final String whileInZoneData = (String) actionContext.getSource().getWhileInZoneData();
                    if (whileInZoneData == null)
                        return false;
                    for (String cardId : whileInZoneData.split(",")) {
                        if (cardId.equals(String.valueOf(physicalCard.getCardId())))
                            return true;
                    }

                    return false;
                }));
        simpleFilters.put("inplay", (actionContext) -> Filters.inPlay);
        simpleFilters.put("item", (actionContext) -> Filters.item);
        simpleFilters.put("nor", (actionContext) -> Filters.Nor);
        simpleFilters.put("self", ActionContext::getSource);
        simpleFilters.put("unbound",
                (actionContext) -> Filters.unboundCompanion);
        simpleFilters.put("unique", (actionContext) -> Filters.unique);
        simpleFilters.put("weapon", (actionContext) -> Filters.weapon);
            // TODO - "your" isn't quite right
        simpleFilters.put("your", (actionContext) -> Filters.your(actionContext.getPerformingPlayerId()));
        simpleFilters.put("yours", (actionContext) -> Filters.your(actionContext.getPerformingPlayerId()));
        simpleFilters.put("yoursevenifnotinplay", (actionContext) -> Filters.yoursEvenIfNotInPlay(actionContext.getPerformingPlayerId()));

        parameterFilters.put("and",
                (parameter, environment) -> {
                    final String[] filters = splitIntoFilters(parameter);
                    FilterableSource[] filterables = new FilterableSource[filters.length];
                    for (int i = 0; i < filters.length; i++)
                        filterables[i] = environment.getFilterFactory().generateFilter(filters[i]);
                    return (actionContext) -> {
                        Filterable[] filters1 = new Filterable[filterables.length];
                        for (int i = 0; i < filterables.length; i++)
                            filters1[i] = filterables[i].getFilterable(actionContext);

                        return Filters.and(filters1);
                    };
                });
        parameterFilters.put("attachedto",
                (parameter, environment) -> {
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameter);
                    return (actionContext) -> Filters.attachedTo(filterableSource.getFilterable(actionContext));
                });
        parameterFilters.put("affiliation", (parameter, environment) -> {
            final Affiliation affiliation = Affiliation.findAffiliation(parameter);
            if (affiliation == null)
                throw new InvalidCardDefinitionException("Unable to find culture for: " + parameter);
            return (actionContext) -> affiliation;
        });
        parameterFilters.put("culturefrommemory", ((parameter, environment) -> actionContext -> {
            Set<Culture> cultures = new HashSet<>();
            for (PhysicalCard physicalCard : actionContext.getCardsFromMemory(parameter)) {
                cultures.add(physicalCard.getBlueprint().getCulture());
            }
            return Filters.or(cultures.toArray(new Culture[0]));
        }));
        parameterFilters.put("hasattached",
                (parameter, environment) -> {
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameter);
                    return (actionContext) -> Filters.hasAttached(filterableSource.getFilterable(actionContext));
                });
        parameterFilters.put("hasattachedcount",
                (parameter, environment) -> {
                    String[] parameterSplit = parameter.split(",", 2);
                    int count = Integer.parseInt(parameterSplit[0]);
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameterSplit[1]);
                    return (actionContext) -> Filters.hasAttached(count, filterableSource.getFilterable(actionContext));
                });
        parameterFilters.put("hasstacked",
                (parameter, environment) -> {
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameter);
                    return (actionContext) -> Filters.hasStacked(filterableSource.getFilterable(actionContext));
                });
        parameterFilters.put("hasstackedcount",
                (parameter, environment) -> {
                    String[] parameterSplit = parameter.split(",", 2);
                    int count = Integer.parseInt(parameterSplit[0]);
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameterSplit[1]);
                    return (actionContext) -> Filters.hasStacked(count, filterableSource.getFilterable(actionContext));
                });
        parameterFilters.put("loweststrength",
                (parameter, environment) -> {
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameter);
                    return actionContext -> {
                        final Filterable sourceFilterable = filterableSource.getFilterable(actionContext);
                        return Filters.and(
                                sourceFilterable, Filters.strengthEqual(
                                        new SingleMemoryEvaluator(actionContext,
                                                new Evaluator(actionContext) {
                                                    @Override
                                                    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                                                        int minStrength = Integer.MAX_VALUE;
                                                        for (PhysicalCard card : Filters.filterActive(cardAffected.getGame(), sourceFilterable))
                                                            minStrength = Math.min(minStrength, cardAffected.getGame().getModifiersQuerying().getStrength(card));
                                                        return minStrength;
                                                    }
                                                }
                                        )
                                )
                        );
                    };
                });
        parameterFilters.put("maxtwilight",
                (parameter, environment) -> {
                    if (parameter.startsWith("memory(") && parameter.endsWith(")")) {
                        String memory = parameter.substring(parameter.indexOf("(") + 1, parameter.lastIndexOf(")"));
                        return actionContext -> {
                            try{
                                final int value = Integer.parseInt(actionContext.getValueFromMemory(memory));
                                return Filters.maxPrintedTwilightCost(value);
                            }
                            catch(IllegalArgumentException ex) {
                                return Filters.maxPrintedTwilightCost(100);
                            }
                        };
                    } else {
                        final ValueSource valueSource = ValueResolver.resolveEvaluator(parameter, environment);
                        return actionContext -> {
                            final int value = valueSource.evaluateExpression(actionContext, null);
                            return Filters.maxPrintedTwilightCost(value);
                        };
                    }
                });
        parameterFilters.put("memory",
                (parameter, environment) -> (actionContext) -> Filters.in(actionContext.getCardsFromMemory(parameter)));
        parameterFilters.put("mintwilight",
                (parameter, environment) -> {
                    if (parameter.startsWith("memory(") && parameter.endsWith(")")) {
                        String memory = parameter.substring(parameter.indexOf("(") + 1, parameter.lastIndexOf(")"));
                        return actionContext -> {
                            try {
                                final int value = Integer.parseInt(actionContext.getValueFromMemory(memory));
                                return Filters.minPrintedTwilightCost(value);
                            }
                            catch(IllegalArgumentException ex) {
                                return Filters.minPrintedTwilightCost(0);
                            }
                        };
                    } else {
                        final ValueSource valueSource = ValueResolver.resolveEvaluator(parameter, environment);
                        return actionContext -> {
                            final int value = valueSource.evaluateExpression(actionContext, null);
                            return Filters.minPrintedTwilightCost(value);
                        };
                    }
                });

        parameterFilters.put("name",
                (parameter, environment) -> {
                    String name = Sanitize(parameter);
                    return (actionContext) -> (Filter)
                            (game, physicalCard) -> physicalCard.getBlueprint().getTitle() != null && name.equals(Sanitize(physicalCard.getBlueprint().getTitle()));
                });
        parameterFilters.put("namefrommemory",
                (parameter, environment) -> actionContext -> {
                    Set<String> titles = new HashSet<>();
                    for (PhysicalCard physicalCard : actionContext.getCardsFromMemory(parameter))
                        titles.add(physicalCard.getBlueprint().getTitle());
                    return (Filter) (game, physicalCard) -> titles.contains(physicalCard.getBlueprint().getTitle());
                });
        parameterFilters.put("nameinstackedon",
                (parameter, environment) -> {
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameter);
                    return actionContext -> {
                        final Filterable sourceFilterable = filterableSource.getFilterable(actionContext);
                        return (Filter) (game, physicalCard) -> {
                            for (PhysicalCard cardWithStack : Filters.filterActive(game, sourceFilterable)) {
                                for (PhysicalCard stackedCard : cardWithStack.getStackedCards()) {
                                    if (stackedCard.getBlueprint().getTitle().equals(physicalCard.getBlueprint().getTitle()))
                                        return true;
                                }
                            }
                            return false;
                        };
                    };
                });
        parameterFilters.put("not",
                (parameter, environment) -> {
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameter);
                    return (actionContext) -> Filters.not(filterableSource.getFilterable(actionContext));
                });
        parameterFilters.put("or",
                (parameter, environment) -> {
                    final String[] filters = splitIntoFilters(parameter);
                    FilterableSource[] filterables = new FilterableSource[filters.length];
                    for (int i = 0; i < filters.length; i++)
                        filterables[i] = environment.getFilterFactory().generateFilter(filters[i]);
                    return (actionContext) -> {
                        Filterable[] filters1 = new Filterable[filterables.length];
                        for (int i = 0; i < filterables.length; i++)
                            filters1[i] = filterables[i].getFilterable(actionContext);

                        return Filters.or(filters1);
                    };
                });
        parameterFilters.put("race",
                (parameter, environment) -> {
                    if (parameter.equals("stored")) {
                        return actionContext -> (Filter) (game, physicalCard) -> {
                            final String value = (String) actionContext.getSource().getWhileInZoneData();
                            if (value == null)
                                return false;
                            else
                                return Race.valueOf(value) == physicalCard.getBlueprint().getRace();
                        };
                    } else if (parameter.equals("cannotspot")) {
                        return actionContext -> (Filter) (game, physicalCard) -> {
                            final Race race = physicalCard.getBlueprint().getRace();
                            if (race != null)
                                return !Filters.canSpot(game, race);
                            return false;
                        };
                    }
                    throw new InvalidCardDefinitionException("Unknown race definition in filter: " + parameter
                            + ".  Do not use race() for races; instead just list the race by itself.");
                });
        parameterFilters.put("side", (parameter, environment) -> {
            final Side side = Side.Parse(parameter);
            if (side == null)
                throw new InvalidCardDefinitionException("Unable to find side for: " + parameter);

            return (actionContext) -> side;
        });
        parameterFilters.put("strengthlessthan",
                (parameter, environment) -> {
                    final ValueSource valueSource = ValueResolver.resolveEvaluator(parameter, environment);

                    return (actionContext) -> {
                        int amount = valueSource.evaluateExpression(actionContext, null);
                        return Filters.lessStrengthThan(amount);
                    };
                });
        parameterFilters.put("strengthmorethan",
                (parameter, environment) -> {
                    final ValueSource valueSource = ValueResolver.resolveEvaluator(parameter, environment);

                    return (actionContext) -> {
                        int amount = valueSource.evaluateExpression(actionContext, null);
                        return Filters.moreStrengthThan(amount);
                    };
                });
        parameterFilters.put("title",parameterFilters.get("name"));
        parameterFilters.put("zone",
                (parameter, environment) -> {
                    final Zone zone = environment.getEnum(Zone.class, parameter, "parameter");
                    return actionContext -> zone;
                });
    }

    private void appendFilter(Filterable value) {
        final String filterName = Sanitize(value.toString());
        final String optionalFilterName = value.toString().toLowerCase().replace("_", "-");
        if (simpleFilters.containsKey(filterName))
            throw new RuntimeException("Duplicate filter name: " + filterName);
        simpleFilters.put(filterName, (actionContext) -> value);
        if (!optionalFilterName.equals(filterName))
            simpleFilters.put(optionalFilterName, (actionContext -> value));
    }

    public FilterableSource generateFilter(String value) throws
            InvalidCardDefinitionException {
        if (value == null)
            throw new InvalidCardDefinitionException("Filter not specified");
        String[] filterStrings = splitIntoFilters(value);
        if (filterStrings.length == 0)
            return (actionContext) -> Filters.any;
        if (filterStrings.length == 1)
            return createFilter(filterStrings[0]);

        FilterableSource[] filters = new FilterableSource[filterStrings.length];
        for (int i = 0; i < filters.length; i++)
            filters[i] = createFilter(filterStrings[i]);
        return (actionContext) -> {
            Filterable[] filter = new Filterable[filters.length];
            for (int i = 0; i < filter.length; i++) {
                filter[i] = filters[i].getFilterable(actionContext);
            }

            return Filters.and(filter);
        };
    }

    public FilterableSource parseSTCCGFilter(String value) throws InvalidCardDefinitionException {
        String orNoParens = "\\s+OR\\s+(?![^\\(]*\\))";
        String andNoParens = "\\s+\\+\\s+(?![^\\(]*\\))";
        if (value == null)
            return null;
        if (value.split(orNoParens).length > 1) {
            String[] stringSplit = value.split(orNoParens);
            List<FilterableSource> filterableSources = new LinkedList<>();
            for (String string : stringSplit)
                filterableSources.add(parseSTCCGFilter(string));
            return (actionContext) -> {
                List<Filterable> filterables = new LinkedList<>();
                for (FilterableSource filterableSource : filterableSources)
                    filterables.add(filterableSource.getFilterable(actionContext));
                return Filters.or(filterables.toArray(new Filterable[0]));
            };
        }
        if (value.split(andNoParens).length > 1) {
            String[] stringSplit = value.split(andNoParens);
            List<FilterableSource> filterableSources = new LinkedList<>();
            for (String string : stringSplit)
                filterableSources.add(parseSTCCGFilter(string));
            return (actionContext) -> {
                List<Filterable> filterables = new LinkedList<>();
                for (FilterableSource filterableSource : filterableSources)
                    filterables.add(filterableSource.getFilterable(actionContext));
                return Filters.and(filterables.toArray(new Filterable[0]));
            };
        }
        if (value.startsWith("(") && value.endsWith(")")) {
            return parseSTCCGFilter(value.substring(1, value.length() - 1));
        }
        if (value.startsWith("not(") && value.endsWith(")")) {
            FilterableSource filterableSource = parseSTCCGFilter(value.substring(4, value.length() - 1));
            return (actionContext) -> Filters.not(filterableSource.getFilterable(actionContext));
        }
        if (value.startsWith("name(") && value.endsWith(")")) {
            return (actionContext) -> Filters.name(value.substring(5, value.length() - 1));
        }
        if (value.startsWith("skill-dots<=")) {
            String[] stringSplit = value.split("<=");
            return (actionContext) -> Filters.skillDotsLessThanOrEqualTo(Integer.parseInt(stringSplit[1]));
        }
        if (value.startsWith("sd-icons=")) {
            String[] stringSplit = value.split("=");
            return (actionContext) -> Filters.specialDownloadIconCount(Integer.parseInt(stringSplit[1]));
        }
        if (value.equals("you have no copies in play")) {
            return (actionContext) -> Filters.youHaveNoCopiesInPlay(actionContext.getPerformingPlayer());
        }
        FilterableSource result = simpleFilters.get(Sanitize(value));
        if (result == null)
            throw new InvalidCardDefinitionException("Unknown filter: " + value);
        else return result;
    }

/*
        First attempt at parseFilter. Will likely not continue.
    public Filter parseFilter(String value, CardGenerationEnvironment environment)
            throws InvalidCardDefinitionException {
        return parseFilter(value, environment, false);
        //		    filter: Your personnel and ships that have a Star Trek: The Next Generation or Star Trek: Generations property logo (even if not in play)
    }

    public Filter parseFilter(String value, CardGenerationEnvironment environment, boolean andMeansOr) throws InvalidCardDefinitionException {
            // TODO - Does not deal with comma-separated and/or lists (e.g., "ships, personnel, and equipment"
        if (value.startsWith("Your")) {
            if (value.endsWith("(even if not in play)")) {
                String replaced = value.replaceAll("\\s*\\(even if not in play\\)$", "").replaceAll("Your[^$]","");
                return Filters.yourCardsIncludingOutOfPlay(parseFilter(replaced, environment));
            }
            else {
                return Filters.yourCardsInPlay(parseFilter(value.replaceAll("Your[^$]", ""), environment));
            }
        }
        if (value.split("\\s*that have\\s*").length == 2) {
            String[] stringSplit = value.split("\\s*that have\\s*");
            return Filters.and(parseFilter(stringSplit[0], environment, true), parseFilter(stringSplit[1], environment, false));
        }
        if (value.split("\\s*and\\s*").length == 2) {
            String[] stringSplit = value.split("\\s*and\\s*");
            if (andMeansOr)
                return Filters.or(parseFilter(stringSplit[0], environment), parseFilter(stringSplit[1], environment));
            else return Filters.and(parseFilter(stringSplit[0], environment), parseFilter(stringSplit[1], environment));
        }
        if (value.split("\\s*or\\s*").length == 2) {
            String[] stringSplit = value.split("\\s*or\\s*");
            return Filters.or(parseFilter(stringSplit[0], environment), parseFilter(stringSplit[1], environment));
        }
        return parseFilter(value, environment);
    } */

    private String[] splitIntoFilters(String value) throws InvalidCardDefinitionException {
        List<String> parts = new LinkedList<>();
        final char[] chars = value.toCharArray();

        int depth = 0;
        StringBuilder sb = new StringBuilder();
        for (char ch : chars) {
            if (depth > 0) {
                if (ch == ')')
                    depth--;
                if (ch == '(')
                    depth++;
                sb.append(ch);
            } else {
                if (ch == ',') {
                    parts.add(sb.toString());
                    sb = new StringBuilder();
                } else {
                    if (ch == ')')
                        throw new InvalidCardDefinitionException("Invalid filter definition: " + value);
                    if (ch == '(')
                        depth++;
                    sb.append(ch);
                }
            }
        }

        if (depth != 0)
            throw new InvalidCardDefinitionException("Not matching number of opening and closing brackets: " + value);

        parts.add(sb.toString());

        return parts.toArray(new String[0]);
    }

    private FilterableSource createFilter(String filterString) throws InvalidCardDefinitionException {
        if (filterString.contains("(") && filterString.endsWith(")")) {
            String filterName = filterString.substring(0, filterString.indexOf("("));
            String filterParameter =
                    filterString.substring(filterString.indexOf("(") + 1, filterString.lastIndexOf(")"));
            return lookupFilter(Sanitize(filterName), Sanitize(filterParameter));
        }
        return lookupFilter(Sanitize(filterString), null);
    }



    private FilterableSource lookupFilter(String name, String parameter) throws InvalidCardDefinitionException {
        if (parameter == null) {
            FilterableSource result = simpleFilters.get(Sanitize(name));
            if (result != null)
                return result;
        }

        final FilterableSourceProducer filterableSourceProducer = parameterFilters.get(Sanitize(name));
        if (filterableSourceProducer == null)
            throw new InvalidCardDefinitionException("Unable to find filter: " + name);

        return filterableSourceProducer.createFilterableSource(parameter, _environment);
    }

    public static String Sanitize(String input)
    {
        return input
                .toLowerCase()
                .replace(" ", "")
                .replace("_", "");
    }
}
