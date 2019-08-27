package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RequirementFactory {
    private Map<String, RequirementProducer> requirementProducers = new HashMap<>();

    public RequirementFactory() {
        requirementProducers.put("canspot", new CanSpot());
        requirementProducers.put("canspotburdens", new CanSpotBurdens());
        requirementProducers.put("controlssite", new ControlsSite());
        requirementProducers.put("twilightpoollessthan", new TwilightPoolLessThan());
        requirementProducers.put("location", new Location());
    }

    public Requirement getRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final String type = FieldUtils.getString(object.get("type"), "type");
        final RequirementProducer requirementProducer = requirementProducers.get(type.toLowerCase());
        if (requirementProducer == null)
            throw new InvalidCardDefinitionException("Unable to resolve requirement of type: " + type);
        return requirementProducer.getPlayRequirement(object, environment);
    }

    public Requirement[] getRequirements(JSONObject[] object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        Requirement[] result = new Requirement[object.length];
        for (int i = 0; i < object.length; i++)
            result[i] = getRequirement(object[i], environment);
        return result;
    }
}
