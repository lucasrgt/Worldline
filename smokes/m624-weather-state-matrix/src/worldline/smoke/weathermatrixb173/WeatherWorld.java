package worldline.smoke.weathermatrixb173;

import net.minecraft.src.ISaveHandler;
import net.minecraft.src.World;
import net.minecraft.src.WorldInfo;

/** Exposes only the protected official weather update and strength state. */
final class WeatherWorld extends World {
  WeatherWorld(ISaveHandler handler, String name, long seed) {
    super(handler, name, seed, null);
  }

  void advanceWeather() {
    updateWeather();
  }

  int[] weather() {
    return values(worldInfo, Math.round(field_27078_C * 1000F),
        Math.round(field_27076_E * 1000F));
  }

  int[] persisted() {
    return values(new WorldInfo(worldInfo.func_22185_a()), -1, -1);
  }

  private static int[] values(WorldInfo info, int rainStrength, int thunderStrength) {
    return new int[] {info.getIsRaining() ? 1 : 0, info.getRainTime(),
        info.getIsThundering() ? 1 : 0, info.getThunderTime(), rainStrength, thunderStrength};
  }
}
