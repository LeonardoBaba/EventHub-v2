package br.com.baba.eventHub.payments.core.util;

public final class CardTokenMasker {

    private CardTokenMasker() {
    }

    public static String mask(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        if (value.length() <= 4) {
            return "****";
        }
        return "****" + value.substring(value.length() - 4);
    }
}
