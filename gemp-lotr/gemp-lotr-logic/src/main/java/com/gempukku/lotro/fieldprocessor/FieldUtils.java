package com.gempukku.lotro.fieldprocessor;

import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.common.Side;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Set;

public class FieldUtils {

    final static Logger LOG = Logger.getLogger(FieldUtils.class);

    public static int getInteger(Object value, String key) throws InvalidCardDefinitionException {
        return getInteger(value, key, 0);
    }

    public static int getInteger(Object value, String key, int defaultValue) throws InvalidCardDefinitionException {
        if (value == null)
            return defaultValue;
        if (!(value instanceof Number))
            throw new InvalidCardDefinitionException("Unknown type in " + key + " field");
        return ((Number) value).intValue();
    }

    public static String[] getStringArray(Object value, String key) throws InvalidCardDefinitionException {
        if (value == null)
            return new String[0];
        else if (value instanceof String)
            return new String[]{(String) value};
        else if (value instanceof final JSONArray array) {
            return (String[]) array.toArray(new String[0]);
        }
        throw new InvalidCardDefinitionException("Unknown type in " + key + " field");
    }

    public static String getString(Object value, String key) throws InvalidCardDefinitionException {
        return getString(value, key, null);
    }

    public static String getString(Object value, String key, String defaultValue) throws InvalidCardDefinitionException {
        if (value == null)
            return defaultValue;
        if (!(value instanceof String))
            throw new InvalidCardDefinitionException("Unknown type in " + key + " field");
        return (String) value;
    }

    public static boolean getBoolean(Object value, String key) throws InvalidCardDefinitionException {
        if (value == null)
            throw new InvalidCardDefinitionException("Value of " + key + " is required");
        if (!(value instanceof Boolean))
            throw new InvalidCardDefinitionException("Unknown type in " + key + " field");
        return (Boolean) value;
    }

    public static boolean getBoolean(Object value, String key, boolean defaultValue) throws InvalidCardDefinitionException {
        if (value == null)
            return defaultValue;
        if (!(value instanceof Boolean))
            throw new InvalidCardDefinitionException("Unknown type in " + key + " field");
        return (Boolean) value;
    }

    public static <T extends Enum<T>> T getEnum(Class<T> enumClass, Object value, String key) throws InvalidCardDefinitionException {
        if (value == null)
            return null;
        final String string = getString(value, key);
        return Enum.valueOf(enumClass, string.toUpperCase().replaceAll("[ '\\-]","_"));
    }

    public static Side getSide(Object value, String key) throws InvalidCardDefinitionException {
        final String string = getString(value, key);
        final Side side = Side.Parse(string);
        if (side == null)
            throw new InvalidCardDefinitionException("Unknown side '" + string + "' in " + key + " field");
        return side;
    }

    public static JSONArray getArray(Object value, String key) throws InvalidCardDefinitionException {
        if (value == null)
            return new JSONArray();
        else if (value instanceof JSONObject) {
            final JSONArray jsonArray = new JSONArray();
            jsonArray.add(value);
            return jsonArray;
        } else if (value instanceof JSONArray)
            return (JSONArray) value;
        throw new InvalidCardDefinitionException("Unknown type in " + key + " field");
    }

    public static JSONObject[] getObjectArray(Object value, String key) throws InvalidCardDefinitionException {
        if (value == null)
            return new JSONObject[0];
        else if (value instanceof JSONObject)
            return new JSONObject[]{(JSONObject) value};
        else if (value instanceof final JSONArray array)
            return (JSONObject[]) array.toArray(new JSONObject[0]);
        else throw new InvalidCardDefinitionException("Unknown type in " + key + " field");
    }

    public static void validateAllowedFields(JSONObject object, String... fields) throws InvalidCardDefinitionException {
        Set<String> keys = object.keySet();
        for (String key : keys) {
            if (!key.equals("type") && !contains(fields, key))
                throw new InvalidCardDefinitionException("Unrecognized field: " + key);
        }
    }

    private static boolean contains(String[] fields, String key) {
        for (String field : fields) {
            if (field.equals(key))
                return true;
        }
        return false;
    }
}
