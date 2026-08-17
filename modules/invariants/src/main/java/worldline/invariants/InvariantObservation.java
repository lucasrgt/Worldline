package worldline.invariants;

import worldline.api.EntityCensus;
import worldline.api.InvariantSample;
import worldline.api.ItemCensus;
import worldline.api.WearCensus;

/** One fail-closed sample of observable game state. */
public final class InvariantObservation {
    private final InvariantSample sample;

    private InvariantObservation(InvariantSample sample) {
        this.sample = sample;
    }

    public static InvariantObservation of(ItemCensus items) {
        return of(InvariantSample.of(items));
    }

    public static InvariantObservation of(ItemCensus items, ItemCensus blocks) {
        return of(InvariantSample.of(items, blocks));
    }

    public static InvariantObservation of(InvariantSample sample) {
        if (sample == null) throw new NullPointerException("sample");
        return new InvariantObservation(sample);
    }

    public ItemCensus items() {
        return sample.items();
    }

    public ItemCensus blocks() {
        return sample.blocks();
    }

    public EntityCensus entities() {
        return sample.entities();
    }

    public ItemCensus imported() {
        return sample.imported();
    }

    public long time() {
        return sample.time();
    }

    public ItemCensus importedBlocks() {
        return sample.importedBlocks();
    }

    public EntityCensus importedEntities() {
        return sample.importedEntities();
    }

    public WearCensus wear() {
        return sample.wear();
    }

    public int health() {
        return sample.health();
    }

    public boolean peaceful() {
        return sample.peaceful();
    }
}
