package worldline.api;

/** Integer pixel rectangle in the current GUI viewport. */
public final class GameUiBounds {
    private final int x, y, width, height;

    public GameUiBounds(int x, int y, int width, int height) {
        if (width < 0 || height < 0) throw new IllegalArgumentException("UI bounds size");
        this.x = x; this.y = y; this.width = width; this.height = height;
    }

    public int x() { return x; }
    public int y() { return y; }
    public int width() { return width; }
    public int height() { return height; }
    public int right() { return Math.addExact(x, width); }
    public int bottom() { return Math.addExact(y, height); }
    public int centerX() { return Math.addExact(x, width / 2); }
    public int centerY() { return Math.addExact(y, height / 2); }
    public int area() { return Math.multiplyExact(width, height); }
    public boolean empty() { return width == 0 || height == 0; }

    public boolean contains(int pointX, int pointY) {
        return pointX >= x && pointY >= y && pointX < right() && pointY < bottom();
    }

    public boolean contains(GameUiBounds other) {
        if (other == null) throw new NullPointerException("other");
        return other.x >= x && other.y >= y && other.right() <= right() && other.bottom() <= bottom();
    }

    public boolean overlaps(GameUiBounds other) {
        if (other == null) throw new NullPointerException("other");
        return !empty() && !other.empty() && x < other.right() && right() > other.x
                && y < other.bottom() && bottom() > other.y;
    }

    public GameUiBounds intersection(GameUiBounds other) {
        if (other == null) throw new NullPointerException("other");
        int left = Math.max(x, other.x), top = Math.max(y, other.y);
        int right = Math.min(right(), other.right()), bottom = Math.min(bottom(), other.bottom());
        return new GameUiBounds(left, top, Math.max(0, right - left), Math.max(0, bottom - top));
    }

    @Override public boolean equals(Object other) {
        if (!(other instanceof GameUiBounds)) return false;
        GameUiBounds value = (GameUiBounds) other;
        return x == value.x && y == value.y && width == value.width && height == value.height;
    }

    @Override public int hashCode() {
        return 31 * (31 * (31 * x + y) + width) + height;
    }

    @Override public String toString() {
        return "GameUiBounds[" + x + "," + y + " " + width + "x" + height + "]";
    }
}
