package worldline.smoke.clientb173;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.ItemStackRecipe;
import worldline.api.RemoteItemStack;

/** Verifies every metadata-sensitive recipe family used by pending behavior contracts. */
final class MetadataRecipeContract {
  private MetadataRecipeContract() {
  }

  static void verify(List<ItemStackRecipe> recipes) {
    require(!recipes.isEmpty(), "metadata recipe snapshot is empty");
    wool(recipes, 1, 14);
    wool(recipes, 2, 13);
    wool(recipes, 4, 11);
    expect(recipes, out(351, 3, 15), in(352, 1, 0));
    expect(recipes, out(351, 2, 1), in(38, 1, 0));
    expect(recipes, out(351, 2, 11), in(37, 1, 0));
    expect(recipes, out(351, 2, 8), in(351, 1, 0), in(351, 1, 15));
    slab(recipes, 24, 1);
    slab(recipes, 5, 2);
    slab(recipes, 4, 3);
    dye(recipes, 1, 11, 14);
    dye(recipes, 1, 4, 5);
    dye(recipes, 2, 15, 10);
    wool(recipes, 11, 4);
    wool(recipes, 14, 1);
    wool(recipes, 9, 6);
    dye(recipes, 2, 4, 6);
    dye(recipes, 1, 15, 9);
    dye(recipes, 4, 15, 12);
    wool(recipes, 13, 2);
    wool(recipes, 12, 3);
    wool(recipes, 10, 5);
    expect(recipes, out(351, 3, 7), in(351, 1, 0), in(351, 2, 15));
    dye(recipes, 8, 15, 7);
    dye(recipes, 5, 9, 13);
    try {
      recipes.clear();
      throw new IllegalStateException("metadata recipes are mutable");
    } catch (UnsupportedOperationException expected) {
    }
  }

  private static void wool(List<ItemStackRecipe> recipes, int dye, int damage) {
    expect(recipes, out(35, 1, damage), in(35, 1, 0), in(351, 1, dye));
  }
  private static void slab(List<ItemStackRecipe> recipes, int input, int damage) {
    expect(recipes, out(44, 3, damage), in(input, 3, 0));
  }
  private static void dye(List<ItemStackRecipe> recipes, int left, int right, int damage) {
    expect(recipes, out(351, 2, damage), in(351, 1, left), in(351, 1, right));
  }
  private static void expect(
      List<ItemStackRecipe> recipes, RemoteItemStack output, RemoteItemStack... inputs) {
    ItemStackRecipe expected =
        new ItemStackRecipe(Arrays.asList(inputs), Collections.singletonList(output));
    if (recipes.contains(expected))
      return;
    StringBuilder related = new StringBuilder();
    for (ItemStackRecipe recipe : recipes)
      if (recipe.outputs().get(0).legacyId() == output.legacyId())
        related.append(recipe.inputs()).append("->").append(recipe.outputs()).append(';');
    throw new IllegalStateException("metadata recipe absent: " + expected.inputs() + "->"
        + expected.outputs() + ", related=" + related);
  }
  private static RemoteItemStack in(int id, int count, int damage) {
    return new RemoteItemStack(id, count, damage);
  }
  private static RemoteItemStack out(int id, int count, int damage) {
    return in(id, count, damage);
  }
  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
}
