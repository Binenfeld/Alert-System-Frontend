package org.example.alertsystemfrontend.network.json;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

// Makes an ObservableList<E> field read/write as a plain JSON array of E, instead of Gson
// reflecting into FXCollections' internal list implementation (which it can't construct anyway).
public class ObservableListTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (!ObservableList.class.isAssignableFrom(type.getRawType())) {
            return null;
        }
        Type elementType = elementTypeOf(type.getType());
        TypeAdapter<?> elementAdapter = gson.getAdapter(TypeToken.get(elementType));
        return (TypeAdapter<T>) newObservableListAdapter(elementAdapter);
    }

    private Type elementTypeOf(Type observableListType) {
        if (observableListType instanceof ParameterizedType parameterizedType) {
            return parameterizedType.getActualTypeArguments()[0];
        }
        return Object.class;
    }

    private <E> TypeAdapter<ObservableList<E>> newObservableListAdapter(TypeAdapter<E> elementAdapter) {
        return new TypeAdapter<>() {
            @Override
            public void write(JsonWriter out, ObservableList<E> value) throws IOException {
                if (value == null) {
                    out.nullValue();
                    return;
                }
                out.beginArray();
                for (E element : value) {
                    elementAdapter.write(out, element);
                }
                out.endArray();
            }

            @Override
            public ObservableList<E> read(JsonReader in) throws IOException {
                if (in.peek() == JsonToken.NULL) {
                    in.nextNull();
                    return FXCollections.observableArrayList();
                }
                ObservableList<E> list = FXCollections.observableArrayList();
                in.beginArray();
                while (in.hasNext()) {
                    list.add(elementAdapter.read(in));
                }
                in.endArray();
                return list;
            }
        };
    }
}
