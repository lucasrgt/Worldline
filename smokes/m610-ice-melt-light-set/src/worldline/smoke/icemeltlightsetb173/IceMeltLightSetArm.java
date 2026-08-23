package worldline.smoke.icemeltlightsetb173;

import java.nio.charset.StandardCharsets;import java.security.MessageDigest;
import worldline.api.*;import worldline.b173server.*;

/** Raised-stone ice-and-torch pad and bounded block-light melt wait. */
public final class IceMeltLightSetArm{
 static final BlockState ICE=new BlockState(79,0),STONE=new BlockState(1,0),TORCH=new BlockState(50,5);
 private IceMeltLightSetArm(){}
 static BlockPosition raise(B173WireClient a,RemoteChunkSnapshot initial,int cx,int cz,int[] column)throws Exception{
  BlockPosition top=foundation(initial,cx,cz);column[0]=0;a.selectHeldSlot(0);
  while(water(at(initial,BlockFace.UP.adjacent(top),cx,cz).legacyId())){top=place(a,top,BlockFace.UP,1);a.moveAndObserve(0D,1D,0D,1);require(++column[0]<=15,"water column exceeded ice-melt-light fixture");}
  for(int lift=0;lift<8;lift++){top=place(a,top,BlockFace.UP,1);a.moveAndObserve(0D,1D,0D,1);column[0]++;}
  a.moveAndObserve(0D,1D,0D,1);a.moveAndObserve(0D,1D,0D,1);return top;
 }
 static BlockPosition place(B173WireClient a,BlockPosition support,BlockFace face,int id)throws Exception{BlockPosition target=face.adjacent(support);a.placeHeldBlock(support,face);a.awaitBlock(target,new BlockState(id,0));return target;}
 static BlockPosition foundation(RemoteChunkSnapshot c,int cx,int cz){for(int x=4;x<=11;x++)for(int z=4;z<=11;z++)for(int y=126;y>=1;y--)if(c.blockAt(x,y,z).legacyId()==3&&water(c.blockAt(x,y+1,z).legacyId()))return new BlockPosition(cx*16+x,y,cz*16+z);throw new IllegalStateException("no deterministic ice-melt-light foundation");}
 static BlockState persist(RemoteChunkSnapshot after,int cx,int cz,BlockPosition top,BlockPosition east,BlockPosition ice,BlockPosition torch){
  require(at(after,top,cx,cz).equals(STONE)&&at(after,east,cx,cz).equals(STONE)&&at(after,torch,cx,cz).equals(TORCH),"persisted ice-melt-light leftover drift");
  BlockState leftover=at(after,ice,cx,cz);require(water(leftover.legacyId()),"BLOCKED ice did not melt cell="+leftover);return leftover;
 }
 static void waitMelt(B173WireClient a,BlockPosition ice,BlockPosition torch,int window,int windows)throws Exception{
  require(worldline.test.WorldlineSmokeAwait.observe(a,5).blockAt(ice.x(),ice.y(),ice.z()).equals(ICE),"live ice vanished before torch wait");
  worldline.test.WorldlineSmokeAwait.awaitWorld(a,v->water(v.blockAt(ice.x(),ice.y(),ice.z()).legacyId()),"torch-adjacent ice melt",windows*window);
  RemoteWorldView end=worldline.test.WorldlineSmokeAwait.observe(a,20);require(end.blockAt(torch.x(),torch.y(),torch.z()).equals(TORCH),"torch extinguished during melt wait");
 }
 static BlockState at(RemoteChunkSnapshot c,BlockPosition p,int cx,int cz){return c.blockAt(p.x()-cx*16,p.y(),p.z()-cz*16);}
 static boolean water(int id){return id==8||id==9;}
 static void awaitPlayers(B173DedicatedServer s,int n){new worldline.test.WorldlineAwait(50).awaitEntity(s::players,p->p.size()==n,"player count");}
 static String cell(BlockPosition p,int id,int meta){return p.x()+":"+p.y()+":"+p.z()+":"+id+":"+meta;}
 static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}
 static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
