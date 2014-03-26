/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nysenate.inventory.server;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import gov.nysenate.inventory.model.InvItem;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author heitner
 */
public class InvItemTypeAdapter implements JsonDeserializer<List<InvItem>> {

    @Override
    public List<InvItem> deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<InvItem> vals = new ArrayList<InvItem>();
        if (json.isJsonArray()) {
            for (JsonElement e : json.getAsJsonArray()) {
                vals.add((InvItem) context.deserialize(e, InvItem.class));
            }
        } else if (json.isJsonObject()) {
            vals.add((InvItem) context.deserialize(json, InvItem.class));
        } else {
            throw new RuntimeException("Unexpected JSON type: " + json.getClass());
        }
        return vals;
    }
}