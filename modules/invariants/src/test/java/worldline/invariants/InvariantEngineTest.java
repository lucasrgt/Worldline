package worldline.invariants;

import java.util.Arrays;
import java.util.Collections;
import worldline.api.CauseDrop;
import worldline.api.EntityCensus;
import worldline.api.GameUiNode;
import worldline.api.InvariantSample;
import worldline.api.InvariantViolation;
import worldline.api.FoodHeal;
import worldline.api.ItemCensus;
import worldline.api.ItemRecipe;
import worldline.api.SpawnRule;
import worldline.api.WearCensus;

public final class InvariantEngineTest {
    private InvariantEngineTest() {}

    public static void main(String[] arguments) {
        transferAndLossHold();
        creationFailsClosed();
        restoreAfterLossFailsClosed();
        explainedCraftHolds();
        unexplainedLeftoverFailsClosed();
        smeltAllowsExtraFuelLoss();
        cakeReturnsEmptyBuckets();
        unmodeledContainerLeftoverFailsClosed();
        stoneBreakHolds();
        sameIdHarvestHolds();
        cobbleWithoutBrokenStoneFails();
        zombieDeathHolds();
        importedChestHolds();
        chickenEggHolds();
        caughtFishHolds();
        timeGoingBackwardFails();
        pigBreedingHolds();
        lonePigSpawnFails();
        grassSpreadHolds();
        peacefulRegenHolds();
        unearnedHealFails();
        wearHoldsAndRepairFails();
        grassPigSpawnHolds();
        slimeSplitHolds();
        caneGrowthHolds();
        cobbleFromFluidsHolds();
        saplingBecomesTreeHolds();
        cakeBiteHolds();
        fallingSandHolds();
        arrowFromBowHolds();
        uiNodesFeedTheCensus();
        engineRejectsEmptyConfiguration();
        System.out.println("InvariantEngineTest passed");
    }

    private static void transferAndLossHold() {
        InvariantEngine engine = InvariantEngine.itemConservation();
        engine.observe(ItemCensus.of(265, 10).plus(50, 2));
        engine.observe(ItemCensus.of(50, 2).plus(265, 10));
        engine.observe(ItemCensus.of(265, 7));
        require(engine.names().equals(Collections.singletonList(ItemConservation.NAME)),
                "item-conservation was not registered");
    }

    private static void creationFailsClosed() {
        InvariantEngine engine = InvariantEngine.itemConservation();
        engine.observe(ItemCensus.of(265, 10));
        try {
            engine.observe(ItemCensus.of(265, 11));
            throw new AssertionError("expected item creation to fail");
        } catch (InvariantViolation violation) {
            require(ItemConservation.NAME.equals(violation.invariant())
                    && violation.getMessage().contains("item 265 grew from 10 to 11"),
                    "creation detail failed: " + violation.getMessage());
        }
        InvariantEngine novel = InvariantEngine.itemConservation();
        novel.observe(ItemCensus.empty());
        try {
            novel.observe(ItemCensus.of(3, 1));
            throw new AssertionError("expected a new item ID to fail");
        } catch (InvariantViolation violation) {
            require(violation.getMessage().contains("item 3 grew from 0 to 1"),
                    "new item detail failed: " + violation.getMessage());
        }
    }

    private static void restoreAfterLossFailsClosed() {
        InvariantEngine engine = InvariantEngine.itemConservation();
        engine.observe(ItemCensus.of(265, 10));
        engine.observe(ItemCensus.of(265, 7));
        try {
            engine.observe(ItemCensus.of(265, 10));
            throw new AssertionError("expected a restored count to fail");
        } catch (InvariantViolation violation) {
            require(violation.getMessage().contains("item 265 grew from 7 to 10"),
                    "restore detail failed: " + violation.getMessage());
        }
    }

    private static void explainedCraftHolds() {
        InvariantEngine engine = InvariantEngine.itemConservation(planks());
        engine.observe(ItemCensus.of(17, 2));
        engine.observe(ItemCensus.of(5, 8));
        engine.observe(ItemCensus.of(5, 4));
    }

    private static void unexplainedLeftoverFailsClosed() {
        InvariantEngine engine = InvariantEngine.itemConservation(planks());
        engine.observe(ItemCensus.of(17, 1));
        try {
            engine.observe(ItemCensus.of(5, 5));
            throw new AssertionError("expected leftover plank creation to fail");
        } catch (InvariantViolation violation) {
            require(violation.getMessage().contains("item 5 grew from 0 to 5"),
                    "leftover detail failed: " + violation.getMessage());
        }
    }

    private static void smeltAllowsExtraFuelLoss() {
        ItemRecipe iron = new ItemRecipe(ItemCensus.of(15, 1), ItemCensus.of(265, 1));
        InvariantEngine engine = InvariantEngine.itemConservation(
                RecipeBook.of(Collections.singletonList(iron)));
        engine.observe(ItemCensus.of(15, 1).plus(263, 1));
        engine.observe(ItemCensus.of(265, 1));
    }

    private static void cakeReturnsEmptyBuckets() {
        InvariantEngine engine = InvariantEngine.itemConservation(cake(true));
        engine.observe(cakeInputs());
        engine.observe(ItemCensus.of(354, 1).plus(325, 3));
    }

    private static void unmodeledContainerLeftoverFailsClosed() {
        InvariantEngine engine = InvariantEngine.itemConservation(cake(false));
        engine.observe(cakeInputs());
        try {
            engine.observe(ItemCensus.of(354, 1).plus(325, 3));
            throw new AssertionError("expected unmodeled empty buckets to fail");
        } catch (InvariantViolation violation) {
            require(violation.getMessage().contains("item 325 grew from 0 to 3"),
                    "container leftover detail failed: " + violation.getMessage());
        }
    }

    private static ItemCensus cakeInputs() {
        return ItemCensus.of(335, 3).plus(353, 2).plus(296, 3).plus(344, 1);
    }

    private static RecipeBook cake(boolean leftovers) {
        ItemCensus outputs = ItemCensus.of(354, 1);
        if (leftovers) outputs = outputs.plus(325, 3);
        return RecipeBook.of(Collections.singletonList(new ItemRecipe(cakeInputs(), outputs)));
    }

    private static void stoneBreakHolds() {
        InvariantEngine engine = InvariantEngine.itemConservation(stoneDrop());
        engine.observe(ItemCensus.empty(), ItemCensus.of(1, 2));
        engine.observe(ItemCensus.of(4, 1), ItemCensus.of(1, 1));
    }

    private static void sameIdHarvestHolds() {
        InvariantEngine engine = InvariantEngine.itemConservation(RecipeBook.of(
                Collections.singletonList(new ItemRecipe(ItemCensus.of(17, 1), ItemCensus.of(17, 1)))));
        engine.observe(ItemCensus.empty(), ItemCensus.of(17, 1));
        engine.observe(ItemCensus.of(17, 1), ItemCensus.empty());
    }

    private static void cobbleWithoutBrokenStoneFails() {
        InvariantEngine engine = InvariantEngine.itemConservation(stoneDrop());
        engine.observe(ItemCensus.empty(), ItemCensus.of(1, 2));
        try {
            engine.observe(ItemCensus.of(4, 1), ItemCensus.of(1, 2));
            throw new AssertionError("expected cobble without a broken stone to fail");
        } catch (InvariantViolation violation) {
            require(violation.getMessage().contains("item 4 grew from 0 to 1"),
                    "unbroken stone detail failed: " + violation.getMessage());
        }
    }

    private static void zombieDeathHolds() {
        InvariantEngine engine = InvariantEngine.itemConservation(RecipeBook.none(),
                DropBook.of(Collections.singletonList(
                        CauseDrop.death("minecraft:zombie", ItemCensus.of(288, 2)))));
        engine.observe(InvariantSample.of(ItemCensus.empty(), ItemCensus.empty(),
                EntityCensus.of("minecraft:zombie", 1), ItemCensus.empty(), 0L));
        engine.observe(InvariantSample.of(ItemCensus.of(288, 2), ItemCensus.empty(),
                EntityCensus.empty(), ItemCensus.empty(), 1L));
    }

    private static void importedChestHolds() {
        InvariantEngine engine = InvariantEngine.itemConservation();
        engine.observe(ItemCensus.empty());
        engine.observe(InvariantSample.of(ItemCensus.of(265, 10), ItemCensus.empty(),
                EntityCensus.empty(), ItemCensus.of(265, 10), 1L));
    }

    private static void chickenEggHolds() {
        InvariantEngine engine = InvariantEngine.itemConservation(RecipeBook.none(),
                DropBook.of(Collections.singletonList(
                        CauseDrop.presence("minecraft:chicken", ItemCensus.of(344, 1)))));
        engine.observe(InvariantSample.of(ItemCensus.empty(), ItemCensus.empty(),
                EntityCensus.of("minecraft:chicken", 1), ItemCensus.empty(), 0L));
        engine.observe(InvariantSample.of(ItemCensus.of(344, 1), ItemCensus.empty(),
                EntityCensus.of("minecraft:chicken", 1), ItemCensus.empty(), 1L));
    }

    private static void caughtFishHolds() {
        InvariantEngine engine = InvariantEngine.itemConservation(RecipeBook.none(),
                DropBook.of(Collections.singletonList(
                        CauseDrop.death("minecraft:fish-hook", ItemCensus.of(349, 1)))));
        engine.observe(InvariantSample.of(ItemCensus.empty(), ItemCensus.empty(),
                EntityCensus.of("minecraft:fish-hook", 1), ItemCensus.empty(), 0L));
        engine.observe(InvariantSample.of(ItemCensus.of(349, 1), ItemCensus.empty(),
                EntityCensus.empty(), ItemCensus.empty(), 1L));
    }

    private static void timeGoingBackwardFails() {
        InvariantEngine engine = InvariantEngine.standard(RecipeBook.none(), DropBook.none());
        require(engine.names().equals(Arrays.asList(ItemConservation.NAME, EntitySpawn.NAME,
                BlockConservation.NAME, HealthConservation.NAME, DurabilityConservation.NAME,
                TimeMonotonic.NAME)), "standard names failed");
        engine.observe(InvariantSample.of(ItemCensus.empty(), ItemCensus.empty(),
                EntityCensus.empty(), ItemCensus.empty(), 3L));
        try {
            engine.observe(InvariantSample.of(ItemCensus.empty(), ItemCensus.empty(),
                    EntityCensus.empty(), ItemCensus.empty(), 2L));
            throw new AssertionError("expected time to fail closed");
        } catch (InvariantViolation violation) {
            require(TimeMonotonic.NAME.equals(violation.invariant())
                    && violation.getMessage().contains("time moved from 3 to 2"),
                    "time detail failed: " + violation.getMessage());
        }
    }

    private static void pigBreedingHolds() {
        InvariantEngine engine = InvariantEngine.standard(RecipeBook.none(), DropBook.none());
        engine.observe(living(EntityCensus.of("minecraft:pig", 2), 0L));
        engine.observe(living(EntityCensus.of("minecraft:pig", 3), 1L));
    }

    private static void lonePigSpawnFails() {
        InvariantEngine engine = InvariantEngine.standard(RecipeBook.none(), DropBook.none());
        engine.observe(living(EntityCensus.empty(), 0L));
        try {
            engine.observe(living(EntityCensus.of("minecraft:pig", 1), 1L));
            throw new AssertionError("expected lone pig spawn to fail closed");
        } catch (InvariantViolation violation) {
            require(EntitySpawn.NAME.equals(violation.invariant()),
                    "spawn detail failed: " + violation.getMessage());
        }
    }

    private static void grassSpreadHolds() {
        RecipeBook transforms = RecipeBook.of(Collections.singletonList(
                new ItemRecipe(ItemCensus.of(3, 1), ItemCensus.of(2, 1))));
        InvariantEngine engine = InvariantEngine.standard(RecipeBook.none(), DropBook.none(),
                transforms, DropBook.none(), FoodBook.none());
        engine.observe(InvariantSample.of(ItemCensus.empty(), ItemCensus.of(3, 1)));
        engine.observe(InvariantSample.of(ItemCensus.empty(), ItemCensus.of(2, 1)));
    }

    private static void peacefulRegenHolds() {
        FoodBook foods = FoodBook.of(Collections.singletonList(new FoodHeal(297, 5)));
        InvariantEngine engine = InvariantEngine.standard(RecipeBook.none(), DropBook.none(),
                RecipeBook.none(), DropBook.none(), foods);
        engine.observe(body(ItemCensus.of(297, 1), WearCensus.empty(), 10, true));
        engine.observe(body(ItemCensus.of(297, 1), WearCensus.empty(), 11, true));
        engine.observe(body(ItemCensus.empty(), WearCensus.empty(), 16, true));
    }

    private static void unearnedHealFails() {
        InvariantEngine engine = InvariantEngine.standard(RecipeBook.none(), DropBook.none());
        engine.observe(body(ItemCensus.empty(), WearCensus.empty(), 10, true));
        try {
            engine.observe(body(ItemCensus.empty(), WearCensus.empty(), 15, true));
            throw new AssertionError("expected unearned heal to fail closed");
        } catch (InvariantViolation violation) {
            require(HealthConservation.NAME.equals(violation.invariant())
                    && violation.getMessage().contains("health grew from 10 to 15"),
                    "heal detail failed: " + violation.getMessage());
        }
    }

    private static void wearHoldsAndRepairFails() {
        InvariantEngine engine = InvariantEngine.standard(RecipeBook.none(), DropBook.none());
        engine.observe(body(ItemCensus.empty(), WearCensus.empty().plus(267, 10, 1), 20, true));
        engine.observe(body(ItemCensus.empty(), WearCensus.empty().plus(267, 11, 1), 20, true));
        try {
            engine.observe(body(ItemCensus.empty(), WearCensus.empty().plus(267, 9, 1), 20, true));
            throw new AssertionError("expected repair to fail closed");
        } catch (InvariantViolation violation) {
            require(DurabilityConservation.NAME.equals(violation.invariant()),
                    "wear detail failed: " + violation.getMessage());
        }
    }

    private static void grassPigSpawnHolds() {
        InvariantEngine engine = InvariantEngine.standard(RecipeBook.none(), DropBook.none(),
                RecipeBook.none(), DropBook.none(), FoodBook.none(), grassPigs());
        engine.observe(scene(ItemCensus.of(2, 8), EntityCensus.empty(), 0L));
        engine.observe(scene(ItemCensus.of(2, 8), EntityCensus.of("minecraft:pig", 1), 1L));
    }

    private static void slimeSplitHolds() {
        InvariantEngine engine = InvariantEngine.standard(RecipeBook.none(), DropBook.none(),
                RecipeBook.none(), DropBook.none(), FoodBook.none(),
                SpawnBook.of(Collections.singletonList(
                        new SpawnRule("minecraft:slime", "minecraft:slime", 4))));
        engine.observe(living(EntityCensus.of("minecraft:slime", 1), 0L));
        engine.observe(living(EntityCensus.of("minecraft:slime", 4), 1L));
    }

    private static void caneGrowthHolds() {
        DropBook fluids = DropBook.of(Collections.singletonList(
                CauseDrop.presence("block:83", ItemCensus.of(83, 64))));
        InvariantEngine engine = InvariantEngine.standard(RecipeBook.none(), DropBook.none(),
                RecipeBook.none(), fluids, FoodBook.none());
        engine.observe(InvariantSample.of(ItemCensus.empty(), ItemCensus.of(83, 1)));
        engine.observe(InvariantSample.of(ItemCensus.empty(), ItemCensus.of(83, 2)));
    }

    private static void cobbleFromFluidsHolds() {
        DropBook fluids = DropBook.of(Collections.singletonList(
                CauseDrop.presence("block:8", ItemCensus.of(4, 64).plus(49, 64))));
        InvariantEngine engine = InvariantEngine.standard(RecipeBook.none(), DropBook.none(),
                RecipeBook.none(), fluids, FoodBook.none());
        engine.observe(InvariantSample.of(ItemCensus.empty(), ItemCensus.of(8, 1).plus(10, 1)));
        engine.observe(InvariantSample.of(ItemCensus.empty(),
                ItemCensus.of(8, 1).plus(10, 1).plus(4, 1)));
    }

    private static void saplingBecomesTreeHolds() {
        DropBook fluids = DropBook.of(Collections.singletonList(
                CauseDrop.death("block:6", ItemCensus.of(17, 64).plus(18, 64))));
        InvariantEngine engine = InvariantEngine.standard(RecipeBook.none(), DropBook.none(),
                RecipeBook.none(), fluids, FoodBook.none());
        engine.observe(InvariantSample.of(ItemCensus.empty(), ItemCensus.of(6, 1)));
        engine.observe(InvariantSample.of(ItemCensus.empty(), ItemCensus.of(17, 6).plus(18, 20)));
    }

    private static void cakeBiteHolds() {
        FoodBook foods = FoodBook.of(Collections.singletonList(new FoodHeal(92, 3)));
        InvariantEngine engine = InvariantEngine.standard(RecipeBook.none(), DropBook.none(),
                RecipeBook.none(), DropBook.none(), foods);
        engine.observe(bite(ItemCensus.of(92, 1), 10));
        engine.observe(bite(ItemCensus.of(92, 1), 13));
    }

    private static void fallingSandHolds() {
        SpawnBook spawns = SpawnBook.of(Collections.singletonList(
                new SpawnRule("block:12", "minecraft:falling-block", 16)));
        DropBook fluids = DropBook.of(Collections.singletonList(
                CauseDrop.death("minecraft:falling-block", ItemCensus.of(12, 64).plus(13, 64))));
        InvariantEngine engine = InvariantEngine.standard(RecipeBook.none(), DropBook.none(),
                RecipeBook.none(), fluids, FoodBook.none(), spawns);
        engine.observe(scene(ItemCensus.of(12, 1), EntityCensus.empty(), 0L));
        engine.observe(scene(ItemCensus.empty(), EntityCensus.of("minecraft:falling-block", 1), 1L));
        engine.observe(scene(ItemCensus.of(12, 1), EntityCensus.empty(), 2L));
    }

    private static void arrowFromBowHolds() {
        InvariantEngine engine = InvariantEngine.standard(RecipeBook.none(), DropBook.none(),
                RecipeBook.none(), DropBook.none(), FoodBook.none(),
                SpawnBook.of(Collections.singletonList(new SpawnRule("item:262", "minecraft:arrow", 8))));
        engine.observe(pack(ItemCensus.of(262, 1), ItemCensus.empty(), EntityCensus.empty(), 0L));
        engine.observe(pack(ItemCensus.empty(), ItemCensus.empty(),
                EntityCensus.of("minecraft:arrow", 1), 1L));
    }

    private static InvariantSample pack(ItemCensus items, ItemCensus blocks, EntityCensus entities,
            long time) {
        return InvariantSample.of(items, blocks, entities, ItemCensus.empty(), time);
    }

    private static SpawnBook grassPigs() {
        return SpawnBook.of(Collections.singletonList(new SpawnRule("block:2", "minecraft:pig", 4)));
    }

    private static InvariantSample scene(ItemCensus blocks, EntityCensus entities, long time) {
        return InvariantSample.of(ItemCensus.empty(), blocks, entities, ItemCensus.empty(), time);
    }

    private static InvariantSample bite(ItemCensus blocks, int health) {
        return InvariantSample.of(ItemCensus.empty(), blocks, EntityCensus.empty(),
                ItemCensus.empty(), ItemCensus.empty(), EntityCensus.empty(), WearCensus.empty(),
                0L, health, true);
    }

    private static InvariantSample living(EntityCensus entities, long time) {
        return InvariantSample.of(ItemCensus.empty(), ItemCensus.empty(), entities,
                ItemCensus.empty(), time);
    }

    private static InvariantSample body(ItemCensus items, WearCensus wear, int health,
            boolean peaceful) {
        return InvariantSample.of(items, ItemCensus.empty(), EntityCensus.empty(),
                ItemCensus.empty(), ItemCensus.empty(), EntityCensus.empty(), wear, 0L, health,
                peaceful);
    }

    private static RecipeBook stoneDrop() {
        return RecipeBook.of(Collections.singletonList(
                new ItemRecipe(ItemCensus.of(1, 1), ItemCensus.of(4, 1))));
    }

    private static RecipeBook planks() {
        return RecipeBook.of(Collections.singletonList(
                new ItemRecipe(ItemCensus.of(17, 1), ItemCensus.of(5, 4))));
    }

    private static void uiNodesFeedTheCensus() {
        ItemCensus census = ItemCensus.fromNodes(Arrays.asList(
                new GameUiNode(GameUiNode.SCREEN, GameUiNode.INVENTORY, -1, -1, 0),
                new GameUiNode(GameUiNode.SLOT, "0", 0, 265, 4),
                new GameUiNode(GameUiNode.SLOT, "1", 1, 265, 6),
                new GameUiNode(GameUiNode.SLOT, "2", 2, -1, 0)));
        require(census.equals(ItemCensus.of(265, 10)) && census.total() == 10 && census.count(50) == 0,
                "UI census aggregation failed");
        InvariantEngine engine = InvariantEngine.itemConservation();
        engine.observe(census);
        engine.observe(ItemCensus.fromNodes(Arrays.asList(
                new GameUiNode(GameUiNode.SLOT, "0", 0, 265, 3),
                new GameUiNode(GameUiNode.SLOT, "8", 8, 265, 7))));
    }

    private static void engineRejectsEmptyConfiguration() {
        try {
            new InvariantEngine(Collections.<Invariant>emptyList());
            throw new AssertionError("expected empty engine to fail");
        } catch (IllegalArgumentException expected) {
            require(expected.getMessage().contains("invariants"), "empty engine message failed");
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
