package com.gempukku.stccg.cards;

import com.gempukku.stccg.cards.fieldprocessor.*;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Quadrant;
import com.gempukku.stccg.common.filterable.Uniqueness;
import com.gempukku.stccg.effectappender.EffectAppender;
import com.gempukku.stccg.effectappender.EffectAppenderFactory;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.filters.FilterFactory;
import com.gempukku.stccg.modifiers.ModifierSourceFactory;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.RequirementFactory;
import com.gempukku.stccg.requirement.trigger.TriggerCheckerFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

public class CardBlueprintFactory {
    private final Map<String, FieldProcessor> fieldProcessors = new HashMap<>();

    private final EffectAppenderFactory effectAppenderFactory = new EffectAppenderFactory(this);
    private final FilterFactory filterFactory = new FilterFactory(this);
    private final RequirementFactory requirementFactory = new RequirementFactory(this);
    private final TriggerCheckerFactory triggerCheckerFactory = new TriggerCheckerFactory();
    private final ModifierSourceFactory modifierSourceFactory = new ModifierSourceFactory();

    public CardBlueprintFactory() {
        fieldProcessors.put("title", new TitleFieldProcessor());
        fieldProcessors.put("property-logo", new PropertyLogoFieldProcessor());
        fieldProcessors.put("lore", new LoreFieldProcessor());
        fieldProcessors.put("subtitle", new SubtitleFieldProcessor());
        fieldProcessors.put("image-url", new ImageUrlFieldProcessor());
        fieldProcessors.put("tribble-value", new TribbleValueFieldProcessor());
        fieldProcessors.put("tribble-power", new TribblePowerFieldProcessor());
        fieldProcessors.put("uniqueness", new UniquenessFieldProcessor());
        fieldProcessors.put("side", new SideFieldProcessor());
        fieldProcessors.put("type", new CardTypeFieldProcessor());
        fieldProcessors.put("seed", new SeedFieldProcessor());
        fieldProcessors.put("icons", new IconsFieldProcessor());

        fieldProcessors.put("quadrant", new QuadrantFieldProcessor());
        fieldProcessors.put("region", new RegionFieldProcessor());
        fieldProcessors.put("location", new LocationFieldProcessor());
        fieldProcessors.put("caninsertintospaceline", new CanInsertIntoSpacelineProcessor());
        fieldProcessors.put("affiliation-icons", new AffiliationIconsFieldProcessor());
        fieldProcessors.put("mission-type", new MissionTypeFieldProcessor());
        fieldProcessors.put("mission-requirements", new MissionRequirementsFieldProcessor());
        fieldProcessors.put("point-box", new PointBoxFieldProcessor());
        fieldProcessors.put("span", new SpanFieldProcessor());
        fieldProcessors.put("integrity", new AttributeFieldProcessor(CardAttribute.INTEGRITY));
        fieldProcessors.put("cunning", new AttributeFieldProcessor(CardAttribute.CUNNING));
        fieldProcessors.put("strength", new AttributeFieldProcessor(CardAttribute.STRENGTH));
        fieldProcessors.put("range", new AttributeFieldProcessor(CardAttribute.RANGE));
        fieldProcessors.put("weapons", new AttributeFieldProcessor(CardAttribute.WEAPONS));
        fieldProcessors.put("shields", new AttributeFieldProcessor(CardAttribute.SHIELDS));
        fieldProcessors.put("classification", new ClassificationFieldProcessor());
        fieldProcessors.put("skill-box", new SkillBoxFieldProcessor());
        fieldProcessors.put("image-options", new ImageOptionsFieldProcessor());

        fieldProcessors.put("race", new RaceFieldProcessor());
        fieldProcessors.put("affiliation", new AffiliationFieldProcessor());
        fieldProcessors.put("staffing", new StaffingFieldProcessor());
        fieldProcessors.put("facility-type", new FacilityTypeFieldProcessor());
        fieldProcessors.put("itemclass", new PossessionClassFieldProcessor());
        fieldProcessors.put("keyword", new KeywordFieldProcessor());
        fieldProcessors.put("keywords", new KeywordFieldProcessor());
        fieldProcessors.put("twilight", new CostFieldProcessor());
        fieldProcessors.put("target", new TargetFieldProcessor());
        fieldProcessors.put("requires", new RequirementFieldProcessor());
        fieldProcessors.put("effects", new EffectFieldProcessor());

        // Fields in the JSON, but not yet implemented
        fieldProcessors.put("gametext", new NullProcessor());
        fieldProcessors.put("ship-class", new NullProcessor());
        fieldProcessors.put("restriction-box", new NullProcessor());
        fieldProcessors.put("dilemma-type", new NullProcessor());
        fieldProcessors.put("dilemma-effect", new DilemmaEffectFieldProcessor());
    }

    public CardBlueprint buildFromJson(String blueprintId, JSONObject json) throws InvalidCardDefinitionException {
//        LOGGER.debug("Calling buildFromJson");
        CardBlueprint result = new CardBlueprint(blueprintId);

        Set<Map.Entry<String, Object>> values = json.entrySet();
        for (Map.Entry<String, Object> value : values) {
            final String field = value.getKey().toLowerCase();
            final Object fieldValue = value.getValue();
            final FieldProcessor fieldProcessor = fieldProcessors.get(field);
            if (fieldProcessor == null)
                throw new InvalidCardDefinitionException("Unrecognized field: " + field);

//            LOGGER.debug("Processing field " + field + " with value " + fieldValue);
            fieldProcessor.processField(field, fieldValue, result, this);
        }

        // Apply uniqueness based on ST1E glossary
        List<CardType> implicitlyUniqueTypes = Arrays.asList(CardType.PERSONNEL, CardType.SHIP, CardType.FACILITY,
                CardType.SITE, CardType.MISSION, CardType.TIME_LOCATION);
        if (result.getUniqueness() == null) {
            if (implicitlyUniqueTypes.contains(result.getCardType())) {
                result.setUniqueness(Uniqueness.UNIQUE);
            } else {
                result.setUniqueness(Uniqueness.UNIVERSAL);
            }
        }

        // Set quadrant to alpha if none was specified
        List<CardType> implicitlyAlphaQuadrant = Arrays.asList(CardType.PERSONNEL, CardType.SHIP, CardType.FACILITY,
                CardType.MISSION);
        if (result.getQuadrant() == null && implicitlyAlphaQuadrant.contains(result.getCardType()))
            result.setQuadrant(Quadrant.ALPHA);

        result.validateConsistency();

        return result;
    }

    public EffectAppenderFactory getEffectAppenderFactory() {
        return effectAppenderFactory;
    }

    public FilterFactory getFilterFactory() {
        return filterFactory;
    }

    public TriggerCheckerFactory getTriggerCheckerFactory() {
        return triggerCheckerFactory;
    }

    public ModifierSourceFactory getModifierSourceFactory() {
        return modifierSourceFactory;
    }

    private static class NullProcessor implements FieldProcessor {
        @Override
        public void processField(String key, Object value, CardBlueprint blueprint,
                                 CardBlueprintFactory environment) {
            // Ignore
        }
    }

    public int getInteger(Object value, String key) throws InvalidCardDefinitionException {
        return getInteger(value, key, 0);
    }

    public int getInteger(Object value, String key, int defaultValue) throws InvalidCardDefinitionException {
        if (value == null)
            return defaultValue;
        if (!(value instanceof Number))
            throw new InvalidCardDefinitionException("Unknown type in " + key + " field");
        return ((Number) value).intValue();
    }

    public String getString(Object value, String key) throws InvalidCardDefinitionException {
        return getString(value, key, null);
    }

    public String getString(Object value, String key, String defaultValue) throws InvalidCardDefinitionException {
        if (value == null)
            return defaultValue;
        if (!(value instanceof String))
            throw new InvalidCardDefinitionException("Unknown type in " + key + " field");
        return (String) value;
    }

    public String[] getStringArray(Object value, String key) throws InvalidCardDefinitionException {
        if (value == null)
            return new String[0];
        else if (value instanceof String)
            return new String[]{(String) value};
        else if (value instanceof final JSONArray array) {
            return (String[]) array.toArray(new String[0]);
        }
        throw new InvalidCardDefinitionException("Unknown type in " + key + " field");
    }

    public FilterableSource getFilterable(JSONObject object)
            throws InvalidCardDefinitionException {
        return filterFactory.generateFilter(getString(object.get("filter"), "filter"));
    }

    public FilterableSource getFilterable(JSONObject object, String defaultValue)
            throws InvalidCardDefinitionException {
        return filterFactory.generateFilter(getString(object.get("filter"), "filter", defaultValue));
    }


    public boolean getBoolean(Object value, String key) throws InvalidCardDefinitionException {
        if (value == null)
            throw new InvalidCardDefinitionException("Value of " + key + " is required");
        if (!(value instanceof Boolean))
            throw new InvalidCardDefinitionException("Unknown type in " + key + " field");
        return (Boolean) value;
    }

    public boolean getBoolean(Object value, String key, boolean defaultValue) throws InvalidCardDefinitionException {
        if (value == null)
            return defaultValue;
        if (!(value instanceof Boolean))
            throw new InvalidCardDefinitionException("Unknown type in " + key + " field");
        return (Boolean) value;
    }

    public <T extends Enum<T>> T getEnum(Class<T> enumClass, Object value, String key)
            throws InvalidCardDefinitionException {
        if (value == null)
            return null;
        final String string = getString(value, key);
        return Enum.valueOf(enumClass, string.toUpperCase().replaceAll("[ '\\-.]","_"));
    }

    public <T extends Enum<T>> T getEnum(Class<T> enumClass, String string) {
        return Enum.valueOf(enumClass, string.toUpperCase().replaceAll("[ '\\-.]","_"));
    }

    public PlayerSource getPlayerSource(JSONObject value, String key, String defaultValue)
            throws InvalidCardDefinitionException {
        String playerString = getString(value.get(key), key, defaultValue);
        return PlayerResolver.resolvePlayer(playerString);
    }

    public Requirement getRequirement(JSONObject object) throws InvalidCardDefinitionException {
        return requirementFactory.getRequirement(object);
    }

    public JSONObject[] getObjectArray(Object value, String key) throws InvalidCardDefinitionException {
        if (value == null)
            return new JSONObject[0];
        else if (value instanceof JSONObject)
            return new JSONObject[]{(JSONObject) value};
        else if (value instanceof final JSONArray array)
            return (JSONObject[]) array.toArray(new JSONObject[0]);
        else throw new InvalidCardDefinitionException("Unknown type in " + key + " field");
    }

    public Requirement[] getRequirementsFromJSON(JSONObject object) throws InvalidCardDefinitionException {
        return requirementFactory.getRequirements(getObjectArray(object.get("requires"), "requires"));
    }

    public EffectAppender[] getEffectAppendersFromJSON(JSONObject object, String key)
            throws InvalidCardDefinitionException {
        return effectAppenderFactory.getEffectAppenders(getObjectArray(object.get(key), key));
    }

    public void validateAllowedFields(JSONObject object, String... fields) throws InvalidCardDefinitionException {
        Set<String> keys = object.keySet();
        for (String key : keys) {
            if (!key.equals("type") && !Arrays.asList(fields).contains(key))
                throw new InvalidCardDefinitionException("Unrecognized field: " + key);
        }
    }
}
