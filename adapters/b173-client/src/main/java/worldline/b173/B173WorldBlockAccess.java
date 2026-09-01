package worldline.b173;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/** Minimal deterministic IBlockAccess used by the native world-render oracle. */
final class B173WorldBlockAccess implements InvocationHandler {
    private final Object blocks, airMaterial;
    private final Field materialField;
    private final String blockIdMethod, metadataMethod;
    private final int legacyId, metadata;

    B173WorldBlockAccess(Object blocks, Field materialField, Object airMaterial,
            String blockIdMethod, String metadataMethod, int legacyId, int metadata) {
        this.blocks = blocks;
        this.materialField = materialField;
        this.airMaterial = airMaterial;
        this.blockIdMethod = blockIdMethod;
        this.metadataMethod = metadataMethod;
        this.legacyId = legacyId;
        this.metadata = metadata;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] arguments) throws Exception {
        String name = method.getName();
        if (name.equals("toString")) return "WorldlineB173WorldBlockAccess";
        if (name.equals("hashCode")) return Integer.valueOf(System.identityHashCode(proxy));
        if (name.equals("equals")) return Boolean.valueOf(proxy == arguments[0]);
        int x = coordinate(arguments, 0), y = coordinate(arguments, 1);
        int z = coordinate(arguments, 2), blockId = blockId(x, y, z);
        Class<?> result = method.getReturnType();
        if (result == int.class && name.equals(blockIdMethod)) return Integer.valueOf(blockId);
        if (result == int.class && name.equals(metadataMethod)) {
            return Integer.valueOf(metadata(x, y, z));
        }
        if (result == float.class) return Float.valueOf(1.0f);
        if (result == boolean.class) return Boolean.valueOf(y == -1);
        if (result.isInstance(airMaterial)) return material(blockId);
        return null;
    }

    private int blockId(int x, int y, int z) {
        if (x == 0 && y == 0 && z == 0) return legacyId;
        if ((legacyId == 64 || legacyId == 71) && x == 0 && y == 1 && z == 0) {
            return legacyId;
        }
        if (legacyId == 65 && x == 0 && y == 0 && z == 1) return 1;
        return x == 0 && y == -1 && z == 0 ? 1 : 0;
    }

    private int metadata(int x, int y, int z) {
        if (x == 0 && y == 0 && z == 0) return metadata;
        if ((legacyId == 64 || legacyId == 71) && x == 0 && y == 1 && z == 0) return 8;
        return 0;
    }

    private Object material(int blockId) throws Exception {
        if (blockId == 0) return airMaterial;
        Object block = Array.get(blocks, blockId);
        return materialField.get(block);
    }

    private static int coordinate(Object[] arguments, int index) {
        return arguments != null && arguments.length > index
                && arguments[index] instanceof Integer ? ((Integer) arguments[index]).intValue() : 0;
    }
}
