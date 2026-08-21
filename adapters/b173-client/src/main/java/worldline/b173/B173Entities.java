package worldline.b173;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityChicken;
import net.minecraft.src.EntityCow;
import net.minecraft.src.EntityCreeper;
import net.minecraft.src.EntityPig;
import net.minecraft.src.EntitySheep;
import net.minecraft.src.EntitySkeleton;
import net.minecraft.src.EntitySlime;
import net.minecraft.src.EntitySpider;
import net.minecraft.src.EntitySquid;
import net.minecraft.src.EntityWolf;
import net.minecraft.src.EntityZombie;
import net.minecraft.src.IInventory;
import net.minecraft.src.World;
import worldline.api.BlockPosition;
import worldline.api.GameEntity;
import worldline.api.GamePosition;
import worldline.api.ItemCensus;

/** Bounded semantic spawn table, entity removal, and container reads. */
final class B173Entities {
    private static final Map<String, Factory> TYPES = new HashMap<>();
    static {
        TYPES.put("minecraft:pig", EntityPig::new);
        TYPES.put("minecraft:cow", EntityCow::new);
        TYPES.put("minecraft:sheep", EntitySheep::new);
        TYPES.put("minecraft:chicken", EntityChicken::new);
        TYPES.put("minecraft:zombie", EntityZombie::new);
        TYPES.put("minecraft:skeleton", EntitySkeleton::new);
        TYPES.put("minecraft:creeper", EntityCreeper::new);
        TYPES.put("minecraft:spider", EntitySpider::new);
        TYPES.put("minecraft:slime", EntitySlime::new);
        TYPES.put("minecraft:squid", EntitySquid::new);
        TYPES.put("minecraft:wolf", EntityWolf::new);
    }

    private interface Factory { Entity create(World world); }

    private B173Entities() {}

    static GameEntity spawn(B173ClientBackend owner, World world, String type,
            GamePosition position) {
        if (type == null) throw new NullPointerException("type");
        if (position == null) throw new NullPointerException("position");
        Factory factory = TYPES.get(type);
        if (factory == null) throw new IllegalArgumentException("unregistered spawn type: " + type);
        Entity entity = factory.create(world);
        entity.setPosition(position.x(), position.y(), position.z());
        if (!world.entityJoinedWorld(entity)) {
            throw new IllegalStateException("spawn was rejected for " + type);
        }
        return new B173Entity(owner, entity);
    }

    static boolean remove(World world, GameEntity handle) {
        if (!(handle instanceof B173Entity)) throw new IllegalArgumentException("foreign entity handle");
        Entity entity = ((B173Entity) handle).entity;
        if (entity.isDead) return false;
        world.setEntityDead(entity);
        return true;
    }

    static ItemCensus itemsAt(World world, BlockPosition position) {
        if (position == null) throw new NullPointerException("position");
        if (!(world.getBlockTileEntity(position.x(), position.y(), position.z())
                instanceof IInventory)) return ItemCensus.empty();
        return B173Items.add(ItemCensus.empty(), (IInventory) world
                .getBlockTileEntity(position.x(), position.y(), position.z()));
    }
}
