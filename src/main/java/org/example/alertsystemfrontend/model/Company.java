package org.example.alertsystemfrontend.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Company {
    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty companyId = new SimpleStringProperty("");
    private final ObjectProperty<ImpressionLevel> peopleImpression = new SimpleObjectProperty<>();
    private final ObjectProperty<ImpressionLevel> financialImpression = new SimpleObjectProperty<>();
    private final IntegerProperty unpaidMonths = new SimpleIntegerProperty(0);
    private final ObservableList<Comment> comments = FXCollections.observableArrayList();

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty companyIdProperty() {
        return companyId;
    }

    public ObjectProperty<ImpressionLevel> peopleImpressionProperty() {
        return peopleImpression;
    }

    public ObjectProperty<ImpressionLevel> financialImpressionProperty() {
        return financialImpression;
    }

    public IntegerProperty unpaidMonthsProperty() {
        return unpaidMonths;
    }

    public void incrementUnpaidMonths() {
        unpaidMonths.set(unpaidMonths.get() + 1);
    }

    public void decrementUnpaidMonths() {
        if (unpaidMonths.get() > 0) {
            unpaidMonths.set(unpaidMonths.get() - 1);
        }
    }

    public ObservableList<Comment> getComments() {
        return comments;
    }

    public void addComment(String text) {
        comments.add(new Comment(text));
    }
}
