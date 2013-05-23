/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nysenate.inventory.server;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.sun.org.apache.bcel.internal.generic.Type;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author heitner
 */
public class PickupGroupTypeAdapter implements JsonDeserializer<List<PickupGroup>> {

    @Override
    public List<PickupGroup> deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<PickupGroup> vals = new ArrayList<PickupGroup>();
        if (json.isJsonArray()) {
            for (JsonElement e : json.getAsJsonArray()) {
                vals.add((PickupGroup) context.deserialize(e, PickupGroup.class));
            }
        } else if (json.isJsonObject()) {
            vals.add((PickupGroup) context.deserialize(json, PickupGroup.class));
        } else {
            throw new RuntimeException("Unexpected JSON type: " + json.getClass());
        }
        return vals;
    }


 }