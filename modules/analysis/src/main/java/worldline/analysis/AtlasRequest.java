package worldline.analysis;

import java.nio.file.Path;

/** One atlas request resolved against an official dedicated server. */
public final class AtlasRequest {
    private final long seed;
    private final int radius;
    private final Path serverJar, workspace;

    public AtlasRequest(long seed, int radius, Path serverJar, Path workspace) {
        if (serverJar == null || workspace == null) throw new NullPointerException("atlas paths");
        if (radius < 1 || radius > 4) throw new IllegalArgumentException("invalid atlas radius");
        this.seed = seed; this.radius = radius;
        this.serverJar = serverJar; this.workspace = workspace;
    }

    public long seed() { return seed; }
    public int radius() { return radius; }
    public Path serverJar() { return serverJar; }
    public Path workspace() { return workspace; }
}
