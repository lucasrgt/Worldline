package worldline.b173;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** Resolves only existing named/client Tiny v2 entries for native reflection fixtures. */
final class B173NativeNames {
    private final boolean official;
    private final Map<String, String> classes = new HashMap<>(), reverse = new HashMap<>();
    private final Map<String, String> members = new HashMap<>();

    B173NativeNames(Path path, boolean official) throws Exception {
        this.official = official;
        java.util.List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        if (lines.isEmpty() || !lines.get(0).equals("tiny\t2\t0\tnamed\tclient\tserver")) {
            throw new IllegalArgumentException("unsupported native mappings");
        }
        String owner = null;
        for (String line : lines.subList(1, lines.size())) {
            String[] row = line.split("\t", -1);
            if (row.length >= 3 && row[0].equals("c")) {
                owner = row[1]; classes.put(owner, row[2]);
                if (!row[2].isEmpty()) reverse.put(row[2], owner);
            } else if (owner != null && row.length >= 5 && row[0].isEmpty()
                    && (row[1].equals("f") || row[1].equals("m"))) {
                String signature = row[1].equals("m")
                        ? row[2].substring(0, row[2].indexOf(')') + 1) : "";
                members.put(owner + '#' + row[1] + '#' + row[3] + signature, row[4]);
            }
        }
    }

    Class<?> type(String owner) throws Exception {
        String name = owner(owner);
        String resolved = official ? classes.get(name) : name;
        if (resolved == null || resolved.isEmpty()) throw new IllegalArgumentException("unmapped " + name);
        return Class.forName(resolved.replace('/', '.'));
    }

    String named(Class<?> type) {
        String name = type.getName().replace('.', '/');
        String resolved = official ? reverse.get(name) : name;
        if (resolved == null) throw new IllegalArgumentException("unmapped runtime type " + name);
        return resolved;
    }

    Method method(String owner, String name, Class<?>... parameters) throws Exception {
        StringBuilder descriptor = new StringBuilder("(");
        for (Class<?> parameter : parameters) descriptor.append(descriptor(parameter));
        descriptor.append(')');
        return type(owner).getMethod(member(owner(owner), "m", name, descriptor.toString()), parameters);
    }

    Object get(Object target, String name) throws Exception { return field(target.getClass(), name).get(target); }
    void set(Object target, String name, Object value) throws Exception {
        field(target.getClass(), name).set(target, value);
    }
    Object singleton(String owner, String name) throws Exception { return field(type(owner), name).get(null); }

    private Field field(Class<?> type, String name) throws Exception {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            String owner = named(current);
            if (!members.containsKey(owner + "#f#" + name)) continue;
            Field field = current.getDeclaredField(member(owner, "f", name, ""));
            field.setAccessible(true); return field;
        }
        throw new NoSuchFieldException(type.getName() + '.' + name);
    }

    private String member(String owner, String kind, String name, String descriptor) {
        String mapped = members.get(owner + '#' + kind + '#' + name + descriptor);
        if (mapped == null || mapped.isEmpty()) {
            throw new IllegalArgumentException("unmapped member " + owner + '.' + name);
        }
        return official ? mapped : name;
    }

    private String descriptor(Class<?> type) {
        if (type == int.class) return "I";
        if (type == float.class) return "F";
        if (type == double.class) return "D";
        if (type == boolean.class) return "Z";
        if (type == long.class) return "J";
        return "L" + named(type) + ";";
    }

    private static String owner(String value) {
        if (value.indexOf('/') >= 0) return value;
        return value.equals("Minecraft") ? "net/minecraft/client/Minecraft" : "net/minecraft/src/" + value;
    }
}
