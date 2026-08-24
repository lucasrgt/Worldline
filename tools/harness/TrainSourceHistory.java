import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

/** Preserves portable source-digest ancestry across repeated train reseals. */
final class TrainSourceHistory {
    private TrainSourceHistory() { }

    static void write(Properties predecessor, Properties target, String stem, String relative) {
        String priorStem = find(predecessor, relative);
        Set<String> ancestors = new LinkedHashSet<>();
        if (priorStem != null) {
            add(ancestors, predecessor.getProperty(priorStem + "prior_sha256"));
            add(ancestors, predecessor.getProperty(priorStem + "current_sha256"));
            int count = integer(predecessor.getProperty(priorStem + "ancestor.count", "0"));
            for (int index = 0; index < count; index++)
                add(ancestors, predecessor.getProperty(priorStem + "ancestor." + index + ".sha256"));
        }
        target.setProperty(stem + "ancestor.count", Integer.toString(ancestors.size()));
        int index = 0;
        for (String digest : ancestors)
            target.setProperty(stem + "ancestor." + index++ + ".sha256", digest);
    }

    static boolean connects(Properties lock, String stem, String digest) {
        int count = integer(lock.getProperty(stem + "ancestor.count", "0"));
        for (int index = 0; index < count; index++)
            if (digest.equals(lock.getProperty(stem + "ancestor." + index + ".sha256"))) return true;
        return false;
    }

    private static String find(Properties values, String relative) {
        int count = integer(values.getProperty("source.count", "0"));
        for (int index = 0; index < count; index++) {
            String stem = "source." + index + ".";
            if (relative.equals(values.getProperty(stem + "path"))) return stem;
        }
        return null;
    }

    private static void add(Set<String> values, String digest) {
        if (digest != null && (digest.equals("added") || digest.equals("removed")
                || digest.matches("[0-9a-f]{64}"))) values.add(digest);
    }

    private static int integer(String value) {
        try { int parsed = Integer.parseInt(value); return Math.max(0, parsed); }
        catch (NumberFormatException error) { throw new IllegalStateException("invalid train source history"); }
    }
}
