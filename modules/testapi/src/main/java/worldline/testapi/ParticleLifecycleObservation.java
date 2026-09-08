package worldline.testapi;

/** One native particle at an exact controlled tick, including its manager membership and frame. */
public final class ParticleLifecycleObservation {
    private final String particle, position, frame;
    private final int tick, age, count, pixels;
    private final boolean dead;

    public ParticleLifecycleObservation(String particle, int tick, int age, boolean dead,
            int count, String position, int pixels, String frame) {
        if (particle == null || !particle.matches("Entity(?:Smoke|Flame|Reddust)FX")
                || tick < 0 || tick > 9 || age < 0 || age > 9 || count < 0 || count > 1
                || pixels < 0 || pixels > 16384 || position == null
                || !position.matches("[0-9a-f]{16},[0-9a-f]{16},[0-9a-f]{16}")
                || frame == null || !frame.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("invalid particle observation");
        }
        this.particle = particle; this.tick = tick; this.age = age; this.dead = dead;
        this.count = count; this.position = position; this.pixels = pixels; this.frame = frame;
    }
    public String particle() { return particle; }
    public int tick() { return tick; }
    public int age() { return age; }
    public boolean dead() { return dead; }
    public int count() { return count; }
    public int pixels() { return pixels; }
    public String canonical() {
        return particle + "|tick=" + tick + "|age=" + age + "|dead=" + dead + "|count=" + count
                + "|position=" + position + "|pixels=" + pixels + "|frame=" + frame;
    }
}
