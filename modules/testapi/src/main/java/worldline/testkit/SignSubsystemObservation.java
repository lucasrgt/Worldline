package worldline.testkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import worldline.api.BlockLifecycleDriver.ReloadBoundary;
import worldline.api.BlockState;
import worldline.api.MovementDisposition;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteSignText;

/** Immutable raw observation of both native Beta 1.7.3 sign block variants. */
public final class SignSubsystemObservation {
    private final List<Integer> standingMetadata;
    private final List<MovementDisposition> collisions;
    private final List<Integer> blockLight, skyLight;
    private final BlockState placedStanding, placedWall, directBrokenFrom, directBrokenTo;
    private final BlockState persistedStanding, persistedWall, tickStanding, tickWall;
    private final BlockState unsupportedStanding, unsupportedWall;
    private final BlockState finalStanding, finalWall;
    private final RemoteSignText standingText, wallText, persistedStandingText, persistedWallText;
    private final RemoteItemStack directDrop;
    private final int signCountBefore, signCountAfterFirstPlace, tickWindow, reloads;
    private final ReloadBoundary boundary;

    public SignSubsystemObservation(List<Integer> standingMetadata,
            BlockState placedStanding, BlockState placedWall,
            int signCountBefore, int signCountAfterFirstPlace,
            BlockState directBrokenFrom, BlockState directBrokenTo, RemoteItemStack directDrop,
            RemoteSignText standingText, RemoteSignText wallText,
            BlockState persistedStanding, BlockState persistedWall,
            RemoteSignText persistedStandingText, RemoteSignText persistedWallText,
            List<MovementDisposition> collisions, List<Integer> blockLight,
            List<Integer> skyLight, int tickWindow, BlockState tickStanding,
            BlockState tickWall, BlockState unsupportedStanding,
            BlockState unsupportedWall, BlockState finalStanding, BlockState finalWall,
            ReloadBoundary boundary, int reloads) {
        this.standingMetadata = integers(standingMetadata, 16, 0, 15, "metadata");
        this.collisions = copy(collisions, 2, "collisions");
        this.blockLight = integers(blockLight, 2, 0, 15, "block light");
        this.skyLight = integers(skyLight, 2, 0, 15, "sky light");
        if (signCountBefore < 1 || signCountBefore > 127 || signCountAfterFirstPlace < 0
                || signCountAfterFirstPlace > 127 || tickWindow < 1 || tickWindow > 24_000
                || reloads < 1 || reloads > 8) {
            throw new IllegalArgumentException("invalid sign observation bounds");
        }
        this.placedStanding = state(placedStanding, "placedStanding");
        this.placedWall = state(placedWall, "placedWall");
        this.directBrokenFrom = state(directBrokenFrom, "directBrokenFrom");
        this.directBrokenTo = state(directBrokenTo, "directBrokenTo");
        this.persistedStanding = state(persistedStanding, "persistedStanding");
        this.persistedWall = state(persistedWall, "persistedWall");
        this.tickStanding = state(tickStanding, "tickStanding");
        this.tickWall = state(tickWall, "tickWall");
        this.unsupportedStanding = state(unsupportedStanding, "unsupportedStanding");
        this.unsupportedWall = state(unsupportedWall, "unsupportedWall");
        this.finalStanding = state(finalStanding, "finalStanding");
        this.finalWall = state(finalWall, "finalWall");
        this.standingText = Objects.requireNonNull(standingText, "standingText");
        this.wallText = Objects.requireNonNull(wallText, "wallText");
        this.persistedStandingText = Objects.requireNonNull(
                persistedStandingText, "persistedStandingText");
        this.persistedWallText = Objects.requireNonNull(persistedWallText, "persistedWallText");
        this.directDrop = Objects.requireNonNull(directDrop, "directDrop");
        this.signCountBefore = signCountBefore;
        this.signCountAfterFirstPlace = signCountAfterFirstPlace;
        this.tickWindow = tickWindow;
        this.boundary = Objects.requireNonNull(boundary, "boundary");
        this.reloads = reloads;
    }

    public List<Integer> standingMetadata() { return standingMetadata; }
    public BlockState placedStanding() { return placedStanding; }
    public BlockState placedWall() { return placedWall; }
    public int signCountBefore() { return signCountBefore; }
    public int signCountAfterFirstPlace() { return signCountAfterFirstPlace; }
    public BlockState directBrokenFrom() { return directBrokenFrom; }
    public BlockState directBrokenTo() { return directBrokenTo; }
    public RemoteItemStack directDrop() { return directDrop; }
    public RemoteSignText standingText() { return standingText; }
    public RemoteSignText wallText() { return wallText; }
    public BlockState persistedStanding() { return persistedStanding; }
    public BlockState persistedWall() { return persistedWall; }
    public RemoteSignText persistedStandingText() { return persistedStandingText; }
    public RemoteSignText persistedWallText() { return persistedWallText; }
    public List<MovementDisposition> collisions() { return collisions; }
    public List<Integer> blockLight() { return blockLight; }
    public List<Integer> skyLight() { return skyLight; }
    public int tickWindow() { return tickWindow; }
    public BlockState tickStanding() { return tickStanding; }
    public BlockState tickWall() { return tickWall; }
    public BlockState unsupportedStanding() { return unsupportedStanding; }
    public BlockState unsupportedWall() { return unsupportedWall; }
    public BlockState finalStanding() { return finalStanding; }
    public BlockState finalWall() { return finalWall; }
    public ReloadBoundary boundary() { return boundary; }
    public int reloads() { return reloads; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof SignSubsystemObservation)) return false;
        SignSubsystemObservation value = (SignSubsystemObservation) other;
        return signCountBefore == value.signCountBefore
                && signCountAfterFirstPlace == value.signCountAfterFirstPlace
                && tickWindow == value.tickWindow && reloads == value.reloads
                && standingMetadata.equals(value.standingMetadata)
                && placedStanding.equals(value.placedStanding) && placedWall.equals(value.placedWall)
                && directBrokenFrom.equals(value.directBrokenFrom)
                && directBrokenTo.equals(value.directBrokenTo) && directDrop.equals(value.directDrop)
                && standingText.equals(value.standingText) && wallText.equals(value.wallText)
                && persistedStanding.equals(value.persistedStanding)
                && persistedWall.equals(value.persistedWall)
                && persistedStandingText.equals(value.persistedStandingText)
                && persistedWallText.equals(value.persistedWallText)
                && collisions.equals(value.collisions) && blockLight.equals(value.blockLight)
                && skyLight.equals(value.skyLight) && tickStanding.equals(value.tickStanding)
                && tickWall.equals(value.tickWall)
                && unsupportedStanding.equals(value.unsupportedStanding)
                && unsupportedWall.equals(value.unsupportedWall)
                && finalStanding.equals(value.finalStanding) && finalWall.equals(value.finalWall)
                && boundary == value.boundary;
    }

    @Override public int hashCode() {
        return Objects.hash(standingMetadata, placedStanding, placedWall, signCountBefore,
                signCountAfterFirstPlace, directBrokenFrom, directBrokenTo, directDrop,
                standingText, wallText, persistedStanding, persistedWall,
                persistedStandingText, persistedWallText, collisions, blockLight, skyLight,
                tickWindow, tickStanding, tickWall, unsupportedStanding, unsupportedWall,
                finalStanding, finalWall, boundary, reloads);
    }

    private static BlockState state(BlockState value, String role) {
        return Objects.requireNonNull(value, role);
    }
    private static <T> List<T> copy(List<T> values, int size, String role) {
        if (values == null || values.size() != size || values.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("invalid sign " + role);
        }
        return Collections.unmodifiableList(new ArrayList<T>(values));
    }
    private static List<Integer> integers(List<Integer> values, int size,
            int minimum, int maximum, String role) {
        List<Integer> result = copy(values, size, role);
        for (Integer value : result) if (value < minimum || value > maximum) {
            throw new IllegalArgumentException("invalid sign " + role + " value");
        }
        return result;
    }
}
