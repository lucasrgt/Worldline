package worldline.testkit;

import java.util.Locale;
import java.util.Objects;

/** Equatable official evidence for whitelist and capacity login policy. */
public final class ServerEntryPolicyFixture {
    private ServerEntryPolicyFixture() { }

    public static Evidence observe(int maximumPlayers, int identities,
            String unlistedRejection, boolean listedAccepted,
            String overflowRejection, boolean whitelistDisabledAccepted) {
        require(maximumPlayers == 1 && identities == 3,
                "entry policy fixture topology drifted");
        require(rejected(unlistedRejection, "white-list"),
                "unlisted identity was not rejected by whitelist policy");
        require(listedAccepted, "listed identity was not admitted");
        require(rejected(overflowRejection, "server is full"),
                "listed overflow identity was not rejected by capacity");
        require(whitelistDisabledAccepted,
                "unlisted identity was not admitted with whitelist disabled");
        return new Evidence(maximumPlayers, identities, true, true, true, true);
    }

    public static void compare(Evidence expected, Evidence observed) {
        if (expected == null || !expected.equals(observed)) {
            throw new IllegalStateException("server entry policy evidence mismatch");
        }
    }

    private static boolean rejected(String value, String reason) {
        if (value == null) {
            return false;
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        return normalized.contains("login rejected") && normalized.contains(reason);
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new IllegalStateException(message);
        }
    }

    public static final class Evidence {
        private final int maximumPlayers;
        private final int identities;
        private final boolean unlistedRejected;
        private final boolean listedAccepted;
        private final boolean overflowRejected;
        private final boolean openAccepted;

        Evidence(int maximumPlayers, int identities, boolean unlistedRejected,
                boolean listedAccepted, boolean overflowRejected, boolean openAccepted) {
            this.maximumPlayers = maximumPlayers;
            this.identities = identities;
            this.unlistedRejected = unlistedRejected;
            this.listedAccepted = listedAccepted;
            this.overflowRejected = overflowRejected;
            this.openAccepted = openAccepted;
        }

        public int maximumPlayers() { return maximumPlayers; }
        public int identities() { return identities; }
        public boolean unlistedRejected() { return unlistedRejected; }
        public boolean listedAccepted() { return listedAccepted; }
        public boolean overflowRejected() { return overflowRejected; }
        public boolean openAccepted() { return openAccepted; }

        @Override public boolean equals(Object other) {
            if (!(other instanceof Evidence)) {
                return false;
            }
            Evidence value = (Evidence) other;
            return maximumPlayers == value.maximumPlayers && identities == value.identities
                    && unlistedRejected == value.unlistedRejected
                    && listedAccepted == value.listedAccepted
                    && overflowRejected == value.overflowRejected
                    && openAccepted == value.openAccepted;
        }

        @Override public int hashCode() {
            return Objects.hash(maximumPlayers, identities, unlistedRejected, listedAccepted,
                    overflowRejected, openAccepted);
        }

        @Override public String toString() {
            return "max=" + maximumPlayers + ",identities=" + identities
                    + ",unlisted=" + unlistedRejected + ",listed=" + listedAccepted
                    + ",overflow=" + overflowRejected + ",open=" + openAccepted;
        }
    }
}
