package worldline.profiling;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Canonical checksum-protected Worldline Profiler artifact. */
public final class ProfilerRunCodec {
    private static final int MAGIC = 0x574c5052;
    private static final int VERSION = 1;
    private static final int DIGEST_BYTES = 32;
    private static final int MAX_TEXT_BYTES = 4096;
    private static final int MAX_CENSUS_BYTES = 512 * 1024 * 1024;
    private ProfilerRunCodec() {}

    public static byte[] encode(ProfilerRun run) {
        if (run == null) throw new NullPointerException("profiler run");
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);
            out.writeInt(MAGIC); out.writeInt(VERSION); out.writeByte(run.mode().ordinal());
            out.writeLong(run.startEpochMillis()); out.writeLong(run.endEpochMillis());
            out.writeShort(run.schema().size());
            for (ProfilerMetric metric : run.schema().metrics()) {
                writeText(out, metric.name()); writeText(out, metric.owner());
                out.writeByte(metric.unit().ordinal()); out.writeByte(metric.kind().ordinal());
                out.writeByte(metric.causality().ordinal());
            }
            out.writeByte(run.tags().size());
            for (Map.Entry<String, String> tag : run.tags().entrySet()) {
                writeText(out, tag.getKey()); writeText(out, tag.getValue());
            }
            byte[] census = FrameCensusCodec.encode(run.census());
            out.writeInt(census.length); out.write(census); out.flush();
            byte[] body = bytes.toByteArray(), digest = digest(body);
            bytes.write(digest); return bytes.toByteArray();
        } catch (IOException error) {
            throw new IllegalStateException("in-memory profiler encoding failed", error);
        }
    }

    public static ProfilerRun decode(byte[] artifact) {
        if (artifact == null) throw new NullPointerException("profiler artifact");
        require(artifact.length >= 32 + DIGEST_BYTES, "truncated profiler artifact");
        int bodyLength = artifact.length - DIGEST_BYTES;
        byte[] body = Arrays.copyOf(artifact, bodyLength);
        require(MessageDigest.isEqual(digest(body),
                Arrays.copyOfRange(artifact, bodyLength, artifact.length)),
                "profiler artifact checksum mismatch");
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(body))) {
            require(input.readInt() == MAGIC, "profiler artifact magic drift");
            require(input.readInt() == VERSION, "profiler artifact version drift");
            ProfilerRun.Mode mode = enumAt(ProfilerRun.Mode.values(), input.readUnsignedByte(),
                    "profiler mode");
            long start = input.readLong(), end = input.readLong();
            int metricCount = input.readUnsignedShort();
            require(metricCount > 0 && metricCount <= ProfilerSchema.MAX_METRICS,
                    "profiler metric count drift");
            List<ProfilerMetric> metrics = new ArrayList<ProfilerMetric>(metricCount);
            for (int index = 0; index < metricCount; index++) {
                String name = readText(input), owner = readText(input);
                ProfilerMetric.Unit unit = enumAt(ProfilerMetric.Unit.values(),
                        input.readUnsignedByte(), "profiler unit");
                ProfilerMetric.Kind kind = enumAt(ProfilerMetric.Kind.values(),
                        input.readUnsignedByte(), "profiler kind");
                ProfilerMetric.Causality causality = enumAt(ProfilerMetric.Causality.values(),
                        input.readUnsignedByte(), "profiler causality");
                metrics.add(ProfilerMetric.of(name, owner, unit, kind, causality));
            }
            int tagCount = input.readUnsignedByte();
            Map<String, String> tags = new LinkedHashMap<String, String>();
            for (int index = 0; index < tagCount; index++)
                require(tags.put(readText(input), readText(input)) == null,
                        "duplicate profiler tag");
            int censusLength = input.readInt();
            require(censusLength > 0 && censusLength <= MAX_CENSUS_BYTES
                    && censusLength == input.available(), "profiler census length drift");
            byte[] census = new byte[censusLength]; input.readFully(census);
            require(input.read() == -1, "trailing profiler artifact bytes");
            return ProfilerRun.of(ProfilerSchema.of(metrics), FrameCensusCodec.decode(census),
                    mode, start, end, tags);
        } catch (EOFException error) {
            throw new IllegalArgumentException("truncated profiler artifact body", error);
        } catch (IOException error) {
            throw new IllegalArgumentException("invalid profiler artifact body", error);
        }
    }

    private static void writeText(DataOutputStream out, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        require(bytes.length <= MAX_TEXT_BYTES, "profiler text too long");
        out.writeShort(bytes.length); out.write(bytes);
    }
    private static String readText(DataInputStream input) throws IOException {
        int length = input.readUnsignedShort();
        require(length <= MAX_TEXT_BYTES, "profiler text too long");
        byte[] bytes = new byte[length]; input.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
    private static <T> T enumAt(T[] values, int index, String label) {
        require(index >= 0 && index < values.length, label + " drift"); return values[index];
    }
    private static byte[] digest(byte[] value) {
        try { return MessageDigest.getInstance("SHA-256").digest(value); }
        catch (NoSuchAlgorithmException error) { throw new IllegalStateException(error); }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
