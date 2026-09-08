package worldline.b173server;

import java.util.Random;

/** Explicit controlled-runtime input: exhausts requested integer draws and rejects unexpected calls. */
final class B173FiniteDraws extends Random {
    private static final long serialVersionUID = 1L;
    private final int[] bounds, values;
    private int cursor;
    private final StringBuilder trace = new StringBuilder();
    B173FiniteDraws(int[] bounds, int[] values) {
        super(173L); this.bounds = bounds.clone(); this.values = values.clone();
        if (bounds.length != values.length) throw new IllegalArgumentException("draw matrix width");
    }
    @Override public int nextInt(int bound) {
        if (cursor == bounds.length || bounds[cursor] != bound || values[cursor] < 0 || values[cursor] >= bound)
            throw new IllegalStateException("unexpected native random bound " + bound + " at " + cursor);
        int result = values[cursor++]; trace.append(bound).append(':').append(result).append(';'); return result;
    }
    @Override public float nextFloat() { return 0.5F; }
    @Override public double nextDouble() { return 0.5D; }
    @Override public double nextGaussian() { return 0D; }
    String trace() {
        if (cursor != bounds.length) throw new IllegalStateException("native branch omitted an expected draw");
        return trace.toString();
    }
}
