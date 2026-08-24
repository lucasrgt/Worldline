package worldline.b173server;

import java.time.Duration;

/** Protocol-14 observer for administrative disconnect and login-rejection boundaries. */
public final class B173ServerAclAccess {
    private B173ServerAclAccess() { }

    public static String awaitDisconnect(B173WireClient client) {
        if (client == null) throw new IllegalArgumentException("null disconnect client");
        for (int count = 0; count < 16; count++) {
            try { client.awaitChat(); }
            catch (IllegalStateException error) {
                String message = messages(error);
                if (message.toLowerCase(java.util.Locale.ROOT).contains("server disconnected"))
                    return message;
                throw error;
            }
        }
        throw new IllegalStateException("administrative disconnect absent from bounded packet window");
    }

    public static String loginRejection(String host, int port, String username, Duration timeout) {
        B173WireClient probe = new B173WireClient(host, port, username, timeout);
        try {
            probe.connect();
            throw new IllegalStateException("administratively rejected login was accepted");
        } catch (IllegalStateException error) {
            String message = messages(error);
            if (!message.toLowerCase(java.util.Locale.ROOT).contains("login rejected")) throw error;
            return message;
        } finally { probe.close(); }
    }

    private static String messages(Throwable error) {
        StringBuilder result = new StringBuilder();
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause.getMessage() == null) continue;
            if (result.length() > 0) result.append(" | ");
            result.append(cause.getMessage());
        }
        return result.toString();
    }
}
