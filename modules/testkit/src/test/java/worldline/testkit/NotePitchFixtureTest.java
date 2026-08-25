package worldline.testkit;

import java.util.ArrayList;
import java.util.List;
import worldline.api.BlockPosition;
import worldline.api.RemoteNoteEvent;

final class NotePitchFixtureTest {
    private NotePitchFixtureTest() { }

    static void execute() {
        NotePitchFixture.Evidence first = evidence(), second = evidence();
        require(first.equals(second) && first.hashCode() == second.hashCode()
                && first.clicks() == 26 && first.wrapPitch() == 0
                && first.retainedPitch() == 2 && first.instrument() == 1,
                "note pitch ladder evidence drifted");
        fail(() -> NotePitchFixture.cycle(shortLadder(), retained()));
        fail(() -> NotePitchFixture.cycle(ladderWithDrift(), retained()));
        fail(() -> NotePitchFixture.cycle(ladderWithWrongInstrument(), retained()));
        fail(() -> NotePitchFixture.cycle(fullLadder(), wrongRetainedPitch()));
    }

    private static NotePitchFixture.Evidence evidence() {
        return NotePitchFixture.cycle(fullLadder(), retained());
    }
    private static List<RemoteNoteEvent> fullLadder() {
        List<RemoteNoteEvent> ladder = new ArrayList<RemoteNoteEvent>();
        for (int index = 0; index <= NotePitchFixture.PITCHES; index++)
            ladder.add(event((index + 1) % NotePitchFixture.PITCHES, 1));
        return ladder;
    }
    private static List<RemoteNoteEvent> shortLadder() {
        return new ArrayList<RemoteNoteEvent>(fullLadder().subList(0, NotePitchFixture.PITCHES));
    }
    private static List<RemoteNoteEvent> ladderWithDrift() {
        List<RemoteNoteEvent> ladder = new ArrayList<RemoteNoteEvent>();
        for (int index = 0; index <= NotePitchFixture.PITCHES; index++)
            ladder.add(event(index % NotePitchFixture.PITCHES, 1));
        return ladder;
    }
    private static List<RemoteNoteEvent> ladderWithWrongInstrument() {
        List<RemoteNoteEvent> ladder = new ArrayList<RemoteNoteEvent>();
        for (int index = 0; index <= NotePitchFixture.PITCHES; index++)
            ladder.add(event((index + 1) % NotePitchFixture.PITCHES, index == 3 ? 2 : 1));
        return ladder;
    }
    private static RemoteNoteEvent retained() { return event(2, 1); }
    private static RemoteNoteEvent wrongRetainedPitch() { return event(1, 1); }
    private static RemoteNoteEvent event(int pitch, int instrument) {
        return new RemoteNoteEvent(54, new BlockPosition(4, 89, 4), instrument, pitch);
    }
    private static void fail(Runnable action) {
        try { action.run(); throw new AssertionError("invalid note ladder evidence accepted"); }
        catch (RuntimeException expected) { }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
