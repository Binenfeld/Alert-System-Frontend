package org.example.alertsystemfrontend.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Comment {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final String text;
    private final String timestamp;

    public Comment(String text) {
        this.text = text;
        this.timestamp = LocalDateTime.now().format(FORMATTER);
    }

    public String getText() {
        return text;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
