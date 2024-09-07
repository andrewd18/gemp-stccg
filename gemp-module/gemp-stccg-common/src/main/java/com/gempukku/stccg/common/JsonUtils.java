package com.gempukku.stccg.common;

import com.alibaba.fastjson.JSON;
import org.apache.commons.io.FilenameUtils;
import org.hjson.JsonValue;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

public final class JsonUtils {

    public static boolean IsInvalidHjsonFile(File file) {
        String ext = FilenameUtils.getExtension(file.getName());
        return !ext.equalsIgnoreCase("json") && !ext.equalsIgnoreCase("hjson");
    }

    //Reads both json and hjson files, converting both to json (for compatibility with other libraries)
    public static String ReadJson(Reader reader) throws IOException {
        return JsonValue.readHjson(reader).toString();
    }

    //Reads the loaded json or hjson file and deserializes as an instance of the provided class
    public static <T> T Convert(Reader reader, Class<T> clazz) throws IOException {
        final String json = ReadJson(reader);
        return JSON.parseObject(json, clazz);
    }

    public static <T> List<T> ConvertArray(Reader reader, Class<T> clazz) throws IOException {
        final String json = ReadJson(reader);
        try {
            T[] jsonList = (T[]) JSON.parseObject(json, clazz.arrayType());
            return Arrays.stream(jsonList).toList();
        }
        catch(Exception ex)
        {
            throw new IOException("Error");
        }

    }
}
