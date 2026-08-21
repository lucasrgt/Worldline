package worldline.cli;

import java.lang.reflect.InvocationTargetException;

/** Shared CLI helpers: strict numbers and reflective provider binding. */
final class Checks {
    private Checks() {}

    static long seed(String text) {
        require(text != null && text.matches("-?(0|[1-9][0-9]{0,18})"), "invalid seed");
        return Long.parseLong(text);
    }

    static int ticks(String text) {
        require(text != null && text.matches("[1-9][0-9]{0,5}"), "invalid tick count");
        return Integer.parseInt(text);
    }

    static <T> T provider(String property, String fallback, Class<T> type) {
        String name = System.getProperty(property, fallback);
        try {
            return Class.forName(name).asSubclass(type).getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("provider " + name + " is unavailable", error);
        }
    }


    static String sha256(byte[] value) {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256").digest(value);
            StringBuilder result = new StringBuilder();
            for (byte item : digest) result.append(String.format("%02x", item & 255));
            return result.toString();
        } catch (java.security.NoSuchAlgorithmException error) {
            throw new IllegalStateException(error);
        }
    }
    static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
