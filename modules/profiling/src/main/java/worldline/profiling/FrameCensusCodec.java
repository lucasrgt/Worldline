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
import java.util.Arrays;

/** Canonical checksum-protected binary representation of a {@link FrameCensus}. */
public final class FrameCensusCodec {
    private static final int MAGIC = 0x574c4643;
    private static final int VERSION = 1;
    private static final int DIGEST_BYTES = 32;
    private static final int MAX_METRICS = 256;
    private static final int MAX_FRAMES = 5_000_000;

    private FrameCensusCodec() {}

    public static byte[] encode(FrameCensus census) {
        if (census == null) throw new NullPointerException("census");
        require(census.metrics() <= MAX_METRICS, "frame metric count drift");
        require(census.frames() <= MAX_FRAMES, "frame count drift");
        try {
            ByteArrayOutputStream bodyBytes = new ByteArrayOutputStream();
            DataOutputStream body = new DataOutputStream(bodyBytes);
            body.writeInt(MAGIC); body.writeInt(VERSION); body.writeInt(census.metrics());
            for (String metric : census.metricNames()) writeAscii(body, metric);
            body.writeInt(census.frames());
            for (int frame = 0; frame < census.frames(); frame++)
                for (long value : census.row(frame)) body.writeLong(value);
            body.flush();
            byte[] payload = bodyBytes.toByteArray(), digest = digest(payload);
            ByteArrayOutputStream result = new ByteArrayOutputStream(payload.length + digest.length);
            result.write(payload); result.write(digest);
            return result.toByteArray();
        } catch (IOException error) {
            throw new IllegalStateException("in-memory frame census encoding failed", error);
        }
    }

    public static FrameCensus decode(byte[] artifact) {
        if (artifact == null) throw new NullPointerException("artifact");
        require(artifact.length >= 16 + DIGEST_BYTES, "truncated frame census");
        int bodyLength = artifact.length - DIGEST_BYTES;
        byte[] body = Arrays.copyOf(artifact, bodyLength);
        byte[] expected = Arrays.copyOfRange(artifact, bodyLength, artifact.length);
        require(MessageDigest.isEqual(digest(body), expected), "frame census checksum mismatch");
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(body))) {
            require(input.readInt() == MAGIC, "frame census magic drift");
            require(input.readInt() == VERSION, "frame census version drift");
            int metricCount = input.readInt();
            require(metricCount > 0 && metricCount <= MAX_METRICS, "frame metric count drift");
            String[] metrics = new String[metricCount];
            for (int index = 0; index < metricCount; index++) metrics[index] = readAscii(input);
            int frames = input.readInt();
            require(frames > 0 && frames <= MAX_FRAMES, "frame count drift");
            long values = (long) frames * (metricCount + 2L);
            require(values * Long.BYTES == input.available(), "frame census body length drift");
            long[][] rows = new long[frames][metricCount + 2];
            for (int frame = 0; frame < frames; frame++)
                for (int column = 0; column < metricCount + 2; column++)
                    rows[frame][column] = input.readLong();
            require(input.read() == -1, "trailing frame census body bytes");
            return FrameCensus.of(metrics, rows);
        } catch (EOFException error) {
            throw new IllegalArgumentException("truncated frame census body", error);
        } catch (IOException error) {
            throw new IllegalArgumentException("invalid frame census body", error);
        }
    }

    private static void writeAscii(DataOutputStream output, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
        require(bytes.length > 0 && bytes.length <= 255, "frame metric name length drift");
        output.writeByte(bytes.length); output.write(bytes);
    }

    private static String readAscii(DataInputStream input) throws IOException {
        int length = input.readUnsignedByte();
        require(length > 0, "empty frame metric name");
        byte[] bytes = new byte[length]; input.readFully(bytes);
        for (byte value : bytes) require(value > 0 && value < 128, "non-ASCII frame metric name");
        return new String(bytes, StandardCharsets.US_ASCII);
    }

    private static byte[] digest(byte[] value) {
        try { return MessageDigest.getInstance("SHA-256").digest(value); }
        catch (NoSuchAlgorithmException error) { throw new IllegalStateException(error); }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
