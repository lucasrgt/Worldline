/** Exposes the protected official weather update and strength fields. */
final class OracleWeatherWorld extends dj {
  OracleWeatherWorld(om handler, String name, long seed) {
    super(handler, name, seed, null);
  }
  void advanceWeather() { i(); }
  int rainStrength() { return Math.round(j * 1000F); }
  int thunderStrength() { return Math.round(l * 1000F); }
}
