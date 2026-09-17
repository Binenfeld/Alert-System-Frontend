package org.example.alertsystemfrontend.network.json;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

// Makes an ObjectProperty<E> field (e.g. Company's ImpressionLevel properties) read/write as a
// plain JSON value of E, delegating to whatever adapter Gson already has for E -- for an enum
// like ImpressionLevel that's Gson's normal name()-based adapter, so it round-trips as e.g.
// "GREAT" with no extra code needed here.
public class ObjectPropertyTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (!ObjectProperty.class.isAssignableFrom(type.getRawType())) {
            return null;
        }
        Type elementType = elementTypeOf(type.getType());
        TypeAdapter<?> elementAdapter = gson.getAdapter(TypeToken.get(elementType));
        return (TypeAdapter<T>) newObjectPropertyAdapter(elementAdapter);
    }

    private Type elementTypeOf(Type objectPropertyType) {
        if (objectPropertyType instanceof ParameterizedType parameterizedType) {
            return parameterizedType.getActualTypeArguments()[0];
        }
        return Object.class;
    }

    private <E> TypeAdapter<ObjectProperty<E>> newObjectPropertyAdapter(TypeAdapter<E> elementAdapter) {
        return new TypeAdapter<>() {
            @Override
            public void write(JsonWriter out, ObjectProperty<E> value) throws IOException {
                if (value == null) {
                    out.nullValue();
                    return;
                }
                elementAdapter.write(out, value.get());
            }

            @Override
            public ObjectProperty<E> read(JsonReader in) throws IOException {
                if (in.peek() == JsonToken.NULL) {
                    in.nextNull();
                    return new SimpleObjectProperty<>(null);
                }
                return new SimpleObjectProperty<>(elementAdapter.read(in));
            }
        };
    }
}
