package worldline.smoke.redstoneloop;
import net.minecraft.src.*;import worldline.api.WorldSource;import worldline.kernel.GameBackend;import worldline.trace.CanonicalTrace;
final class RedstoneLoopBackend implements GameBackend{
 private static final int Y=65;private static final int[][]MAIN={{9,8},{10,8},{11,8},{11,9},{11,10},{10,10},{9,10},{9,9}},CONTROL={{10,12},{11,12},{11,13},{11,14},{10,14},{9,14},{9,13}};private final long seed;private World world;RedstoneLoopBackend(long s){seed=s;}public void bootHeadless(){System.setProperty("java.awt.headless","true");}
 public void loadWorld(WorldSource source){String n=source.path().getFileName().toString();world=new World(new MemorySaveHandler(seed,n),n,seed,null);for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)world.getChunkFromChunkCoords(x,z);}
 public void tick(){requireWorld().tick();}public void close(){world=null;}
 void power(){World w=requireWorld();require(w.setBlockAndMetadataWithNotify(8,Y,8,Block.torchRedstoneActive.blockID,5),"torch placement failed");for(int[]p:MAIN)wire(p);for(int[]p:CONTROL)wire(p);}
 void removeSource(){require(requireWorld().setBlockWithNotify(8,Y,8,0),"source removal failed");}
 void snapshot(CanonicalTrace t,String label){World w=requireWorld();int[]v=new int[17];v[0]=w.getBlockId(8,Y,8);for(int i=0;i<8;i++)v[i+1]=w.getBlockMetadata(MAIN[i][0],Y,MAIN[i][1]);for(int i=0;i<7;i++)v[i+9]=w.getBlockMetadata(CONTROL[i][0],Y,CONTROL[i][1]);v[16]=w.getBlockId(9,Y,12);t.record(label,w.getWorldTime(),w.loadedEntityList.size(),v);}
 void assertFinal(){World w=requireWorld();for(int[]p:MAIN)require(w.getBlockMetadata(p[0],Y,p[1])==0,"main loop retained power");for(int[]p:CONTROL)require(w.getBlockMetadata(p[0],Y,p[1])==0,"control gained power");require(w.getBlockId(9,Y,12)==0,"control gap closed");}
 private void wire(int[]p){require(requireWorld().setBlockWithNotify(p[0],Y,p[1],Block.redstoneWire.blockID),"wire placement failed");}private World requireWorld(){if(world==null)throw new IllegalStateException("world absent");return world;}private static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
