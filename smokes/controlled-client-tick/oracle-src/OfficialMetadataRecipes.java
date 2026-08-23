import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Reads exact metadata-sensitive recipes from the official client registry. */
final class OfficialMetadataRecipes {
    private static final String[] EXPECTED = {
        "35:1:0+351:1:1->35:1:14", "35:1:0+351:1:2->35:1:13",
        "35:1:0+351:1:4->35:1:11", "352:1:0->351:3:15",
        "38:1:0->351:2:1", "37:1:0->351:2:11",
        "351:1:0+351:1:15->351:2:8", "24:3:0->44:3:1",
        "5:3:0->44:3:2", "4:3:0->44:3:3",
        "351:1:1+351:1:11->351:2:14", "351:1:1+351:1:4->351:2:5",
        "351:1:2+351:1:15->351:2:10", "35:1:0+351:1:11->35:1:4",
        "35:1:0+351:1:14->35:1:1", "35:1:0+351:1:9->35:1:6",
        "351:1:2+351:1:4->351:2:6", "351:1:1+351:1:15->351:2:9",
        "351:1:4+351:1:15->351:2:12", "35:1:0+351:1:13->35:1:2",
        "35:1:0+351:1:12->35:1:3", "35:1:0+351:1:10->35:1:5",
        "351:1:0+351:2:15->351:3:7", "351:1:8+351:1:15->351:2:7",
        "351:1:5+351:1:9->351:2:13"
    };
    private OfficialMetadataRecipes() {}

    static void verify() {
        try {
            Class<?> managerType = Class.forName("hk");
            Object manager = method(managerType, "a").invoke(null);
            List<?> recipes = (List<?>) method(managerType, "b").invoke(manager);
            require(recipes.contains(null) == false, "official recipe registry contains null");
            for (String expected : EXPECTED)
                require(contains(recipes, expected), "official metadata recipe absent: " + expected);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("cannot inspect official metadata recipes", error);
        }
    }

    private static boolean contains(List<?> recipes, String expected)
            throws ReflectiveOperationException {
        for (Object recipe : recipes) if (expected.equals(describe(recipe))) return true;
        return false;
    }

    private static String describe(Object recipe) throws ReflectiveOperationException {
        String type = recipe.getClass().getName();
        List<Object> inputs = new ArrayList<Object>();
        if (type.equals("is")) {
            Object array = field(recipe.getClass(), "d").get(recipe);
            for (int index = 0; index < Array.getLength(array); index++) {
                Object value = Array.get(array, index);
                if (value != null) inputs.add(value);
            }
        } else if (type.equals("tt")) {
            inputs.addAll((List<?>) field(recipe.getClass(), "b").get(recipe));
        } else return "";
        Object output = method(recipe.getClass(), "b").invoke(recipe);
        return canonical(inputs) + "->" + stack(output);
    }

    private static String canonical(List<?> stacks) throws ReflectiveOperationException {
        TreeMap<Long, Integer> totals = new TreeMap<Long, Integer>();
        for (Object value : stacks) {
            int id = field(value.getClass(), "c").getInt(value);
            int count = field(value.getClass(), "a").getInt(value);
            int damage = ((Integer) method(value.getClass(), "i").invoke(value)).intValue();
            if (damage < 0) {
                if (((Boolean) method(value.getClass(), "e").invoke(value)).booleanValue())
                    return "wildcard";
                damage = 0;
            }
            long key = ((long) id << 32) | damage;
            totals.put(key, totals.getOrDefault(key, 0) + count);
        }
        StringBuilder result = new StringBuilder();
        for (Map.Entry<Long, Integer> entry : totals.entrySet()) {
            if (result.length() > 0) result.append('+');
            long key = entry.getKey();
            result.append((int) (key >>> 32)).append(':').append(entry.getValue())
                    .append(':').append((int) key);
        }
        return result.toString();
    }

    private static String stack(Object value) throws ReflectiveOperationException {
        return field(value.getClass(), "c").getInt(value) + ":"
                + field(value.getClass(), "a").getInt(value) + ":"
                + method(value.getClass(), "i").invoke(value);
    }

    private static Field field(Class<?> type, String name) throws ReflectiveOperationException {
        Field value = type.getDeclaredField(name); value.setAccessible(true); return value;
    }
    private static Method method(Class<?> type, String name) throws ReflectiveOperationException {
        Method value = type.getDeclaredMethod(name); value.setAccessible(true); return value;
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
