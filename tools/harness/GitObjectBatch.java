import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

/** Bounded exact reads from one persistent Git object-query process. */
final class GitObjectBatch {
    private GitObjectBatch() { }

    static List<String> read(Path root, List<String> objects, boolean missingAllowed)
            throws Exception {
        Process process = new ProcessBuilder("git", "cat-file", "--batch").directory(root.toFile())
                .redirectError(ProcessBuilder.Redirect.DISCARD).start();
        List<String> values = new java.util.ArrayList<>();
        try {
            try (OutputStream input = process.getOutputStream(); InputStream output = process.getInputStream()) {
                for (String object : objects) {
                    input.write((object + "\n").getBytes(StandardCharsets.UTF_8)); input.flush();
                    String headerText = line(output);
                    if (missingAllowed && headerText.equals(object + " missing")) {
                        values.add(null); continue;
                    }
                    String[] header = headerText.split(" ");
                    require(header.length == 3 && header[0].matches("[0-9a-f]{40}|[0-9a-f]{64}")
                            && header[1].equals("blob") && header[2].matches("[0-9]+"),
                            "invalid git object header: " + headerText.replace("\r", "<CR>"));
                    int size = Integer.parseInt(header[2]);
                    require(size > 0 && size <= 16 * 1024 * 1024, "git object is oversized");
                    byte[] bytes = output.readNBytes(size);
                    require(bytes.length == size && output.read() == '\n', "truncated git object");
                    values.add(new String(bytes, StandardCharsets.UTF_8));
                }
            }
            require(process.waitFor(30, java.util.concurrent.TimeUnit.SECONDS)
                    && process.exitValue() == 0, "git object batch failed");
            return java.util.Collections.unmodifiableList(values);
        } finally { if (process.isAlive()) process.destroyForcibly(); }
    }

    static void selfTest(Path root) throws Exception {
        String missing = "HEAD:.worldline-git-object-batch-missing";
        List<String> values = read(root,
                List.of("HEAD:smokes/train-reconciliation.lock", missing), true);
        require(values.size() == 2 && values.get(0).contains("schema=1") && values.get(1) == null,
                "git object batch result drifted");
        try { read(root, List.of(missing), false);
            throw new IllegalStateException("missing git object was accepted");
        } catch (IllegalStateException expected) { }
    }

    private static String line(InputStream input) throws Exception {
        ByteArrayOutputStream value = new ByteArrayOutputStream(); int next;
        while ((next = input.read()) != '\n') {
            require(next >= 0 && value.size() < 512, "invalid git object header");
            value.write(next);
        }
        return value.toString(StandardCharsets.UTF_8);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
