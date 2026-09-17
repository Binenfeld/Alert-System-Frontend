package org.example.alertsystemfrontend.model;

// Mirrors the backend's Model.Company.ImpressionLevel (same constant names GREAT/GOOD/OKAY/BAD,
// so the two sides agree on the wire value Gson serializes via name()) -- the Hebrew label here is
// purely a frontend display concern and never goes over the wire.
public enum ImpressionLevel {
    GREAT("מצוין"),
    GOOD("טוב"),
    OKAY("בינוני"),
    BAD("רע");

    private final String hebrewLabel;

    ImpressionLevel(String hebrewLabel) {
        this.hebrewLabel = hebrewLabel;
    }

    @Override
    public String toString() {
        return hebrewLabel;
    }
}
