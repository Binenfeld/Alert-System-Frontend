package org.example.alertsystemfrontend.network.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

// Single shared Gson instance for the wire protocol: knows how to read/write JavaFX property and
// ObservableList fields on the model classes (Company, AlertContact, ...) as their plain JSON
// counterparts (string, number, array), so the model classes can be handed to Gson directly.
public final class GsonProvider {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(StringProperty.class, new StringPropertyAdapter())
            .registerTypeHierarchyAdapter(IntegerProperty.class, new IntegerPropertyAdapter())
            .registerTypeAdapterFactory(new ObservableListTypeAdapterFactory())
            .registerTypeAdapterFactory(new ObjectPropertyTypeAdapterFactory())
            .create();

    private GsonProvider() {
    }

    public static Gson get() {
        return GSON;
    }
}
