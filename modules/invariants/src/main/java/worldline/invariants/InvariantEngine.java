package worldline.invariants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.InvariantMinecraftRuntime;
import worldline.api.InvariantSample;
import worldline.api.ItemCensus;
import worldline.api.ItemCensusObserver;

/** Runs a fixed set of invariants against each observation, in declaration order. */
public final class InvariantEngine implements ItemCensusObserver {
    private final List<Invariant> invariants;

    public InvariantEngine(List<Invariant> invariants) {
        if (invariants == null || invariants.isEmpty()) throw new IllegalArgumentException("invariants");
        List<Invariant> copy = new ArrayList<Invariant>();
        for (Invariant invariant : invariants) {
            if (invariant == null) throw new NullPointerException("invariant");
            copy.add(invariant);
        }
        this.invariants = Collections.unmodifiableList(copy);
    }

    public static InvariantEngine itemConservation() {
        return itemConservation(RecipeBook.none());
    }

    public static InvariantEngine itemConservation(RecipeBook recipes) {
        return itemConservation(recipes, DropBook.none());
    }

    public static InvariantEngine itemConservation(RecipeBook recipes, DropBook drops) {
        return new InvariantEngine(Collections.<Invariant>singletonList(
                new ItemConservation(recipes, drops)));
    }

    public static InvariantEngine standard(RecipeBook recipes, DropBook drops) {
        return standard(recipes, drops, RecipeBook.none(), DropBook.none(), FoodBook.none(),
                SpawnBook.none());
    }

    public static InvariantEngine standard(InvariantMinecraftRuntime runtime) {
        if (runtime == null) throw new NullPointerException("runtime");
        return standard(RecipeBook.of(runtime.recipes()), DropBook.of(runtime.drops()),
                RecipeBook.of(runtime.transforms()), DropBook.of(runtime.fluids()),
                FoodBook.of(runtime.foods()), SpawnBook.of(runtime.spawns()));
    }

    public static InvariantEngine standard(RecipeBook recipes, DropBook drops, RecipeBook transforms,
            DropBook fluids, FoodBook foods) {
        return standard(recipes, drops, transforms, fluids, foods, SpawnBook.none());
    }

    public static InvariantEngine standard(RecipeBook recipes, DropBook drops, RecipeBook transforms,
            DropBook fluids, FoodBook foods, SpawnBook spawns) {
        return new InvariantEngine(Arrays.asList(
                new ItemConservation(recipes, drops),
                new EntitySpawn(spawns),
                new BlockConservation(transforms, fluids),
                new HealthConservation(foods),
                new DurabilityConservation(),
                new TimeMonotonic()));
    }

    public void observe(ItemCensus census) {
        observe(InvariantSample.of(census));
    }

    @Override
    public void observe(ItemCensus items, ItemCensus blocks) {
        observe(InvariantSample.of(items, blocks));
    }

    @Override
    public void observe(InvariantSample sample) {
        observe(InvariantObservation.of(sample));
    }

    public void observe(InvariantObservation observation) {
        if (observation == null) throw new NullPointerException("observation");
        for (Invariant invariant : invariants) invariant.observe(observation);
    }

    public List<String> names() {
        List<String> names = new ArrayList<String>();
        for (Invariant invariant : invariants) names.add(invariant.name());
        return Collections.unmodifiableList(names);
    }
}
