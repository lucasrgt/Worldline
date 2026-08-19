package worldline.b173server;

import java.io.DataOutputStream;
import java.io.IOException;

/** Keepalive flying packets while a bounded inbound wait is armed. */
final class B173Pulse {
    private B173Pulse() {}
    static Thread start(DataOutputStream output) {
        Thread thread = new Thread(() -> { try { while (!Thread.currentThread().isInterrupted()) {
            synchronized (output) { output.writeByte(10); output.writeBoolean(false); output.flush(); }
            Thread.sleep(1000L);
        } } catch (IOException ignored) { } catch (InterruptedException stopped) {
            Thread.currentThread().interrupt(); } }, "worldline-b173-pulse");
        thread.setDaemon(true); thread.start(); return thread;
    }
}
