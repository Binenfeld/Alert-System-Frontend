package org.example.alertsystemfrontend.network.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.lang.reflect.Type;

// Makes a StringProperty field read/write as a plain JSON string, instead of Gson reflecting
// into its internal value/name/bean/listener fields.
public class StringPropertyAdapter implements JsonSerializer<StringProperty>, JsonDeserializer<StringProperty> {

    @Override
    public JsonElement serialize(StringProperty src, Type typeOfSrc, JsonSerializationContext context) {
        return src == null || src.get() == null ? JsonNull.INSTANCE : new JsonPrimitive(src.get());
    }

    @Override
    public StringProperty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return new SimpleStringProperty(json.isJsonNull() ? null : json.getAsString());
    }
}
