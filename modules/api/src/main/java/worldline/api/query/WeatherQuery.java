package worldline.api.query;

import java.util.Objects;

/** Neutral weather observation, independent of rain-start/stop scenario types. */
public final class WeatherQuery {
    private final boolean raining;
    private final boolean thunder;

    private WeatherQuery(boolean raining, boolean thunder) {
        this.raining = raining;
        this.thunder = thunder;
    }

    public static WeatherQuery clear() { return new WeatherQuery(false, false); }
    public static WeatherQuery rain() { return new WeatherQuery(true, false); }
    public static WeatherQuery thunderstorm() { return new WeatherQuery(true, true); }

    public static WeatherQuery of(boolean raining, boolean thunder) {
        if (thunder && !raining)
            throw new IllegalArgumentException("thunder without rain");
        return new WeatherQuery(raining, thunder);
    }

    public boolean raining() { return raining; }
    public boolean thunder() { return thunder; }
    public boolean clearSkies() { return !raining && !thunder; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof WeatherQuery)) return false;
        WeatherQuery value = (WeatherQuery) other;
        return raining == value.raining && thunder == value.thunder;
    }

    @Override public int hashCode() { return Objects.hash(raining, thunder); }
}
