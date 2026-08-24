import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/** Validates and fingerprints the permanent Cartesian cross-lane seed matrix. */
record LaneMatrixContract(String id, int seedCount, int chunkCount, int caseCount,
                          List<String> seeds, List<String> chunks, String contract) {
    static LaneMatrixContract load(Path root) throws Exception {
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(
                root.resolve("smokes/lane-matrix.properties"), StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return parse(values);
    }

    static LaneMatrixContract parse(Properties values) throws Exception {
        require("1".equals(required(values, "schema")), "unsupported lane matrix schema");
        String id = required(values, "id");
        require(id.matches("[a-z0-9]+(?:-[a-z0-9]+)*"), "invalid lane matrix id");
        int seedCount = count(values, "seed.count");
        int chunkCount = count(values, "chunk.count");
        int caseCount = count(values, "case.count");
        require(seedCount >= 2 && chunkCount >= 2 && caseCount == seedCount * chunkCount,
                "lane matrix must be a complete multi-seed Cartesian product");
        List<String> seeds = rows(values, "seed", seedCount);
        List<String> chunks = rows(values, "chunk", chunkCount);
        Set<String> uniqueSeeds = new HashSet<>(seeds);
        Set<String> uniqueChunks = new HashSet<>(chunks);
        require(uniqueSeeds.size() == seedCount && uniqueChunks.size() == chunkCount,
                "lane matrix rows must be unique");
        for (String seed : seeds) Long.parseLong(seed);
        for (String chunk : chunks)
            require(chunk.matches("-?[0-9]+:-?[0-9]+"), "invalid lane matrix chunk: " + chunk);
        StringBuilder canonical = new StringBuilder("schema=1\nid=").append(id).append('\n');
        canonical.append("seed.count=").append(seedCount).append('\n');
        for (int index = 0; index < seeds.size(); index++)
            canonical.append("seed.").append(index + 1).append('=').append(seeds.get(index)).append('\n');
        canonical.append("chunk.count=").append(chunkCount).append('\n');
        for (int index = 0; index < chunks.size(); index++)
            canonical.append("chunk.").append(index + 1).append('=').append(chunks.get(index)).append('\n');
        canonical.append("case.count=").append(caseCount).append('\n');
        return new LaneMatrixContract(id, seedCount, chunkCount, caseCount,
                List.copyOf(seeds), List.copyOf(chunks), digest(canonical.toString()));
    }

    void validateDescriptor(Properties descriptor) {
        require(String.join(";", seeds).equals(required(descriptor, "matrix.seeds"))
                        && String.join(";", chunks).equals(required(descriptor, "matrix.chunks"))
                        && Integer.toString(seedCount).equals(required(descriptor, "lane.matrix.seed.count"))
                        && Integer.toString(chunkCount).equals(required(descriptor, "lane.matrix.chunk.count"))
                        && Integer.toString(caseCount).equals(required(descriptor, "lane.matrix.case.count")),
                "milestone descriptor diverged from permanent lane matrix");
    }

    void validateRecord(Properties values) {
        require(id.equals(required(values, "id"))
                        && Integer.toString(seedCount).equals(required(values, "matrix.seed.count"))
                        && Integer.toString(chunkCount).equals(required(values, "matrix.chunk.count"))
                        && Integer.toString(caseCount).equals(required(values, "matrix.case.count"))
                        && contract.equals(required(values, "matrix.contract.sha256")),
                "lane matrix record diverged from its permanent contract");
    }

    void write(Properties values) {
        values.setProperty("matrix.seed.count", Integer.toString(seedCount));
        values.setProperty("matrix.chunk.count", Integer.toString(chunkCount));
        values.setProperty("matrix.case.count", Integer.toString(caseCount));
        values.setProperty("matrix.contract.sha256", contract);
    }

    private static int count(Properties values, String key) {
        int result = Integer.parseInt(required(values, key));
        require(result > 0 && result <= 64, "invalid lane matrix count: " + key);
        return result;
    }

    private static List<String> rows(Properties values, String stem, int count) {
        List<String> result = new ArrayList<>();
        for (int index = 1; index <= count; index++)
            result.add(required(values, stem + "." + index));
        return result;
    }

    private static String required(Properties values, String key) {
        String value = values.getProperty(key, "").trim();
        require(!value.isEmpty(), "missing lane matrix field: " + key);
        return value;
    }

    private static String digest(String value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8)));
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
