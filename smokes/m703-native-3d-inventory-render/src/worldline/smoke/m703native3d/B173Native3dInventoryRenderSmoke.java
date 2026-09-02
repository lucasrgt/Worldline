package worldline.smoke.m703native3d;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import worldline.b173.B173BlockInventoryFrame;
import worldline.b173.B173BlockInventoryRender;
import worldline.testkit.NativeBlockRenderEvidence;
import worldline.testkit.NativeBlockRenderFixture;
import worldline.testapi.NativeBlockRenderObservation;
import worldline.testapi.NativeBlockRenderPlan;
import worldline.testapi.NativeBlockRenderSubject;

/** Renders one declarative 3D inventory family through mapped or official classes. */
public final class B173Native3dInventoryRenderSmoke {
    private B173Native3dInventoryRenderSmoke() { }

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 10) throw new IllegalArgumentException("expected render mapping");
        String role = arguments[0];
        Path clientJar = Paths.get(arguments[7]);
        Path catalog = Paths.get(arguments[8]);
        Path evidencePath = Paths.get(arguments[9]);
        List<NativeBlockRenderSubject> subjects = subjects(catalog);
        int[] ids = subjects.stream().mapToInt(NativeBlockRenderSubject::legacyId).toArray();
        int[] metadata = subjects.stream().mapToInt(NativeBlockRenderSubject::metadata).toArray();
        List<B173BlockInventoryFrame> frames = B173BlockInventoryRender.render(clientJar,
                arguments[1], arguments[2], arguments[3], arguments[4], arguments[5],
                arguments[6], ids, metadata);
        List<NativeBlockRenderObservation> observations = new ArrayList<>();
        for (int index = 0; index < subjects.size(); index++) {
            NativeBlockRenderSubject subject = subjects.get(index);
            B173BlockInventoryFrame frame = frames.get(index);
            require(subject.renderType() == frame.renderType(),
                    "catalog render type drift: " + subject.subject());
            observations.add(new NativeBlockRenderObservation(subject.subject(),
                    frame.legacyId(), frame.metadata(), frame.renderType(), frame.geometryPixels(),
                    frame.frameSha256(), "draw-calls=" + frame.drawCalls()
                            + ",pixels=" + frame.geometryPixels()));
        }
        NativeBlockRenderEvidence evidence = NativeBlockRenderFixture.verify(
                new NativeBlockRenderPlan("native-3d-inventory-render", subjects), observations);
        String canonical = evidence.canonical();
        Files.createDirectories(evidencePath.getParent());
        Files.write(evidencePath, canonical.getBytes(StandardCharsets.UTF_8));
        String signature = sha256(canonical.getBytes(StandardCharsets.UTF_8));
        String provenance = frames.get(0).provenance().replace('\\', '/');
        require(frames.stream().allMatch(frame -> frame.provenance().replace('\\', '/')
                .equals(provenance)), "mixed native renderer provenance");
        System.out.println("WORLDLINE_M703_ROLE=" + role);
        System.out.println("WORLDLINE_M703_SUBJECTS=" + subjects.size());
        System.out.println("WORLDLINE_M703_SIGNATURE=" + signature);
        System.out.println("WORLDLINE_M703_PROVENANCE=" + provenance);
    }

    private static List<NativeBlockRenderSubject> subjects(Path path) throws Exception {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        require(!lines.isEmpty() && lines.get(0).equals(
                "subject_id\tlegacy_id\tname\tmetadata\trender_type"),
                "native render catalog header drift");
        List<NativeBlockRenderSubject> rows = new ArrayList<>();
        for (int index = 1; index < lines.size(); index++) {
            String[] fields = lines.get(index).split("\\t", -1);
            require(fields.length == 5 && fields[2].matches("[a-z0-9-]+"),
                    "invalid native render catalog row " + index);
            rows.add(new NativeBlockRenderSubject(fields[0], Integer.parseInt(fields[1]),
                    Integer.parseInt(fields[3]), Integer.parseInt(fields[4])));
        }
        require(rows.size() == 63, "native render subject census drift: " + rows.size());
        return rows;
    }

    private static String sha256(byte[] bytes) throws Exception {
        StringBuilder value = new StringBuilder();
        for (byte item : MessageDigest.getInstance("SHA-256").digest(bytes)) {
            value.append(String.format("%02x", item & 255));
        }
        return value.toString();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
