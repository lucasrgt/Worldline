package worldline.api;

/** First-class equality result of two WorldlineEvidence values. */
public final class WorldlineEvidenceDiff {
    private final WorldlineEvidence left, right;

    WorldlineEvidenceDiff(WorldlineEvidence left, WorldlineEvidence right) {
        this.left = left;
        this.right = right;
    }

    public WorldlineEvidence left() { return left; }
    public WorldlineEvidence right() { return right; }
    public boolean sameBehavior() { return left.behavior().equals(right.behavior()); }
    public boolean sameSignature() { return left.signature().equals(right.signature()); }
    public boolean diverged() { return !left.equals(right); }

    public String render() {
        return "left.behavior=" + left.token() + "\nright.behavior=" + right.token() + "\n"
                + "left.atlas=" + left.behavior().atlasId() + "\nright.atlas=" + right.behavior().atlasId() + "\n"
                + "left.lane=" + left.lane() + "\nright.lane=" + right.lane() + "\n"
                + "same.behavior=" + sameBehavior() + "\nsame.signature=" + sameSignature() + "\n"
                + "diverged=" + diverged() + "\n"
                + "left.signature=" + left.signature() + "\nright.signature=" + right.signature() + "\n"
                + "left.signal=" + left.signal() + "\nright.signal=" + right.signal() + "\n";
    }
}
