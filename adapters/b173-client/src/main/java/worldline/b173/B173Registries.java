package worldline.b173;

import java.util.SortedMap;
import java.util.TreeMap;
import net.minecraft.src.Block;
import net.minecraft.src.CraftingManager;
import net.minecraft.src.EntityList;
import net.minecraft.src.FurnaceRecipes;
import net.minecraft.src.IRecipe;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ShapedRecipes;
import net.minecraft.src.ShapelessRecipes;

/** Captures b1.7.3 registry state as sorted, bounded census rows. */
final class B173Registries {
    private B173Registries() {}

    static TreeMap<String, String> blocks() {
        TreeMap<String, String> rows = new TreeMap<>();
        for (int id = 0; id < Block.blocksList.length; id++) {
            Block block = Block.blocksList[id];
            if (block != null) rows.put(String.format("b%03d", id), block.getClass().getSimpleName());
        }
        return rows;
    }

    static TreeMap<String, String> items() {
        TreeMap<String, String> rows = new TreeMap<>();
        for (int id = 0; id < Item.itemsList.length; id++) {
            Item item = Item.itemsList[id];
            if (item != null) rows.put(String.format("i%05d", id),
                    item.getClass().getSimpleName()
                            + "|stack=" + item.getItemStackLimit()
                            + "|damage=" + item.getMaxDamage());
        }
        return rows;
    }

    static TreeMap<String, String> entities() {
        @SuppressWarnings("unchecked")
        java.util.Map<Integer, Class<?>> byId = (java.util.Map<Integer, Class<?>>)
                B173Reflect.get(EntityList.class, "IDtoClassMapping", null);
        @SuppressWarnings("unchecked")
        java.util.Map<Class<?>, String> byClass = (java.util.Map<Class<?>, String>)
                B173Reflect.get(EntityList.class, "classToStringMapping", null);
        TreeMap<String, String> rows = new TreeMap<>();
        for (java.util.Map.Entry<Integer, Class<?>> entry : byId.entrySet()) {
            int id = entry.getKey();
            Class<?> type = entry.getValue();
            String name = byClass.get(type);
            if (id < 0 || id > 999 || name == null || name.isEmpty()) {
                throw new IllegalStateException("invalid EntityList mapping " + id);
            }
            rows.put(String.format("e%03d", id),
                    "name=" + name + "|class=" + type.getSimpleName());
        }
        return rows;
    }

    static TreeMap<String, String> recipes() {
        TreeMap<String, String> rows = new TreeMap<>();
        int index = 0;
        for (Object value : CraftingManager.getInstance().getRecipeList()) {
            if (!(value instanceof IRecipe)) continue;
            IRecipe recipe = (IRecipe) value;
            ItemStack output = recipe.getRecipeOutput();
            if (output == null) continue;
            StringBuilder row = new StringBuilder("out=").append(output.itemID)
                    .append('x').append(output.stackSize).append(" in=");
            row.append(inputs(recipe));
            rows.put(String.format("r%04d", index), row.toString());
            index++;
        }
        return rows;
    }

    static TreeMap<String, String> smelts() {
        TreeMap<String, String> rows = new TreeMap<>();
        for (Object raw : FurnaceRecipes.smelting().getSmeltingList().entrySet()) {
            java.util.Map.Entry<?, ?> entry = (java.util.Map.Entry<?, ?>) raw;
            int inputKey = (Integer) entry.getKey();
            ItemStack output = (ItemStack) entry.getValue();
            rows.put(String.format("s%08d", inputKey),
                    "in=" + inputKey + " out=" + output.itemID + "x" + output.stackSize);
        }
        return rows;
    }

    private static String inputs(IRecipe recipe) {
        StringBuilder inputs = new StringBuilder();
        if (recipe instanceof ShapedRecipes) {
            ShapedRecipes shaped = (ShapedRecipes) recipe;
            Object shapedItems = B173Reflect.get(ShapedRecipes.class, "recipeItems", shaped);
            for (ItemStack stack : (ItemStack[]) shapedItems) append(inputs, stack);
        } else if (recipe instanceof ShapelessRecipes) {
            ShapelessRecipes shapeless = (ShapelessRecipes) recipe;
            Object looseItems = B173Reflect.get(ShapelessRecipes.class, "recipeItems", shapeless);
            for (Object value : (java.util.List<?>) looseItems) append(inputs, (ItemStack) value);
        }
        if (inputs.length() == 0) return "none";
        return inputs.substring(0, inputs.length() - 1);
    }

    private static void append(StringBuilder target, ItemStack stack) {
        target.append(stack == null ? 0 : stack.itemID).append(',');
    }
}
