package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.api.BlockState;
import worldline.api.MovementDisposition;

/** Immutable raw observation of a caller-executed cake-serving lifecycle. */
public final class CakeServingObservation {
    private final List<BlockState> states;
    private final List<Integer> health, blockLight, skyLight;
    private final List<MovementDisposition> collisions;
    private final BlockState tickBefore, tickAfter, reloaded;
    private final BlockState supported, unsupported, persisted;
    private final int tickWindow, collisionLaneMilli, collisionTravelMilli, reloads;
    private final ReloadBoundary boundary;

    public CakeServingObservation(List<BlockState> states, List<Integer> health,
            List<MovementDisposition> collisions, int collisionLaneMilli,
            int collisionTravelMilli, List<Integer> blockLight, List<Integer> skyLight,
            int tickWindow, BlockState tickBefore, BlockState tickAfter,
            BlockState reloaded, BlockState supported, BlockState unsupported,
            BlockState persisted, ReloadBoundary boundary, int reloads) {
        this.states = copy(states, 7, "states");
        this.health = integers(health, 7, 0, 20, "health");
        this.collisions = copy(collisions, 6, "collisions");
        this.blockLight = integers(blockLight, 6, 0, 15, "blockLight");
        this.skyLight = integers(skyLight, 6, 0, 15, "skyLight");
        if (collisionLaneMilli < -2_000 || collisionLaneMilli > 2_000
                || collisionTravelMilli < 1 || collisionTravelMilli > 10_000
                || tickWindow < 1 || tickWindow > 24_000 || reloads < 1 || reloads > 8) {
            throw new IllegalArgumentException("invalid cake observation bounds");
        }
        this.collisionLaneMilli = collisionLaneMilli;
        this.collisionTravelMilli = collisionTravelMilli;
        this.tickWindow = tickWindow;
        this.tickBefore = Objects.requireNonNull(tickBefore, "tickBefore");
        this.tickAfter = Objects.requireNonNull(tickAfter, "tickAfter");
        this.reloaded = Objects.requireNonNull(reloaded, "reloaded");
        this.supported = Objects.requireNonNull(supported, "supported");
        this.unsupported = Objects.requireNonNull(unsupported, "unsupported");
        this.persisted = Objects.requireNonNull(persisted, "persisted");
        this.boundary = Objects.requireNonNull(boundary, "boundary");
        this.reloads = reloads;
    }

    public List<BlockState> states() { return states; }
    public List<Integer> health() { return health; }
    public List<MovementDisposition> collisions() { return collisions; }
    public int collisionLaneMilli() { return collisionLaneMilli; }
    public int collisionTravelMilli() { return collisionTravelMilli; }
    public List<Integer> blockLight() { return blockLight; }
    public List<Integer> skyLight() { return skyLight; }
    public int tickWindow() { return tickWindow; }
    public BlockState tickBefore() { return tickBefore; }
    public BlockState tickAfter() { return tickAfter; }
    public BlockState reloaded() { return reloaded; }
    public BlockState supported() { return supported; }
    public BlockState unsupported() { return unsupported; }
    public BlockState persisted() { return persisted; }
    public ReloadBoundary boundary() { return boundary; }
    public int reloads() { return reloads; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof CakeServingObservation)) return false;
        CakeServingObservation value = (CakeServingObservation) other;
        return tickWindow == value.tickWindow && collisionLaneMilli == value.collisionLaneMilli
                && collisionTravelMilli == value.collisionTravelMilli && reloads == value.reloads
                && states.equals(value.states) && health.equals(value.health)
                && collisions.equals(value.collisions) && blockLight.equals(value.blockLight)
                && skyLight.equals(value.skyLight) && tickBefore.equals(value.tickBefore)
                && tickAfter.equals(value.tickAfter) && reloaded.equals(value.reloaded)
                && supported.equals(value.supported) && unsupported.equals(value.unsupported)
                && persisted.equals(value.persisted) && boundary == value.boundary;
    }

    @Override public int hashCode() {
        return Objects.hash(states, health, collisions, collisionLaneMilli,
                collisionTravelMilli, blockLight, skyLight, tickWindow, tickBefore,
                tickAfter, reloaded, supported, unsupported, persisted, boundary, reloads);
    }

    private static <T> List<T> copy(List<T> values, int size, String role) {
        if (values == null || values.size() != size || values.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("invalid cake " + role);
        }
        return Collections.unmodifiableList(new ArrayList<T>(values));
    }

    private static List<Integer> integers(List<Integer> values, int size,
            int minimum, int maximum, String role) {
        List<Integer> copy = copy(values, size, role);
        for (Integer value : copy) if (value < minimum || value > maximum) {
            throw new IllegalArgumentException("invalid cake " + role + " value");
        }
        return copy;
    }
}
