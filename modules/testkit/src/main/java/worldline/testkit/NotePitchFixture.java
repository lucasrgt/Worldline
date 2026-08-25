package worldline.testkit;

import java.util.List;
import java.util.Objects;
import worldline.api.BlockPosition;
import worldline.api.RemoteNoteEvent;

/**
 * Reusable bounded contract for the official note-block pitch ladder. One
 * activation click increments the tile pitch modulo twenty-five and then plays
 * the new pitch through Packet54, so twenty-five clicks traverse every pitch
 * once and end on the wrap back to zero. A twenty-sixth click replays pitch
 * one, leaving the tile at pitch one; the single click observed across a
 * save-and-restart reload must therefore replay pitch two, which distinguishes
 * a persisted tile from a fresh pitch-zero note.
 */
public final class NotePitchFixture {
    /** Official Beta 1.7.3 note blocks expose exactly twenty-five tunable pitches. */
    public static final int PITCHES = 25;
    private NotePitchFixture() { }

    /**
     * Validates one complete official tuning cycle: {@link #PITCHES} ascending
     * Packet54 pitches plus one confirmation click past the wrap, then the
     * retained click observed after a save-and-restart reload.
     */
    public static Evidence cycle(List<RemoteNoteEvent> ladder, RemoteNoteEvent retained) {
        if (ladder == null || ladder.size() != PITCHES + 1)
            throw new IllegalArgumentException("ladder must hold the full sequence plus one wrap");
        BlockPosition position = ladder.get(0).position();
        int instrument = ladder.get(0).instrument();
        require(instrument >= 0 && instrument <= 4, "instrument outside the vanilla set");
        for (int index = 0; index <= PITCHES; index++) {
            RemoteNoteEvent event = ladder.get(index);
            require(event.packetId() == 54, "official tuning emits Packet54");
            require(event.position().equals(position), "tuning left the note cell");
            require(event.instrument() == instrument, "instrument changed during the ladder");
            require(event.pitch() == (index + 1) % PITCHES,
                    "pitch ladder drifted at index " + index);
        }
        require(retained != null && retained.packetId() == 54,
                "reloaded click emitted no Packet54");
        require(retained.position().equals(position), "reload drifted from the note cell");
        require(retained.instrument() == instrument, "reload changed the instrument");
        require(retained.pitch() == 2, "reload lost the tuned pitch two");
        return new Evidence(PITCHES + 1, 0, 2, instrument);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    /** Equatable qualification evidence: counts and pitches only, never coordinates. */
    public static final class Evidence {
        private final int clicks, wrapPitch, retainedPitch, instrument;
        private Evidence(int clicks, int wrapPitch, int retainedPitch, int instrument) {
            this.clicks = clicks; this.wrapPitch = wrapPitch;
            this.retainedPitch = retainedPitch; this.instrument = instrument;
        }
        public int clicks() { return clicks; }
        public int wrapPitch() { return wrapPitch; }
        public int retainedPitch() { return retainedPitch; }
        public int instrument() { return instrument; }
        @Override public boolean equals(Object other) {
            if (!(other instanceof Evidence)) return false;
            Evidence value = (Evidence) other;
            return clicks == value.clicks && wrapPitch == value.wrapPitch
                    && retainedPitch == value.retainedPitch && instrument == value.instrument;
        }
        @Override public int hashCode() {
            return Objects.hash(clicks, wrapPitch, retainedPitch, instrument);
        }
    }
}
