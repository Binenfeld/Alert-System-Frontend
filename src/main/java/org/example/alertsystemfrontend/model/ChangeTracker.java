package org.example.alertsystemfrontend.model;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.function.Function;

// Watches an ObservableList and flags itself dirty when an item is added or removed, or when one
// of the Observable properties `watchedProperties` returns for an item changes in place (e.g. a
// bound TextField edit, or a nested ObservableList like Company's comments). Call markClean() once
// the network layer has successfully sent the list's current contents.
public class ChangeTracker<T> {

    private final BooleanProperty changed = new SimpleBooleanProperty(false);
    private final Function<T, Observable[]> watchedProperties;
    private final InvalidationListener itemListener = obs -> changed.set(true);

    public ChangeTracker(ObservableList<T> list, Function<T, Observable[]> watchedProperties) {
        this.watchedProperties = watchedProperties;
        for (T item : list) {
            attach(item);
        }
        list.addListener((ListChangeListener<T>) change -> {
            while (change.next()) {
                change.getAddedSubList().forEach(this::attach);
                change.getRemoved().forEach(this::detach);
                if (change.wasAdded() || change.wasRemoved()) {
                    changed.set(true);
                }
            }
        });
    }

    public boolean isChanged() {
        return changed.get();
    }

    public BooleanProperty changedProperty() {
        return changed;
    }

    public void markClean() {
        changed.set(false);
    }

    private void attach(T item) {
        for (Observable observable : watchedProperties.apply(item)) {
            observable.addListener(itemListener);
        }
    }

    private void detach(T item) {
        for (Observable observable : watchedProperties.apply(item)) {
            observable.removeListener(itemListener);
        }
    }
}
