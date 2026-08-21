package worldline.api;

/** Signals a rejected click only after its authoritative full/cursor resync completes. */
public final class RemoteTransactionRejectedException extends IllegalStateException {
    private static final long serialVersionUID = 1L;
    private final transient RemoteRejectedTransaction recovery;

    public RemoteTransactionRejectedException(RemoteRejectedTransaction recovery) {
        super("remote personal transaction was rejected and reconciled");
        if (recovery == null) throw new IllegalArgumentException("null rejected transaction recovery");
        this.recovery = recovery;
    }

    public RemoteRejectedTransaction recovery() { return recovery; }
}
