package org.example.alertsystemfrontend.network.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.lang.reflect.Type;

// Makes an IntegerProperty field read/write as a plain JSON number, instead of Gson reflecting
// into its internal value/name/bean/listener fields.
public class IntegerPropertyAdapter implements JsonSerializer<IntegerProperty>, JsonDeserializer<IntegerProperty> {

    @Override
    public JsonElement serialize(IntegerProperty src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.get());
    }

    @Override
    public IntegerProperty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return new SimpleIntegerProperty(json.getAsInt());
    }
}
