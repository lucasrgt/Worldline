package worldline.testapi;

import java.util.Objects;

/** One native entity width, height, offset, AABB, and contact disposition observation. */
public final class EntityPhysicalEnvelopeObservation {
    private final String subject;
    private final float width, height, yOffset;
    private final boolean collidable, pushable, pairBox, centered, vertical;

    public EntityPhysicalEnvelopeObservation(String subject, float width, float height,
            float yOffset, boolean collidable, boolean pushable, boolean pairBox,
            boolean centered, boolean vertical) {
        if (subject == null || !subject.matches(
                "b[0-9]+\\.[0-9]+\\.[0-9]+:entity/[0-9]{3}")) {
            throw new IllegalArgumentException("subject");
        }
        if (!finitePositive(width) || !finitePositive(height) || !Float.isFinite(yOffset)) {
            throw new IllegalArgumentException("physical dimensions");
        }
        this.subject = subject;
        this.width = width;
        this.height = height;
        this.yOffset = yOffset;
        this.collidable = collidable;
        this.pushable = pushable;
        this.pairBox = pairBox;
        this.centered = centered;
        this.vertical = vertical;
    }

    public String subject() { return subject; }
    public float width() { return width; }
    public float height() { return height; }
    public float yOffset() { return yOffset; }
    public boolean collidable() { return collidable; }
    public boolean pushable() { return pushable; }
    public boolean pairBox() { return pairBox; }
    public boolean centered() { return centered; }
    public boolean vertical() { return vertical; }

    String canonical() {
        return "width=" + Float.toString(width) + "|height=" + Float.toString(height)
                + "|y-offset=" + Float.toString(yOffset)
                + "|collidable=" + collidable + "|pushable=" + pushable
                + "|pair-box=" + pairBox + "|centered=" + centered
                + "|vertical=" + vertical;
    }

    @Override public boolean equals(Object other) {
        if (!(other instanceof EntityPhysicalEnvelopeObservation)) return false;
        EntityPhysicalEnvelopeObservation value = (EntityPhysicalEnvelopeObservation) other;
        return subject.equals(value.subject)
                && Float.floatToIntBits(width) == Float.floatToIntBits(value.width)
                && Float.floatToIntBits(height) == Float.floatToIntBits(value.height)
                && Float.floatToIntBits(yOffset) == Float.floatToIntBits(value.yOffset)
                && collidable == value.collidable && pushable == value.pushable
                && pairBox == value.pairBox && centered == value.centered
                && vertical == value.vertical;
    }

    @Override public int hashCode() {
        return Objects.hash(subject, Float.floatToIntBits(width), Float.floatToIntBits(height),
                Float.floatToIntBits(yOffset), collidable, pushable, pairBox, centered, vertical);
    }

    private static boolean finitePositive(float value) {
        return Float.isFinite(value) && value > 0F;
    }
}
