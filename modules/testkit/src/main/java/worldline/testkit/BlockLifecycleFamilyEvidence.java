package worldline.testkit;
import worldline.testapi.BlockLifecycleScenario;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import worldline.api.BlockPosition;
import worldline.api.BlockState;

/** Canonical row oracle shared by independently signed lifecycle families. */
final class BlockLifecycleFamilyEvidence {
    private BlockLifecycleFamilyEvidence() { }

    static void verify(List<BlockLifecycleScenario> rows, Map<String, String> evidence) {
        for (BlockLifecycleScenario row : rows) if (!expected(row).equals(evidence.get(row.id())))
            throw new IllegalStateException(row.id() + " evidence drift");
    }

    static String layers(List<BlockLifecycleScenario> rows) {
        StringJoiner value = new StringJoiner("+");
        for (BlockLifecycleScenario row : rows) value.add(shortLayer(row.placement().layer().name())
                + "-" + shortLayer(row.persistence().layer().name())
                + "-" + shortLayer(row.transition().layer().name())
                + "-" + shortLayer(row.drops().layer().name()));
        return value.toString();
    }

    static String rowIds(List<BlockLifecycleScenario> rows) {
        StringJoiner value = new StringJoiner("+");
        for (BlockLifecycleScenario row : rows) value.add(row.id());
        return value.toString();
    }

    private static String expected(BlockLifecycleScenario row) {
        String claim = row.subject() + "#";
        String drops = row.dropMatrix().canonical();
        BlockPosition support = row.support(), target = row.target();
        BlockState supportState = row.supportState();
        String overhead = row.overheadState() == null ? "" : "\noverhead="
                + row.overhead().x() + ":" + row.overhead().y() + ":" + row.overhead().z()
                + ":" + row.overheadState().legacyId() + ":" + row.overheadState().metadata();
        String neighbor = row.neighbor() == null ? "" : "\nneighbor="
                + row.neighborPosition().x() + ":" + row.neighborPosition().y() + ":"
                + row.neighborPosition().z() + ":" + row.neighbor().state().legacyId()
                + ":" + row.neighbor().state().metadata();
        return "schema=worldline.block-lifecycle-evidence.v1\nscenario=" + row.id()
                + "\nsubject=" + row.subject()
                + "\nclaim.gameplay-placement=" + claim + "gameplay-placement|"
                + row.placement().layer() + "\nclaim.save-reload=" + claim + "save-reload|"
                + row.persistence().layer() + "\nclaim.break-transition=" + claim
                + "break-transition|" + row.transition().layer() + "\nclaim.drop-matrix="
                + claim + "drop-matrix|" + row.drops().layer() + "\nsupport="
                + support.x() + ":" + support.y() + ":" + support.z() + ":"
                + supportState.legacyId() + ":" + supportState.metadata() + overhead + neighbor
                + "\ntarget="
                + target.x() + ":" + target.y() + ":" + target.z() + "\nplaced="
                + row.placedState().legacyId() + ":" + row.placedState().metadata()
                + "\ndrops=" + drops + "\nreload=FRESH_LOGIN\n";
    }

    private static String shortLayer(String value) { return value.substring(0, 1); }
}
