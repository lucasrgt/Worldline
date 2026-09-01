/** PID-reuse-safe identity of the canonical official-runtime lease owner. */
record RuntimeLeaseIdentity(String pid, String started) {
    static RuntimeLeaseIdentity environment() {
        String pid = required("WORLDLINE_RUNTIME_LEASE");
        String started = required("WORLDLINE_RUNTIME_LEASE_STARTED");
        require(pid.matches("[0-9]+") && started.matches("[0-9]+"),
                "invalid canonical runtime lease identity");
        ProcessHandle owner = ProcessHandle.of(Long.parseLong(pid)).orElseThrow();
        long actual = owner.info().startInstant().orElseThrow().toEpochMilli();
        require(owner.isAlive() && actual == Long.parseLong(started),
                "canonical runtime lease PID/start identity is not active");
        return new RuntimeLeaseIdentity(pid, started);
    }

    private static String required(String name) {
        String value = System.getenv(name);
        require(value != null && !value.isBlank(), "missing " + name); return value;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
}
