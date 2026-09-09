package worldline.testapi;

/** One native outcome with the exact bounded integer draws consumed by its action. */
public final class RandomOutcomeObservation {
    private final String key, draws;
    private final int counter, result, item, count;
    private final boolean dead;
    public RandomOutcomeObservation(String key, String draws, int counter, int result,
            int item, int count, boolean dead) {
        if (key == null || !key.matches("[a-z0-9/-]+") || draws == null
                || !draws.matches("(?:[0-9]+:[0-9]+;)*") || counter < 0 || result < 0 || item < 0 || count < 0)
            throw new IllegalArgumentException("invalid random outcome");
        this.key = key; this.draws = draws; this.counter = counter; this.result = result;
        this.item = item; this.count = count; this.dead = dead;
    }
    public String key() { return key; }
    public String draws() { return draws; }
    public int counter() { return counter; }
    public int result() { return result; }
    public int item() { return item; }
    public int count() { return count; }
    public boolean dead() { return dead; }
    public String canonical() {
        return key + "|" + draws + "|" + counter + "|" + result + "|" + item + "|" + count + "|" + dead;
    }
}
