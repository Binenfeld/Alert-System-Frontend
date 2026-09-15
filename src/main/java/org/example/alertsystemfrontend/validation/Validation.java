package org.example.alertsystemfrontend.validation;

import java.util.regex.Pattern;

public final class Validation {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    private Validation() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static boolean isValidEmail(String value) {
        return value != null && EMAIL_PATTERN.matcher(value.trim()).matches();
    }

    // Israeli company/organization number (ח"פ): 9 digits validated with the same
    // Luhn-style check-digit algorithm used for Israeli ID numbers.
    public static boolean isValidCompanyId(String value) {
        if (value == null) return false;
        String digits = value.trim();
        if (!digits.matches("\\d{9}")) return false;
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            int digit = digits.charAt(i) - '0';
            int weight = (i % 2 == 0) ? 1 : 2;
            int product = digit * weight;
            sum += product > 9 ? product - 9 : product;
        }
        return sum % 10 == 0;
    }
}
