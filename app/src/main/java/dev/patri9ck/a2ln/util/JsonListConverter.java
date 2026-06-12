package dev.patri9ck.a2ln.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class JsonListConverter {

    private static final Gson GSON = new Gson();

    private JsonListConverter() {}

    public static String toJson(List<?> data) {
        return GSON.toJson(data);
    }

    public static <T> List<T> fromJson(String json, Class<T> type) {
        if (json == null) {
            return new ArrayList<>();
        }

        return GSON.fromJson(json, TypeToken.getParameterized(ArrayList.class, type).getType());
    }
}
