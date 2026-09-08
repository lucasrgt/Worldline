import java.util.List;
import java.util.Locale;

record M791Result(long changedPixels, int maxDelta, long offWork, long onWork,
                  double workRatio, boolean reduced, boolean activated) {
    static M791Result evaluate(List<M791Session> sessions, List<M791VisualPair> pairs) {
        long changed = 0L, off = 0L, on = 0L;
        int delta = 0;
        boolean reduced = true, active = true;
        for (M791VisualPair pair : pairs) {
            changed += pair.changedPixels();
            delta = Math.max(delta, pair.maxDelta());
        }
        for (M791Session session : sessions) {
            off += session.off().submitted;
            on += session.on().submitted;
            reduced &= session.on().submitted < session.off().submitted;
            active &= session.off().culled == 0L && session.on().culled > 0L
                && session.on().slices > 0;
        }
        return new M791Result(changed, delta, off, on, (double) on / off, reduced, active);
    }

    boolean passes(double maximum) {
        return changedPixels == 0L && maxDelta == 0 && reduced && activated && workRatio <= maximum;
    }

    String summary() {
        return "visual.changed.pixels=" + changedPixels + ",visual.max.delta=" + maxDelta
            + ",work.submitted=" + offWork + "->" + onWork + ",work.ratio="
            + String.format(Locale.ROOT, "%.3f", workRatio)
            + ",work.reduced=" + reduced + ",occlusion.activated=" + activated;
    }
}
