package worldline.atlas;

/** One deterministic ranked Atlas match. */
public final class AtlasHit implements Comparable<AtlasHit> {
    private final AtlasRecord record;
    private final int score;
    private final String relation;

    AtlasHit(AtlasRecord record, int score, String relation) {
        this.record = record; this.score = score; this.relation = relation;
    }

    public AtlasRecord record() { return record; }
    public int score() { return score; }
    public String relation() { return relation; }

    @Override public int compareTo(AtlasHit other) {
        int byScore = Integer.compare(other.score, score);
        return byScore != 0 ? byScore : record.id().compareTo(other.record.id());
    }
}
