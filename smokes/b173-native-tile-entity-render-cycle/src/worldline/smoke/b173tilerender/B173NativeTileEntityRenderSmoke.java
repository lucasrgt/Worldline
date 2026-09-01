package worldline.smoke.b173tilerender;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import worldline.b173.B173TileEntityFrame;
import worldline.b173.B173TileEntityRender;
import worldline.testkit.NativeTileEntityRenderEvidence;
import worldline.testkit.NativeTileEntityRenderFixture;
import worldline.testkit.NativeTileEntityRenderObservation;
import worldline.testkit.NativeTileEntityRenderPlan;
import worldline.testkit.NativeTileEntityRenderSubject;

/** Renders every render-type -1 block through mapped or official tile renderers. */
public final class B173NativeTileEntityRenderSmoke {
    private B173NativeTileEntityRenderSmoke() { }

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 48) throw new IllegalArgumentException("expected tile mapping");
        String role = arguments[0];
        String[] names = Arrays.copyOfRange(arguments, 1, 45);
        Path clientJar = Paths.get(arguments[45]);
        Path catalog = Paths.get(arguments[46]);
        Path evidencePath = Paths.get(arguments[47]);
        List<NativeTileEntityRenderSubject> subjects = subjects(catalog);
        int[] ids = subjects.stream().mapToInt(NativeTileEntityRenderSubject::legacyId).toArray();
        int[] metadata = subjects.stream().mapToInt(
                NativeTileEntityRenderSubject::metadata).toArray();
        String[] routes = subjects.stream().map(NativeTileEntityRenderSubject::renderer)
                .toArray(String[]::new);
        List<B173TileEntityFrame> frames = B173TileEntityRender.render(
                clientJar, names, ids, metadata, routes);
        List<NativeTileEntityRenderObservation> observations = new ArrayList<>();
        for (int index = 0; index < subjects.size(); index++) {
            NativeTileEntityRenderSubject subject = subjects.get(index);
            B173TileEntityFrame frame = frames.get(index);
            require(subject.legacyId() == frame.legacyId()
                    && subject.metadata() == frame.metadata()
                    && subject.renderer().equals(frame.renderer()), "tile render identity drift");
            observations.add(new NativeTileEntityRenderObservation(subject,
                    frame.geometryPixels(), frame.frameSha256(),
                    "draw-calls=1,pixels=" + frame.geometryPixels()));
        }
        NativeTileEntityRenderEvidence evidence = NativeTileEntityRenderFixture.verify(
                new NativeTileEntityRenderPlan("native-tile-entity-render", subjects),
                observations);
        String canonical = evidence.canonical();
        Files.createDirectories(evidencePath.getParent());
        Files.write(evidencePath, canonical.getBytes(StandardCharsets.UTF_8));
        String signature = sha256(canonical.getBytes(StandardCharsets.UTF_8));
        String provenance = frames.get(0).provenance().replace('\\', '/');
        require(frames.stream().allMatch(frame -> frame.provenance().replace('\\', '/')
                .equals(provenance)), "mixed tile renderer provenance");
        System.out.println("WORLDLINE_TILE_RENDER_ROLE=" + role);
        System.out.println("WORLDLINE_TILE_RENDER_SUBJECTS=" + subjects.size());
        System.out.println("WORLDLINE_TILE_RENDER_SIGNATURE=" + signature);
        System.out.println("WORLDLINE_TILE_RENDER_PROVENANCE=" + provenance);
    }

    private static List<NativeTileEntityRenderSubject> subjects(Path path) throws Exception {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        require(!lines.isEmpty() && lines.get(0).equals(
                "subject_id\tlegacy_id\tname\tmetadata\trender_type"), "catalog header drift");
        List<NativeTileEntityRenderSubject> rows = new ArrayList<>();
        for (int index = 1; index < lines.size(); index++) {
            String[] fields = lines.get(index).split("\\t", -1);
            require(fields.length == 5 && fields[2].matches("[a-z0-9-]+")
                    && Integer.parseInt(fields[4]) == -1, "invalid catalog row " + index);
            int legacyId = Integer.parseInt(fields[1]);
            rows.add(new NativeTileEntityRenderSubject(fields[0], legacyId,
                    Integer.parseInt(fields[3]), legacyId == 36 ? "moving-piston" : "sign",
                    legacyId == 36 ? "SINGULAR" : "ARCHETYPE"));
        }
        require(rows.size() == 3, "native tile-render census drift: " + rows.size());
        return rows;
    }

    private static String sha256(byte[] bytes) throws Exception {
        StringBuilder value = new StringBuilder();
        for (byte item : MessageDigest.getInstance("SHA-256").digest(bytes))
            value.append(String.format("%02x", item & 255));
        return value.toString();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
