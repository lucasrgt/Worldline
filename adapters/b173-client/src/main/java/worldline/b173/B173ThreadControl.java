package worldline.b173;

/** Captures and terminates the one vanilla thread created by headless boot. */
final class B173ThreadControl {
    private Thread timer;

    void capture() {
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (thread.isAlive() && thread.isDaemon()
                    && "Timer hack thread".equals(thread.getName())) {
                timer = thread;
                return;
            }
        }
        throw new IllegalStateException("vanilla timer thread was not started");
    }

    boolean isAlive() { return timer != null && timer.isAlive(); }

    void stop() {
        if (timer == null) return;
        timer.interrupt();
        try { timer.join(1000L); }
        catch (InterruptedException error) { Thread.currentThread().interrupt(); }
    }
}
