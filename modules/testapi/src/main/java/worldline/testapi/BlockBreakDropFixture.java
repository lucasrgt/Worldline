package worldline.testapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import worldline.api.RemoteItemStack;

/** Validates state transitions and exact-or-bounded drops for a gameplay break. */
public final class BlockBreakDropFixture {
    private static final Comparator<BlockCellTransition> TRANSITION_ORDER = Comparator
            .comparingInt((BlockCellTransition value) -> value.position().x())
            .thenComparingInt(value -> value.position().y())
            .thenComparingInt(value -> value.position().z())
            .thenComparingInt(value -> value.before().legacyId())
            .thenComparingInt(value -> value.before().metadata())
            .thenComparingInt(value -> value.after().legacyId())
            .thenComparingInt(value -> value.after().metadata());
    private static final Comparator<RemoteItemStack> DROP_ORDER = Comparator
            .comparingInt(RemoteItemStack::legacyId)
            .thenComparingInt(RemoteItemStack::damage)
            .thenComparingInt(RemoteItemStack::count);

    private BlockBreakDropFixture() { }

    public static BlockBreakDropEvidence execute(String subject, String archetype,
            boolean singular, int toolItemId, List<BlockCellTransition> expected,
            List<BlockCellTransition> observed, BlockLifecycleDropMatrix dropMatrix,
            List<RemoteItemStack> observedDrops) {
        return execute(new BlockConformanceProfile(subject,
                Collections.singletonList(archetype), singular, Collections.emptyMap()),
                toolItemId,
                expected, observed, dropMatrix, observedDrops);
    }

    public static BlockBreakDropEvidence execute(BlockConformanceProfile profile,
            int toolItemId, List<BlockCellTransition> expected,
            List<BlockCellTransition> observed, BlockLifecycleDropMatrix dropMatrix,
            List<RemoteItemStack> observedDrops) {
        Objects.requireNonNull(profile, "profile");
        // Zero is the canonical public representation for a bare-hand break.
        if (toolItemId < 0) throw new IllegalArgumentException("invalid break tool");
        List<BlockCellTransition> canonical = transitions(expected, "expected");
        require(canonical.equals(transitions(observed, "observed")),
                "break transitions drifted");
        Objects.requireNonNull(dropMatrix, "dropMatrix");
        List<RemoteItemStack> drops = drops(observedDrops);
        require(dropMatrix.accepts(drops), "drop matrix drifted: expected="
                + dropMatrix.description() + ",actual=" + drops);
        BlockConformanceTemplate template = new BlockConformanceTemplate(
                "drop-matrix", ConformanceLayer.ARCHETYPE);
        ConformanceLayer layer = profile.layer(template);
        return new BlockBreakDropEvidence(profile, layer, toolItemId,
                canonical, drops, dropMatrix.canonical());
    }

    private static List<BlockCellTransition> transitions(
            List<BlockCellTransition> source, String label) {
        if (source == null || source.isEmpty())
            throw new IllegalArgumentException(label + " transitions are empty");
        List<BlockCellTransition> result = new ArrayList<BlockCellTransition>(source);
        Set<BlockCellTransition> unique = new HashSet<BlockCellTransition>();
        for (BlockCellTransition value : result)
            if (value == null || !unique.add(value))
                throw new IllegalArgumentException(label + " transitions contain a duplicate");
        result.sort(TRANSITION_ORDER);
        return Collections.unmodifiableList(result);
    }

    private static List<RemoteItemStack> drops(List<RemoteItemStack> source) {
        if (source == null || source.stream().anyMatch(Objects::isNull))
            throw new IllegalArgumentException("invalid observed drops");
        List<RemoteItemStack> result = new ArrayList<RemoteItemStack>(source);
        result.sort(DROP_ORDER);
        return Collections.unmodifiableList(result);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
