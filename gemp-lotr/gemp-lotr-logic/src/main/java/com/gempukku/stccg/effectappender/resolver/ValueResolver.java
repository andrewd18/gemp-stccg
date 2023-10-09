package com.gempukku.stccg.effectappender.resolver;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Keyword;
import com.gempukku.stccg.common.filterable.Race;
import com.gempukku.stccg.evaluator.*;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.RequirementUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Collection;

public class ValueResolver {
    public static <AbstractGame extends DefaultGame> ValueSource<AbstractGame> resolveEvaluator(Object value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveEvaluator(value, null, environment);
    }

    public static <AbstractGame extends DefaultGame> ValueSource<AbstractGame> resolveEvaluator(Object value, Integer defaultValue, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        if (value == null && defaultValue == null)
            throw new InvalidCardDefinitionException("Value not defined");
        if (value == null)
            return new ConstantEvaluator<>(defaultValue);
        if (value instanceof Number)
            return new ConstantEvaluator<>(((Number) value).intValue());
        if (value instanceof String stringValue) {
            if (stringValue.contains("-")) {
                final String[] split = stringValue.split("-", 2);
                final int min = Integer.parseInt(split[0]);
                final int max = Integer.parseInt(split[1]);
                if (min > max || min < 0 || max < 1)
                    throw new InvalidCardDefinitionException("Unable to resolve count: " + value);
                return new ValueSource<>() {
                    @Override
                    public Evaluator<AbstractGame> getEvaluator(DefaultActionContext<AbstractGame> actionContext) {
                        throw new RuntimeException("Evaluator has resolved to range");
                    }

                    @Override
                    public int getMinimum(DefaultActionContext<AbstractGame> actionContext) {
                        return min;
                    }

                    @Override
                    public int getMaximum(DefaultActionContext<AbstractGame> actionContext) {
                        return max;
                    }
                };
            } else {
                int v = Integer.parseInt(stringValue);
                return new ConstantEvaluator<>(v);
            }
        }
        if (value instanceof JSONObject object) {
            final String type = FieldUtils.getString(object.get("type"), "type");
            if (type.equalsIgnoreCase("range")) {
                FieldUtils.validateAllowedFields(object, "from", "to");
                ValueSource fromValue = resolveEvaluator(object.get("from"), environment);
                ValueSource toValue = resolveEvaluator(object.get("to"), environment);
                return new ValueSource<>() {
                    @Override
                    public Evaluator getEvaluator(DefaultActionContext<AbstractGame> actionContext) {
                        throw new RuntimeException("Evaluator has resolved to range");
                    }

                    @Override
                    public int getMinimum(DefaultActionContext<AbstractGame> actionContext) {
                        return fromValue.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                    }

                    @Override
                    public int getMaximum(DefaultActionContext<AbstractGame> actionContext) {
                        return toValue.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                    }
                };
            } else if (type.equalsIgnoreCase("requires")) {
                FieldUtils.validateAllowedFields(object, "requires", "true", "false");
                final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
                final Requirement[] conditions = environment.getRequirementFactory().getRequirements(conditionArray, environment);
                ValueSource trueValue = resolveEvaluator(object.get("true"), environment);
                ValueSource falseValue = resolveEvaluator(object.get("false"), environment);
                return actionContext -> (Evaluator) (game, cardAffected) -> {
                    if (RequirementUtils.acceptsAllRequirements(conditions, actionContext)) {
                        return trueValue.getEvaluator(actionContext).evaluateExpression(game, cardAffected);
                    } else {
                        return falseValue.getEvaluator(actionContext).evaluateExpression(game, cardAffected);
                    }
                };

            } else if (type.equalsIgnoreCase("siteNumberInMemory")) {
                FieldUtils.validateAllowedFields(object, "memory");
                final String memory = FieldUtils.getString(object.get("memory"), "memory");
                return actionContext -> (game, cardAffected) -> actionContext.getCardFromMemory(memory).getSiteNumber();
            } else if (type.equalsIgnoreCase("forEachInMemory")) {
                FieldUtils.validateAllowedFields(object, "memory", "limit");
                final String memory = FieldUtils.getString(object.get("memory"), "memory");
                final int limit = FieldUtils.getInteger(object.get("limit"), "limit", Integer.MAX_VALUE);
                return (actionContext) -> {
                    final int count = actionContext.getCardsFromMemory(memory).size();
                    return new ConstantEvaluator(Math.min(limit, count));
                };
            } else if (type.equalsIgnoreCase("forEachMatchingInMemory")) {
                FieldUtils.validateAllowedFields(object, "memory", "filter", "limit");
                final String memory = FieldUtils.getString(object.get("memory"), "memory");
                final String filter = FieldUtils.getString(object.get("filter"), "filter");
                final int limit = FieldUtils.getInteger(object.get("limit"), "limit", Integer.MAX_VALUE);
                final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
                return (actionContext) -> {
                    final int count = Filters.filter(actionContext.getCardsFromMemory(memory), actionContext.getGame(),
                            filterableSource.getFilterable(actionContext)).size();
                    return new ConstantEvaluator(Math.min(limit, count));
                };
            } else if (type.equalsIgnoreCase("forEachKeyword")) {
                FieldUtils.validateAllowedFields(object, "filter", "keyword");
                final String filter = FieldUtils.getString(object.get("filter"), "filter");
                final Keyword keyword = FieldUtils.getEnum(Keyword.class, object.get("keyword"), "keyword");
                final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
                return (actionContext) -> (game, cardAffected) -> {
                    int count = 0;
                    for (PhysicalCard physicalCard : Filters.filterActive(game, filterableSource.getFilterable(actionContext))) {
                        count += game.getModifiersQuerying().getKeywordCount(game, physicalCard, keyword);
                    }
                    return count;
                };

            } else if (type.equalsIgnoreCase("forEachKeywordOnCardInMemory")) {
                FieldUtils.validateAllowedFields(object, "memory", "keyword");
                final String memory = FieldUtils.getString(object.get("memory"), "memory");
                final Keyword keyword = FieldUtils.getEnum(Keyword.class, object.get("keyword"), "keyword");
                if (keyword == null)
                    throw new InvalidCardDefinitionException("Keyword cannot be null");
                return (actionContext) -> {
                    final DefaultGame game = actionContext.getGame();
                    int count = 0;
                    final Collection<? extends PhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(memory);
                    for (PhysicalCard cardFromMemory : cardsFromMemory) {
                        count += game.getModifiersQuerying().getKeywordCount(game, cardFromMemory, keyword);
                    }
                    return new ConstantEvaluator(count);
                };
            } else if (type.equalsIgnoreCase("limit")) {
                FieldUtils.validateAllowedFields(object, "limit", "value");
                ValueSource limitSource = resolveEvaluator(object.get("limit"), 1, environment);
                ValueSource valueSource = resolveEvaluator(object.get("value"), 0, environment);
                return (actionContext) -> new LimitEvaluator(valueSource.getEvaluator(actionContext), limitSource.getEvaluator(actionContext));
            } else if (type.equalsIgnoreCase("cardphaselimit")) {
                FieldUtils.validateAllowedFields(object, "limit", "amount");
                ValueSource limitSource = resolveEvaluator(object.get("limit"), 0, environment);
                ValueSource valueSource = resolveEvaluator(object.get("amount"), 0, environment);
                return (actionContext) -> new CardPhaseLimitEvaluator(actionContext.getSource(),
                        actionContext.getGame().getGameState().getCurrentPhase(), limitSource.getEvaluator(actionContext),
                        valueSource.getEvaluator(actionContext));
            } else if (type.equalsIgnoreCase("countStacked")) {
                FieldUtils.validateAllowedFields(object, "on", "filter");
                final String on = FieldUtils.getString(object.get("on"), "on");
                final String filter = FieldUtils.getString(object.get("filter"), "filter", "any");

                final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
                final FilterableSource onFilter = environment.getFilterFactory().generateFilter(on, environment);

                return (actionContext) -> {
                    final Filterable on1 = onFilter.getFilterable(actionContext);
                    return new CountStackedEvaluator(on1, filterableSource.getFilterable(actionContext));
                };
            } else if (type.equalsIgnoreCase("forEachInDiscard")) {
                FieldUtils.validateAllowedFields(object, "filter", "multiplier", "limit", "player");
                final String filter = FieldUtils.getString(object.get("filter"), "filter", "any");
                final int multiplier = FieldUtils.getInteger(object.get("multiplier"), "multiplier", 1);
                final int limit = FieldUtils.getInteger(object.get("limit"), "limit", Integer.MAX_VALUE);
                final String playerInput = FieldUtils.getString(object.get("player"), "player", "you");
                final PlayerSource playerSrc = PlayerResolver.resolvePlayer(playerInput);

                final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
                return actionContext -> new MultiplyEvaluator(multiplier, new Evaluator() {
                    final String player = playerSrc.getPlayer(actionContext);
                    @Override
                    public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                        final Filterable filterable = filterableSource.getFilterable(actionContext);
                                // Lines below commented out since this code originally counted ALL discard piles
//                        int count = 0;
//                        for (String player : game.getGameState().getPlayerOrder().getAllPlayers())
                        int count = Filters.filter(game.getGameState().getDiscard(player), game, filterable).size();
                        return Math.min(limit, count);
                    }
                });
            } else if (type.equalsIgnoreCase("forEachInHand")) {
                FieldUtils.validateAllowedFields(object, "filter", "hand");
                final String filter = FieldUtils.getString(object.get("filter"), "filter", "any");
                final String hand = FieldUtils.getString(object.get("hand"), "hand", "you");
                final PlayerSource player = PlayerResolver.resolvePlayer(hand);
                final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
                return actionContext ->
                        (Evaluator) (game, cardAffected) -> Filters.filter(game.getGameState().getHand(player.getPlayer(actionContext)),
                                game, filterableSource.getFilterable(actionContext)).size();
            } else if (type.equalsIgnoreCase("forEachInPlayPile")) {
                FieldUtils.validateAllowedFields(object, "filter", "owner");
                final String filter = FieldUtils.getString(object.get("filter"), "filter", "any");
                final String owner = FieldUtils.getString(object.get("owner"), "owner", "you");
                final PlayerSource player = PlayerResolver.resolvePlayer(owner);
                final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
                return actionContext -> (Evaluator<TribblesGame>) (game, cardAffected) ->
                        Filters.filter(game.getGameState().getPlayPile(player.getPlayer(actionContext)),
                                game, filterableSource.getFilterable(actionContext)).size();
            } else if (type.equalsIgnoreCase("countCardsInPlayPile")) {
                FieldUtils.validateAllowedFields(object, "owner");
                final String owner = FieldUtils.getString(object.get("owner"), "owner", "you");
                final PlayerSource player = PlayerResolver.resolvePlayer(owner);
                return actionContext -> (Evaluator<TribblesGame>) (game, cardAffected)
                        -> game.getGameState().getPlayPile(player.getPlayer(actionContext)).size();
            } else if (type.equalsIgnoreCase("forEachInDeadPile")) {
                FieldUtils.validateAllowedFields(object, "filter", "multiplier");
                final String filter = FieldUtils.getString(object.get("filter"), "filter", "any");
                final int multiplier = FieldUtils.getInteger(object.get("multiplier"), "multiplier", 1);
                final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
                return actionContext ->
                        (Evaluator) (game, cardAffected) -> multiplier * Filters.filter(game.getGameState().getDeadPile(game.getGameState().getCurrentPlayerId()),
                                game, filterableSource.getFilterable(actionContext)).size();
            } else if (type.equalsIgnoreCase("fromMemory")) {
                FieldUtils.validateAllowedFields(object, "memory", "multiplier", "limit");
                String memory = FieldUtils.getString(object.get("memory"), "memory");
                final int multiplier = FieldUtils.getInteger(object.get("multiplier"), "multiplier", 1);
                final int limit = FieldUtils.getInteger(object.get("limit"), "limit", Integer.MAX_VALUE);
                return (actionContext) -> {
                    int value1 = Integer.parseInt(actionContext.getValueFromMemory(memory));
                    return new ConstantEvaluator(Math.min(limit, multiplier * value1));
                };
            } else if (type.equalsIgnoreCase("multiply")) {
                FieldUtils.validateAllowedFields(object, "multiplier", "source");
                final ValueSource multiplier = ValueResolver.resolveEvaluator(object.get("multiplier"), environment);
                final ValueSource valueSource = ValueResolver.resolveEvaluator(object.get("source"), 0, environment);
                return (actionContext) -> new MultiplyEvaluator(multiplier.getEvaluator(actionContext), valueSource.getEvaluator(actionContext));
            } else if (type.equalsIgnoreCase("cardAffectedLimitPerPhase")) {
                FieldUtils.validateAllowedFields(object, "limit", "source", "prefix");
                final int limit = FieldUtils.getInteger(object.get("limit"), "limit");
                final String prefix = FieldUtils.getString(object.get("prefix"), "prefix", "");
                final ValueSource valueSource = ValueResolver.resolveEvaluator(object.get("source"), 0, environment);
                return (actionContext -> new CardAffectedPhaseLimitEvaluator(
                        actionContext.getSource(),
                        actionContext.getGame().getGameState().getCurrentPhase(),
                        limit, prefix, valueSource.getEvaluator(actionContext)));
            } else if (type.equalsIgnoreCase("forEachStrength")) {
                FieldUtils.validateAllowedFields(object, "multiplier", "over", "filter");
                final int multiplier = FieldUtils.getInteger(object.get("multiplier"), "multiplier", 1);
                final int over = FieldUtils.getInteger(object.get("over"), "over", 0);
                final String filter = FieldUtils.getString(object.get("filter"), "filter", "any");

                final FilterableSource vitalitySource = environment.getFilterFactory().generateFilter(filter, environment);

                return (actionContext) -> {
                    if (filter.equals("any")) {
                        return new MultiplyEvaluator(multiplier,
                                (game, cardAffected) -> Math.max(0, game.getModifiersQuerying().getStrength(game, cardAffected) - over));
                    } else {
                        return new MultiplyEvaluator(multiplier,
                                (game, cardAffected) -> {
                                    final Filterable filterable = vitalitySource.getFilterable(actionContext);
                                    int strength = 0;
                                    for (PhysicalCard physicalCard : Filters.filterActive(game, filterable)) {
                                        strength += game.getModifiersQuerying().getStrength(game, physicalCard);
                                    }

                                    return Math.max(0, strength - over);
                                });
                    }
                };
            } else if (type.equalsIgnoreCase("printedStrengthFromMemory")) {
                FieldUtils.validateAllowedFields(object, "memory");
                final String memory = FieldUtils.getString(object.get("memory"), "memory");

                return actionContext -> (Evaluator<DefaultGame>) (game, cardAffected) -> {
                    int result = 0;
                    for (PhysicalCard physicalCard : actionContext.getCardsFromMemory(memory)) {
                        result += physicalCard.getBlueprint().getStrength();
                    }
                    return result;
                };
            } else if (type.equalsIgnoreCase("strengthFromMemory")) {
                FieldUtils.validateAllowedFields(object, "memory");
                final String memory = FieldUtils.getString(object.get("memory"), "memory");

                return actionContext -> (Evaluator<DefaultGame>) (game, cardAffected) -> {
                    int result = 0;
                    for (PhysicalCard physicalCard : actionContext.getCardsFromMemory(memory)) {
                        result += game.getModifiersQuerying().getStrength(game, physicalCard);
                    }
                    return result;
                };
            } else if (type.equalsIgnoreCase("tribbleValueFromMemory")) {
                FieldUtils.validateAllowedFields(object, "memory");
                final String memory = FieldUtils.getString(object.get("memory"), "memory");

                return actionContext -> (Evaluator<DefaultGame>) (game, cardAffected) -> {
                    int result = 0;
                    for (PhysicalCard physicalCard : actionContext.getCardsFromMemory(memory)) {
                        result += physicalCard.getBlueprint().getTribbleValue();
                    }
                    return result;
                };
            }
            else if (type.equalsIgnoreCase("subtract")) {
                FieldUtils.validateAllowedFields(object, "firstNumber", "secondNumber");
                final ValueSource firstNumber = ValueResolver.resolveEvaluator(object.get("firstNumber"), 0, environment);
                final ValueSource secondNumber = ValueResolver.resolveEvaluator(object.get("secondNumber"), 0, environment);
                return actionContext -> (Evaluator) (game, cardAffected) -> {
                    final int first = firstNumber.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                    final int second = secondNumber.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                    return first - second;
                };
            } else if (type.equalsIgnoreCase("sum")) {
                FieldUtils.validateAllowedFields(object, "source");
                final JSONArray sourceArray = FieldUtils.getArray(object.get("source"), "source");
                ValueSource[] sources = new ValueSource[sourceArray.size()];
                for (int i = 0; i < sources.length; i++)
                    sources[i] = ValueResolver.resolveEvaluator(sourceArray.get(i), 0, environment);

                return actionContext -> {
                    Evaluator[] evaluators = new Evaluator[sources.length];
                    for (int i = 0; i < sources.length; i++)
                        evaluators[i] = sources[i].getEvaluator(actionContext);

                    return (game, cardAffected) -> {
                        int sum = 0;
                        for (Evaluator evaluator : evaluators)
                            sum += evaluator.evaluateExpression(game, cardAffected);

                        return sum;
                    };
                };
            } else if (type.equalsIgnoreCase("twilightCostInMemory")) {
                FieldUtils.validateAllowedFields(object, "multiplier", "memory");
                final int multiplier = FieldUtils.getInteger(object.get("multiplier"), "multiplier", 1);
                final String memory = FieldUtils.getString(object.get("memory"), "memory");
                return actionContext -> (Evaluator<DefaultGame>) (game, cardAffected) -> {
                    int total = 0;
                    for (PhysicalCard physicalCard : actionContext.getCardsFromMemory(memory)) {
                        total += physicalCard.getBlueprint().getTwilightCost();
                    }
                    return multiplier * total;
                };
            } else if (type.equalsIgnoreCase("maxOfRaces")) {
                FieldUtils.validateAllowedFields(object, "filter");
                final String filter = FieldUtils.getString(object.get("filter"), "filter");

                final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

                return actionContext -> (game, cardAffected) -> {
                    int result = 0;
                    final Filterable filterable = filterableSource.getFilterable(actionContext);
                    for (Race race : Race.values())
                        result = Math.max(result, Filters.countSpottable(game, race, filterable));

                    return result;
                };
            } else if (type.equalsIgnoreCase("max")) {
                FieldUtils.validateAllowedFields(object, "first", "second");
                ValueSource first = resolveEvaluator(object.get("first"), environment);
                ValueSource second = resolveEvaluator(object.get("second"), environment);

                return actionContext -> (game, cardAffected) ->
                        Math.max(
                                first.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null),
                                second.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null)
                        );
            } else if (type.equalsIgnoreCase("min")) {
                FieldUtils.validateAllowedFields(object, "first", "second");
                ValueSource first = resolveEvaluator(object.get("first"), environment);
                ValueSource second = resolveEvaluator(object.get("second"), environment);

                return actionContext -> (game, cardAffected) ->
                        Math.min(
                                first.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null),
                                second.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null)
                        );
            }
            throw new InvalidCardDefinitionException("Unrecognized type of an evaluator " + type);
        }
        throw new InvalidCardDefinitionException("Unable to resolve an evaluator");
    }
}
