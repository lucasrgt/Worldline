package worldline.smoke.b173lifecycleprovider;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import worldline.api.RemoteItemStack;
import worldline.testkit.BlockLifecycleScenario;

/** Renders the exact public scenario contract independently of runtime artifacts. */
final class B173LifecycleEvidenceOracle {
    private B173LifecycleEvidenceOracle() { }

    static void verify(List<BlockLifecycleScenario> rows, Map<String, String> evidence) {
        for (BlockLifecycleScenario row : rows) {
            if (!expected(row).equals(evidence.get(row.id()))) {
                throw new IllegalStateException(row.id() + " evidence drift");
            }
        }
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
        StringBuilder drops = new StringBuilder();
        for (RemoteItemStack item : row.expectedDrops()) {
            if (drops.length() > 0) drops.append(',');
            drops.append(item.legacyId()).append(':').append(item.count())
                    .append(':').append(item.damage());
        }
        return "schema=worldline.block-lifecycle-evidence.v1\nscenario=" + row.id()
                + "\nsubject=" + row.subject()
                + "\nclaim.gameplay-placement=" + claim + "gameplay-placement|"
                + row.placement().layer() + "\nclaim.save-reload=" + claim + "save-reload|"
                + row.persistence().layer() + "\nclaim.break-transition=" + claim
                + "break-transition|" + row.transition().layer() + "\nclaim.drop-matrix="
                + claim + "drop-matrix|" + row.drops().layer()
                + "\nsupport=4:71:4:1:0\ntarget=4:72:4\nplaced="
                + row.placedState().legacyId() + ":" + row.placedState().metadata()
                + "\ndrops=" + drops + "\nreload=FRESH_LOGIN\n";
    }

    private static String shortLayer(String value) { return value.substring(0, 1); }
}
