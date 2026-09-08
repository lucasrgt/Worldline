package worldline.b173server;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** Resolves existing named/server Tiny identities without inventing or changing mappings. */
final class B173NativeServerSymbols {
    private final Map<String, String> members = new HashMap<>();
    private final Map<String, String> classes = new HashMap<>(), reverse = new HashMap<>();
    private final Map<String, Method> methods = new HashMap<>();
    private final Map<String, Field> fields = new HashMap<>();
    B173NativeServerSymbols(Path path, boolean official) throws Exception {
        java.util.List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        if (lines.isEmpty() || !lines.get(0).equals("tiny\t2\t0\tnamed\tclient\tserver")) {
            throw new IllegalArgumentException("unsupported native server mappings");
        }
        String owner = null;
        for (String line : lines.subList(1, lines.size())) {
            String[] row = line.split("\t", -1);
            if (row.length == 4 && row[0].equals("c")) {
                owner = row[1]; String name = row[official ? 3 : 1];
                if (!name.isEmpty()) { classes.put(owner, name); reverse.put(name, owner); }
            } else if (owner != null && row.length == 6 && row[0].isEmpty()
                    && (row[1].equals("f") || row[1].equals("m"))) {
                String name = row[official ? 5 : 3];
                if (!name.isEmpty()) members.put(owner + '#' + row[1] + '#' + row[3]
                        + (row[1].equals("m") ? row[2] : ""), name);
            }
        }
    }
    Class<?> type(String shortName) throws Exception {
        String name = classes.get(owner(shortName));
        if (name == null) throw new IllegalArgumentException("unmapped server type: " + shortName);
        return Class.forName(name.replace('/', '.'));
    }
    String named(Class<?> type) {
        String result = reverse.get(type.getName().replace('.', '/'));
        if (result == null) throw new IllegalArgumentException("unexpected native type: " + type);
        return result;
    }
    Object get(Object target, String name) throws Exception { return field(target.getClass(), name).get(target); }
    Object constant(String owner, String name) throws Exception { return field(type(owner), name).get(null); }
    void set(Object target, String name, Object value) throws Exception { field(target.getClass(), name).set(target, value); }
    Object call(Object target, String owner, String name, String descriptor, Object... arguments) throws Exception {
        return method(owner, name, descriptor).invoke(target, arguments);
    }
    Method method(String owner, String name, String descriptor) throws Exception {
        String key = owner + '#' + name + descriptor;
        if (methods.containsKey(key)) return methods.get(key);
        String resolved = member(owner(owner) + "#m#" + name + descriptor);
        String mappedDescriptor = descriptor;
        for (Map.Entry<String, String> entry : classes.entrySet()) {
            mappedDescriptor = mappedDescriptor.replace("L" + entry.getKey() + ";", "L" + entry.getValue() + ";");
        }
        Class<?>[] parameters = java.lang.invoke.MethodType.fromMethodDescriptorString(
                mappedDescriptor, getClass().getClassLoader()).parameterArray();
        Method result = type(owner).getDeclaredMethod(resolved, parameters);
        result.setAccessible(true); methods.put(key, result); return result;
    }
    private Field field(Class<?> type, String name) throws Exception {
        String key = type.getName() + '#' + name;
        if (fields.containsKey(key)) return fields.get(key);
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            String owner = named(current);
            if (members.containsKey(owner + "#f#" + name)) {
                Field result = current.getDeclaredField(member(owner + "#f#" + name));
                result.setAccessible(true); fields.put(key, result); return result;
            }
        }
        throw new NoSuchFieldException(type.getName() + '.' + name);
    }
    private String member(String key) {
        String value = members.get(key);
        if (value == null) throw new IllegalArgumentException("unmapped server member: " + key);
        return value;
    }
    private static String owner(String name) { return name.contains("/") ? name : "net/minecraft/src/" + name; }
}
