package worldline.smoke.redstoneore;
import net.minecraft.src.*;import worldline.api.WorldSource;import worldline.kernel.GameBackend;import worldline.trace.CanonicalTrace;
/** Bridges the runtime to a clicked/untouched redstone-ore random-tick fixture. */
@SuppressWarnings({"rawtypes","unchecked"}) final class RedstoneOreBackend implements GameBackend{
 private static final int X=8,Y=65,Z=8,NX=9;private final long seed;private DeterministicWorld world;private EntityPlayer player;
 RedstoneOreBackend(long seed){this.seed=seed;}public void bootHeadless(){System.setProperty("java.awt.headless","true");}
 public void loadWorld(WorldSource source){String n=source.path().getFileName().toString();world=new DeterministicWorld(new MemorySaveHandler(seed,n),n,seed);for(int x=-9;x<=9;x++)for(int z=-9;z<=9;z++)world.getChunkFromChunkCoords(x,z);player=new EntityPlayer(world){};player.setLocationAndAngles(8.5D,65D,8.5D,0F,0F);world.playerEntities.add(player);world.freezeRandom(seed);require(world.getBlockId(X,64,Z)==Block.stone.blockID,"fixture stone missing");}
 public void tick(){((World)requireWorld()).tick();}public void close(){world=null;player=null;}
 void seed(){World w=requireWorld();require(w.setBlockWithNotify(X,Y,Z,Block.oreRedstone.blockID)&&w.setBlockWithNotify(NX,Y,Z,Block.oreRedstone.blockID),"ore fixture placement failed");}
 void trigger(){Block.oreRedstone.onBlockClicked(requireWorld(),X,Y,Z,player);require(requireWorld().getBlockId(X,Y,Z)==Block.oreRedstoneGlowing.blockID,"ore did not activate");}
 boolean reverted(){return requireWorld().getBlockId(X,Y,Z)==Block.oreRedstone.blockID;}
 void snapshot(CanonicalTrace t,String label,int tick){World w=requireWorld();t.record(label,w.getWorldTime(),w.loadedEntityList.size(),tick,w.getBlockId(X,Y,Z),w.getBlockMetadata(X,Y,Z),w.getBlockId(NX,Y,Z),w.getBlockMetadata(NX,Y,Z));}
 void assertFinal(){World w=requireWorld();require(reverted()&&w.getBlockId(NX,Y,Z)==Block.oreRedstone.blockID,"ore reversion/control drifted");}
 private DeterministicWorld requireWorld(){if(world==null)throw new IllegalStateException("world absent");return world;}private static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
