package worldline.cli;

import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;
import worldline.atlas.AtlasRecord;
import worldline.atlas.AtlasStore;

/** Discovers an external project and reports its canonical Atlas contribution. */
final class AtlasExtensionCommand {
    private AtlasExtensionCommand() { }

    static int run(String projectRoot, PrintStream output, PrintStream error) {
        try {
            AtlasStore store = AtlasStore.standard(Paths.get("."), Paths.get(projectRoot));
            Set<String> ids = new LinkedHashSet<String>();
            int records = 0;
            for (AtlasRecord record : store.records()) {
                String id = control(record.control(), "extension");
                if (!id.isEmpty()) { ids.add(id); records++; }
            }
            output.print("WORLDLINE_ATLAS_EXTENSIONS=PASS\n");
            output.print("extensions=" + ids.size() + "\n");
            output.print("extension_records=" + records + "\n");
            for (String id : ids) output.print("extension=" + id + "\n");
            output.print("sha256=" + store.sha256() + "\n");
            return 0;
        } catch (RuntimeException failure) {
            error.println("worldline command failed: " + failure.getMessage());
            return 1;
        }
    }

    private static String control(String value, String key) {
        for (String item : value.split(";", -1)) {
            if (item.startsWith(key + "=")) return item.substring(key.length() + 1);
        }
        return "";
    }
}
