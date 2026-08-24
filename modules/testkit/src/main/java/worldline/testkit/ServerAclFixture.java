package worldline.testkit;

import java.util.Locale;
import java.util.Objects;

/** Reusable evidence boundary for dedicated-server command and session ACLs. */
public final class ServerAclFixture {
    private ServerAclFixture() { }

    public static Evidence observe(String regularRecord, long regularTime, long regularTarget,
            String operatorRecord, long operatorTime, long operatorTarget,
            String revokedRecord, long revokedTime, long revokedTarget,
            String kickDisconnect, boolean kickReconnect,
            String banDisconnect, String banRejection, boolean pardonReconnect) {
        require(denied(regularRecord, regularTarget) && outside(regularTime, regularTarget),
                "regular player command was not denied");
        require(accepted(operatorRecord, operatorTarget)
                && within(operatorTime, operatorTarget, 1000L),
                "operator command was not accepted");
        require(denied(revokedRecord, revokedTarget) && outside(revokedTime, revokedTarget)
                && within(revokedTime, operatorTarget, 2000L),
                "deoperator command authority was not revoked");
        require(disconnect(kickDisconnect) && kickReconnect,
                "kick did not disconnect without banning");
        require(disconnect(banDisconnect) && contains(banRejection, "login rejected")
                && contains(banRejection, "banned") && pardonReconnect,
                "ban or pardon session boundary drifted");
        return new Evidence(true, true, true, true, kickReconnect,
                true, true, pardonReconnect);
    }

    private static boolean denied(String value, long target) {
        return contains(value, "tried command: time set " + target);
    }
    private static boolean accepted(String value, long target) {
        return contains(value, "issued server command: time set " + target);
    }
    private static boolean disconnect(String value) {
        return contains(value, "server disconnected");
    }
    private static boolean within(long value, long target, long allowance) {
        return value >= target && value < target + allowance;
    }
    private static boolean outside(long value, long target) {
        return value < target || value >= target + 1000L;
    }
    private static boolean contains(String value, String token) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(token);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    public static final class Evidence {
        private final boolean regularDenied, operatorAllowed, deoperatorDenied;
        private final boolean kickDisconnected, kickReconnect;
        private final boolean banDisconnected, banRejected, pardonReconnect;
        Evidence(boolean regularDenied, boolean operatorAllowed, boolean deoperatorDenied,
                boolean kickDisconnected, boolean kickReconnect, boolean banDisconnected,
                boolean banRejected, boolean pardonReconnect) {
            this.regularDenied = regularDenied; this.operatorAllowed = operatorAllowed;
            this.deoperatorDenied = deoperatorDenied; this.kickDisconnected = kickDisconnected;
            this.kickReconnect = kickReconnect; this.banDisconnected = banDisconnected;
            this.banRejected = banRejected; this.pardonReconnect = pardonReconnect;
        }
        public boolean regularDenied() { return regularDenied; }
        public boolean operatorAllowed() { return operatorAllowed; }
        public boolean deoperatorDenied() { return deoperatorDenied; }
        public boolean kickDisconnected() { return kickDisconnected; }
        public boolean kickReconnect() { return kickReconnect; }
        public boolean banDisconnected() { return banDisconnected; }
        public boolean banRejected() { return banRejected; }
        public boolean pardonReconnect() { return pardonReconnect; }
        @Override public boolean equals(Object other) {
            if (!(other instanceof Evidence)) return false;
            Evidence value = (Evidence) other;
            return regularDenied == value.regularDenied && operatorAllowed == value.operatorAllowed
                    && deoperatorDenied == value.deoperatorDenied
                    && kickDisconnected == value.kickDisconnected
                    && kickReconnect == value.kickReconnect
                    && banDisconnected == value.banDisconnected && banRejected == value.banRejected
                    && pardonReconnect == value.pardonReconnect;
        }
        @Override public int hashCode() {
            return Objects.hash(regularDenied, operatorAllowed, deoperatorDenied,
                    kickDisconnected, kickReconnect, banDisconnected, banRejected, pardonReconnect);
        }
    }
}
