package worldline.api;

/** Immutable protocol-14 silent-client keep-alive timeout observation. */
public final class RemoteKeepAliveTimeout {
    private final int keepAlivePackets;
    private final long elapsedMillis;
    private final boolean streamClosed;

    public RemoteKeepAliveTimeout(int keepAlivePackets, long elapsedMillis, boolean streamClosed) {
        if (keepAlivePackets < 0 || elapsedMillis < 0L)
            throw new IllegalArgumentException("invalid keep-alive timeout");
        this.keepAlivePackets = keepAlivePackets;
        this.elapsedMillis = elapsedMillis;
        this.streamClosed = streamClosed;
    }

    public int keepAlivePackets() { return keepAlivePackets; }
    public long elapsedMillis() { return elapsedMillis; }
    public boolean streamClosed() { return streamClosed; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof RemoteKeepAliveTimeout)) return false;
        RemoteKeepAliveTimeout value = (RemoteKeepAliveTimeout) other;
        return keepAlivePackets == value.keepAlivePackets && elapsedMillis == value.elapsedMillis
                && streamClosed == value.streamClosed;
    }

    @Override public int hashCode() {
        int result = 31 * keepAlivePackets + Long.hashCode(elapsedMillis);
        return 31 * result + Boolean.hashCode(streamClosed);
    }
}
