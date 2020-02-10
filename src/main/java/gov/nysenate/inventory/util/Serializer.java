package gov.nysenate.inventory.util;

import com.google.gson.*;
import gov.nysenate.inventory.server.VerificationReports;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Serializer {
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Serializer.class.getName());

    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();

    /**
     * Serialize an object into a json string
     */
    public static <T> String serialize(T type) {
        return gson.toJson(type);
    }

    /**
     * Constructs a list of objects from a json string.
     * @param json The json string.
     * @param clazz The type of objects.
     * @param <T>
     * @return
     */
    public static <T> List<T> deserialize(String json, Class<T> clazz) {
        List<T> results = new ArrayList<>();
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(json);
        if (element.isJsonObject()) {
            results.add(gson.fromJson(json, clazz));
        } else {
            JsonArray array = element.getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                log.info(i+": "+array.get(i).toString());
                results.add(gson.fromJson(array.get(i).toString(), clazz));
            }
        }
        return results;
    }
}
