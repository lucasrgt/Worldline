package worldline.stationapi;

/** Session and request mismatches must be rejected even when the native state is otherwise valid. */
public final class StationApiControlProtocolTest {
    private StationApiControlProtocolTest() { }
    public static void verify() {
        StationApiSnapshot state = StationApiSnapshot.parse("STATE session=s01 username=WlSta01 tick=0 time=1"
                + " entity=1 health=20 selected=0 x=0.0 y=70.0 z=0.0", "STATE", "s01");
        new StationApiControlReply("RESULT session=s01 request=1 value=ok", "s01", 1, state);
        for (String reply : new String[] {"RESULT session=s02 request=1 value=ok",
                "RESULT session=s01 request=2 value=ok", "RESULT session=s01 request=0 value=ok",
                "RESULT session=s01 request=1 value=ok extra=1", "RESULT session=s01 request=1 value="}) {
            try { new StationApiControlReply(reply, "s01", 1, state); }
            catch (IllegalStateException expected) { continue; }
            throw new AssertionError("invalid native acknowledgement accepted");
        }
    }
}
