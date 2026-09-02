package worldline.b173server;

import java.net.ServerSocket;
import java.nio.file.Path;
import java.time.Duration;
import java.util.TreeMap;
import worldline.analysis.AtlasRequest;
import worldline.analysis.AtlasRunner;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;

/** Renders a seed terrain atlas through the official dedicated server. */
public final class B173AtlasRunner implements AtlasRunner {
    private static final Duration TIMEOUT = Duration.ofSeconds(120);
    private static final double STEP = 4.0D;

    @Override
    public String render(AtlasRequest request) {
        int radius = request.radius();
        int side = radius * 2 + 1;
        int port = freePort();
        B173DedicatedServer server = OfficialServerBootstrap.start(request.serverJar(),
                request.workspace(), port, request.seed(), TIMEOUT);
        B173WireClient client = new B173WireClient("127.0.0.1", port, "atlas", TIMEOUT);
        TreeMap<Integer, Integer> palette = new TreeMap<>();
        StringBuilder cells = new StringBuilder();
        try {
            server.boot();
            B173PlayerSeed.write(request.workspace(), "atlas", 8.5D, 120.0D, 8.5D);
            client.connect();
            client.synchronizePose();
            RemoteChunkSnapshot[][] chunks = capture(client, radius);
            render(chunks, radius, palette, cells);
        } catch (Exception failure) {
            throw new IllegalStateException("atlas capture failed: " + root(failure), failure);
        } finally {
            client.close();
            server.close();
        }
        return page(request, side, palette, cells.toString());
    }

    /** Visits every chunk in a spiral, gliding under the server speed limit. */
    private static RemoteChunkSnapshot[][] capture(B173WireClient client, int radius)
            throws Exception {
        int side = radius * 2 + 1;
        RemoteChunkSnapshot[][] chunks = new RemoteChunkSnapshot[side][side];
        double x = 8.5D, z = 8.5D;
        for (int ring = 0; ring <= radius; ring++) {
            for (int cx = -ring; cx <= ring; cx++) {
                for (int cz = -ring; cz <= ring; cz++) {
                    if (Math.max(Math.abs(cx), Math.abs(cz)) != ring) continue;
                    double[] at = glide(client, x, z, cx * 16 + 8, cz * 16 + 8);
                    x = at[0];
                    z = at[1];
                    RemoteWorldView view = client.awaitRemoteChunk(cx, cz);
                    chunks[cx + radius][cz + radius] = view.chunkAt(cx, cz);
                }
            }
        }
        return chunks;
    }

    /** Moves in <=4-block steps so the vanilla anti-cheat never trips. */
    private static double[] glide(B173WireClient client, double x, double z,
            double targetX, double targetZ) throws Exception {
        while (Math.abs(targetX - x) > STEP || Math.abs(targetZ - z) > STEP) {
            double dx = clamp(targetX - x), dz = clamp(targetZ - z);
            client.moveAndObserve(dx, 0.0D, dz, 1);
            x += dx;
            z += dz;
        }
        client.moveAndObserve(targetX - x, 0.0D, targetZ - z, 2);
        return new double[] {targetX, targetZ};
    }

    private static double clamp(double value) {
        return Math.max(-STEP, Math.min(STEP, value));
    }

    private static void render(RemoteChunkSnapshot[][] chunks, int radius,
            TreeMap<Integer, Integer> palette, StringBuilder cells) {
        for (int worldZ = -radius * 16; worldZ < radius * 16 + 16; worldZ += 16) {
            for (int innerZ = 0; innerZ < 16; innerZ++) {
                cells.append("<tr>");
                for (int worldX = -radius * 16; worldX < radius * 16 + 16; worldX += 16) {
                    int chunkX = (worldX >> 4) + radius, chunkZ = (worldZ >> 4) + radius;
                    int x = worldX & 15, z = innerZ;
                    RemoteChunkSnapshot chunk = chunks[chunkX][chunkZ];
                    cells.append(cell(chunks[chunkX][chunkZ], x, top(chunk, x, z),
                            z, palette));
                }
                cells.append("</tr>\n");
            }
        }
    }

    private static int top(RemoteChunkSnapshot chunk, int x, int z) {
        for (int y = 127; y >= 0; y--) {
            if (chunk.blockAt(x, y, z).legacyId() != 0) return y;
        }
        return 0;
    }

    private static String cell(RemoteChunkSnapshot chunk, int x, int top, int unused,
            TreeMap<Integer, Integer> palette) {
        int id = chunk.blockAt(x, top, unused).legacyId();
        Integer count = palette.get(id);
        palette.put(id, count == null ? 1 : count + 1);
        return "<td title=\"id=" + id + " y=" + top
                + "\" style=\"background:" + color(id) + "\"></td>";
    }

    private static String color(int id) {
        return String.format("#%02x%02x%02x",
                40 + (id * 37) % 200, 40 + (id * 61) % 180, 60 + (id * 97) % 160);
    }

    private static String page(AtlasRequest request, int side,
            TreeMap<Integer, Integer> palette, String bodyCells) {
        StringBuilder legend = new StringBuilder();
        for (java.util.Map.Entry<Integer, Integer> entry : palette.entrySet()) {
            legend.append("<span style=\"color:").append(color(entry.getKey()))
                    .append("\">&#9632;</span>").append(entry.getKey())
                    .append("&times;").append(entry.getValue()).append(' ');
        }
        return "<!DOCTYPE html>\n<html><head><meta charset=\"utf-8\">"
                + "<title>Worldline Seed Atlas</title><style>"
                + "body{font-family:monospace;background:#111;color:#ddd;margin:24px}"
                + "table{border-collapse:collapse;border:1px solid #444}"
                + "td{width:9px;height:9px}</style></head><body>\n"
                + "<h1>Worldline Seed Atlas</h1>\n<p>seed=" + request.seed()
                + " radius=" + request.radius() + " side=" + side * 16 + "</p>\n"
                + "<p>" + legend + "</p>\n<table>\n" + bodyCells
                + "</table>\n</body></html>\n";
    }

    private static Throwable root(Throwable failure) {
        Throwable cause = failure;
        while (cause.getCause() != null) cause = cause.getCause();
        return cause;
    }

    private static int side(int radius) { return radius * 2 + 1; }

    private static int freePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (Exception error) {
            throw new IllegalStateException("no free port for atlas server", error);
        }
    }
}
