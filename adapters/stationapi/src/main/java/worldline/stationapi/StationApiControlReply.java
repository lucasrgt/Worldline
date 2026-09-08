package worldline.stationapi;

/** Strict command acknowledgement bound to one session, request and unchanged game tick. */
final class StationApiControlReply {
    final String value;
    final StationApiSnapshot snapshot;
    StationApiControlReply(String line, String session, long request, StationApiSnapshot snapshot) {
        String prefix = "RESULT session=" + session + " request=" + request + " value=";
        if (line == null || !line.startsWith(prefix) || line.length() == prefix.length()
                || line.substring(prefix.length()).matches(".*\\s.*"))
            throw new IllegalStateException("StationAPI command acknowledgement differs");
        this.value = line.substring(prefix.length()); this.snapshot = snapshot;
    }
}
