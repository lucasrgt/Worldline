package aero.modellib;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.HexFormat;

/** Writes one complete M768 census without duplicating its payload in heap. */
final class WorldlineHistoricalArtifact {
    private WorldlineHistoricalArtifact() {}

    static String write(Path path, String[] metrics, long[] rows, int count, int width)
            throws Exception {
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        Files.deleteIfExists(temporary);
        MessageDigest bodyDigest = MessageDigest.getInstance("SHA-256");
        try {
            try (OutputStream file = new BufferedOutputStream(Files.newOutputStream(temporary,
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE));
                    DigestOutputStream hashed = new DigestOutputStream(file, bodyDigest);
                    DataOutputStream body = new DataOutputStream(hashed)) {
                writeBody(body, metrics, rows, count, width);
            }
            Files.write(temporary, bodyDigest.digest(), StandardOpenOption.APPEND);
            move(temporary, path);
            return sha256(path);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void writeBody(DataOutputStream body, String[] metrics, long[] rows,
            int count, int width) throws Exception {
        body.writeInt(0x574c4643);
        body.writeInt(1);
        body.writeInt(metrics.length);
        for (String metric : metrics) {
            byte[] name = metric.getBytes(StandardCharsets.US_ASCII);
            body.writeByte(name.length);
            body.write(name);
        }
        body.writeInt(count);
        long priorTime = -1L;
        for (int frame = 0; frame < count; frame++) {
            int base = frame * width;
            require(rows[base] == frame && rows[base + 1] > priorTime,
                    "invalid M768 frame identity");
            for (int column = 0; column < width; column++) {
                long value = rows[base + column];
                require(value >= 0L, "negative M768 metric");
                body.writeLong(value);
            }
            priorTime = rows[base + 1];
        }
    }

    private static void move(Path source, Path target) throws Exception {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static String sha256(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = new BufferedInputStream(Files.newInputStream(path))) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = input.read(buffer)) >= 0) digest.update(buffer, 0, length);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
