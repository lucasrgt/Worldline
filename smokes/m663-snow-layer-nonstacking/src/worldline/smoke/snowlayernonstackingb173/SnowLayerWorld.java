package worldline.smoke.snowlayernonstackingb173;

import net.minecraft.src.BiomeGenBase;
import net.minecraft.src.Block;
import net.minecraft.src.EnumSkyBlock;
import net.minecraft.src.ISaveHandler;
import net.minecraft.src.World;

/** Exposes native weather priming and bounded ambient snow observations. */
final class SnowLayerWorld extends World {
    private static final int SURFACE_Y = 65;
    private final boolean snowfall;
    private int coldX;
    private int coldZ;
    private int minimumX;
    private int minimumZ;
    private int targetX;
    private int targetZ;
    private boolean targetFound;

    SnowLayerWorld(ISaveHandler handler, String name, long seed, boolean snowfall) {
        super(handler, name, seed, null);
        this.snowfall = snowfall;
    }

    @SuppressWarnings("unchecked")
    void prepare() {
        int centerX = 0;
        int centerZ = 0;
        boolean found = false;
        for (int cx = -128; cx <= 128 && !found; cx++) {
            for (int cz = -128; cz <= 128; cz++) {
                BiomeGenBase biome = getWorldChunkManager().getBiomeGenAt(
                        cx * 16 + 8, cz * 16 + 8);
                if (biome.getEnableSnow()) {
                    centerX = cx;
                    centerZ = cz;
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            throw new IllegalStateException("seed has no cold biome in search boundary");
        }
        minimumX = (centerX - 9) * 16;
        minimumZ = (centerZ - 9) * 16;
        for (int cx = centerX - 9; cx <= centerX + 9; cx++) {
            for (int cz = centerZ - 9; cz <= centerZ + 9; cz++) {
                getChunkFromChunkCoords(cx, cz);
            }
        }
        playerEntities.add(new SnowLayerPlayer(this, centerX * 16, centerZ * 16));
        found = false;
        for (int x = minimumX; x < minimumX + 304 && !found; x++) {
            for (int z = minimumZ; z < minimumZ + 304; z++) {
                if (getWorldChunkManager().getBiomeGenAt(x, z).getEnableSnow()) {
                    coldX = x;
                    coldZ = z;
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            throw new IllegalStateException("active radius has no cold biome");
        }
        for (int step = 0; step < 25; step++) {
            updateWeather();
        }
    }

    void ambientPass() {
        doRandomUpdateTicks();
        rememberTarget();
    }

    int[] observation() {
        rememberTarget();
        int x = targetFound ? targetX : coldX;
        int z = targetFound ? targetZ : coldZ;
        return new int[] {
            getBlockId(x, SURFACE_Y, z),
            getBlockMetadata(x, SURFACE_Y, z),
            getBlockId(x, SURFACE_Y + 1, z),
            getBlockMetadata(x, SURFACE_Y + 1, z),
            getWorldChunkManager().getBiomeGenAt(x, z).getEnableSnow() ? 1 : 0,
            snowfall ? 1 : 0,
            getSavedLightValue(EnumSkyBlock.Block, x, SURFACE_Y, z),
            targetFound ? 1 : 0,
            columnSnowCount(x, z)
        };
    }

    private void rememberTarget() {
        if (targetFound) {
            return;
        }
        for (int x = minimumX; x < minimumX + 304; x++) {
            for (int z = minimumZ; z < minimumZ + 304; z++) {
                if (isSnowAt(x, SURFACE_Y, z)) {
                    targetX = x;
                    targetZ = z;
                    targetFound = true;
                    return;
                }
            }
        }
    }

    private boolean isSnowAt(int x, int y, int z) {
        return getBlockId(x, y, z) == Block.snow.blockID;
    }

    private int columnSnowCount(int x, int z) {
        int count = 0;
        for (int y = 0; y < 128; y++) {
            if (isSnowAt(x, y, z)) {
                count++;
            }
        }
        return count;
    }
}
