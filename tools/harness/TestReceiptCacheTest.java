import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Proves that unit-suite PASS evidence is reusable only while byte-exact. */
public final class TestReceiptCacheTest {
    public static void main(String[] arguments) {
        Path root = Path.of("").toAbsolutePath().normalize();
        Path temporary = null;
        try {
            temporary = Files.createTempDirectory("worldline-test-receipts-");
            TestReceiptCache cache = new TestReceiptCache(root, temporary);
            String fingerprint = cache.fingerprint("ExampleSuite", "compiled-tests-v1");
            cache.passed("ExampleSuite", fingerprint, "ExampleSuite passed\n");
            require(cache.restore("ExampleSuite", fingerprint), "intact proof was not restored");
            Path evidence = temporary.resolve("ExampleSuite").resolve(fingerprint + ".log");
            Files.writeString(evidence, "altered\n", StandardCharsets.UTF_8);
            require(!cache.restore("ExampleSuite", fingerprint), "altered evidence was accepted");
            String changed = cache.fingerprint("ExampleSuite", "compiled-tests-v2");
            require(!cache.restore("ExampleSuite", changed), "changed input restored stale proof");
            require(!TestReceiptCache.sampledForRecheck("seed", "suite", fingerprint, 0)
                            && TestReceiptCache.sampledForRecheck("seed", "suite", fingerprint, 100),
                    "nightly recheck sampling boundaries drifted");
            System.out.println("  test suite receipt cache self-test: passed");
        } catch (Exception error) {
            System.err.println("test suite receipt cache self-test failed: " + error.getMessage());
            System.exit(1);
        } finally {
            if (temporary != null) try { SafeTreeDelete.delete(temporary); }
            catch (Exception ignored) { }
        }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
