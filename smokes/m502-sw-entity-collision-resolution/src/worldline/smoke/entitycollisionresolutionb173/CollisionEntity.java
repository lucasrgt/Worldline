package worldline.smoke.entitycollisionresolutionb173;

import net.minecraft.src.EntityLiving;
import net.minecraft.src.World;

/** Concrete fixture entity with vanilla living-entity update and collision behavior. */
final class CollisionEntity extends EntityLiving {
  CollisionEntity(World world) {
    super(world);
    health = 10;
  }

  @Override
  protected void updatePlayerActionState() {
  }
}
