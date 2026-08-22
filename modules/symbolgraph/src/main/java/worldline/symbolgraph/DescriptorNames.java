package worldline.symbolgraph;

import java.util.Map;

/** Remaps object types in JVM descriptors while preserving primitives and arrays. */
final class DescriptorNames {
    private DescriptorNames() {}

    static String remap(String descriptor, Map<String, String> classes) {
        if (descriptor == null || descriptor.isEmpty()) throw new IllegalArgumentException("descriptor");
        StringBuilder result = new StringBuilder();
        int cursor = 0;
        while (cursor < descriptor.length()) {
            char token = descriptor.charAt(cursor);
            if (token != 'L') {
                result.append(token);
                cursor++;
                continue;
            }
            int end = descriptor.indexOf(';', cursor);
            if (end < 0) throw new IllegalArgumentException("malformed descriptor: " + descriptor);
            String original = descriptor.substring(cursor + 1, end);
            String mapped = classes.get(original);
            result.append('L').append(mapped == null || mapped.isEmpty() ? original : mapped).append(';');
            cursor = end + 1;
        }
        return result.toString();
    }
}
