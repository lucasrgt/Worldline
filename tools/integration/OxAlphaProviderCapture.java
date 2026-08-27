import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;

/** Preserves exact private stderr bytes while classifying provider evidence in streaming form. */
final class OxAlphaProviderCapture {
    private final OutputStream output;
    private final OxAlphaProviderLogMonitor monitor;
    private volatile IOException failure;
    private Thread thread;

    private OxAlphaProviderCapture(OutputStream output, String selectedModel) {
        this.output = output;
        this.monitor = new OxAlphaProviderLogMonitor(selectedModel);
    }

    static OxAlphaProviderCapture start(Process process, Path stderr, String selectedModel)
            throws IOException {
        OutputStream output = Files.newOutputStream(stderr, StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE);
        OxAlphaProviderCapture capture = new OxAlphaProviderCapture(output, selectedModel);
        try {
            capture.thread = Thread.ofPlatform().daemon().name("ox-alpha-provider-capture")
                    .start(() -> capture.copy(process));
            return capture;
        } catch (RuntimeException failure) {
            output.close();
            throw failure;
        }
    }

    String classification() {
        return monitor.classification();
    }

    void await() throws Exception {
        thread.join(TimeUnit.SECONDS.toMillis(10));
        require(!thread.isAlive(), "provider capture did not stop");
        if (failure != null) {
            throw failure;
        }
    }

    private void copy(Process process) {
        try (InputStream input = process.getErrorStream(); output) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) >= 0) {
                output.write(buffer, 0, count);
                output.flush();
                monitor.accept(buffer, count);
            }
            monitor.finish();
        } catch (IOException error) {
            failure = error;
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
