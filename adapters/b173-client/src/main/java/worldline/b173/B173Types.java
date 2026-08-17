package worldline.b173;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityChicken;
import net.minecraft.src.EntityCow;
import net.minecraft.src.EntityCreeper;
import net.minecraft.src.EntityFish;
import net.minecraft.src.EntityGhast;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPig;
import net.minecraft.src.EntityPigZombie;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntitySheep;
import net.minecraft.src.EntitySkeleton;
import net.minecraft.src.EntitySlime;
import net.minecraft.src.EntitySpider;
import net.minecraft.src.EntitySquid;
import net.minecraft.src.EntityWolf;
import net.minecraft.src.EntityZombie;

/** Stable type strings for mapped b1.7.3 entities. */
final class B173Types {
    private B173Types() {}

    static String of(Entity entity) {
        if (entity instanceof EntityPlayer) return "minecraft:player";
        if (entity instanceof EntityPigZombie) return "minecraft:pig-zombie";
        if (entity instanceof EntityZombie) return "minecraft:zombie";
        if (entity instanceof EntitySkeleton) return "minecraft:skeleton";
        if (entity instanceof EntityCreeper) return "minecraft:creeper";
        if (entity instanceof EntitySpider) return "minecraft:spider";
        if (entity instanceof EntitySlime) return "minecraft:slime";
        if (entity instanceof EntitySheep) return "minecraft:sheep";
        if (entity instanceof EntityPig) return "minecraft:pig";
        if (entity instanceof EntityCow) return "minecraft:cow";
        if (entity instanceof EntityChicken) return "minecraft:chicken";
        if (entity instanceof EntitySquid) return "minecraft:squid";
        if (entity instanceof EntityWolf) return "minecraft:wolf";
        if (entity instanceof EntityGhast) return "minecraft:ghast";
        if (entity instanceof EntityFish) return "minecraft:fish-hook";
        if (entity instanceof EntityItem) return "minecraft:item";
        if (entity instanceof EntityLiving) return "minecraft:mob";
        return named(entity.getClass().getSimpleName());
    }

    private static String named(String simple) {
        if ("EntityFallingSand".equals(simple)) return "minecraft:falling-block";
        if ("EntityTNTPrimed".equals(simple)) return "minecraft:tnt";
        if ("EntityArrow".equals(simple)) return "minecraft:arrow";
        if ("EntitySnowball".equals(simple)) return "minecraft:snowball";
        if ("EntityEgg".equals(simple)) return "minecraft:egg";
        if ("EntityBoat".equals(simple)) return "minecraft:boat";
        if ("EntityMinecart".equals(simple)) return "minecraft:minecart";
        if ("EntityPainting".equals(simple)) return "minecraft:painting";
        if ("EntityFireball".equals(simple)) return "minecraft:fireball";
        if ("EntityLightningBolt".equals(simple)) return "minecraft:lightning";
        return "worldline:unknown";
    }
}
