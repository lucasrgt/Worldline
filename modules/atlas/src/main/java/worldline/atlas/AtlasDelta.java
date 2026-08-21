package worldline.atlas;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** CLI-only milestone delta. Not a Verify gate. */
public final class AtlasDelta {
    private static final Pattern MILESTONE = Pattern.compile("(?i)^m?(\\d+)$");
    private static final Pattern EXPERIMENT = Pattern.compile("^atlas\\.experiment\\.m(\\d+)(?:-.*)?$");

    private AtlasDelta() {}

    public static String since(AtlasStore store, String token) {
        int floor = parse(token);
        List<AtlasRecord> changed = new ArrayList<AtlasRecord>();
        for (AtlasRecord record : store.kind(AtlasKind.EXPERIMENT)) {
            Matcher matcher = EXPERIMENT.matcher(record.id());
            if (matcher.matches() && Integer.parseInt(matcher.group(1)) > floor) changed.add(record);
        }
        for (AtlasRecord record : store.kind(AtlasKind.HYPOTHESIS)) {
            if (mentions(record, floor)) changed.add(record);
        }
        StringBuilder text = new StringBuilder();
        text.append("since=").append(floor).append('\n');
        text.append("changed=").append(changed.size()).append('\n');
        for (AtlasRecord record : changed) {
            text.append(record.id()).append('=').append(record.status()).append('\n');
        }
        return text.toString();
    }

    private static boolean mentions(AtlasRecord record, int floor) {
        for (String ref : record.refs()) {
            Matcher matcher = EXPERIMENT.matcher(ref);
            if (matcher.matches() && Integer.parseInt(matcher.group(1)) > floor) return true;
        }
        return false;
    }

    static int parse(String token) {
        if (token == null) throw new IllegalArgumentException("since");
        Matcher matcher = MILESTONE.matcher(token.trim());
        if (!matcher.matches()) throw new IllegalArgumentException("since " + token);
        return Integer.parseInt(matcher.group(1));
    }
}
