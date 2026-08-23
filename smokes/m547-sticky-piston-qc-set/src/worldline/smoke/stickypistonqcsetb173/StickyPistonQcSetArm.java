package worldline.smoke.stickypistonqcsetb173;

import java.nio.charset.StandardCharsets;import java.security.MessageDigest;
import worldline.api.*;import worldline.b173server.*;

/** West sticky 29 arm with the lever on the stone ABOVE the piston, not on the piston cell. */
public final class StickyPistonQcSetArm{
 final BlockPosition support,piston,head,pushed,qc,lever;
 private StickyPistonQcSetArm(BlockPosition s,BlockPosition p,BlockPosition h,BlockPosition u,BlockPosition q,BlockPosition l){support=s;piston=p;head=h;pushed=u;qc=q;lever=l;}
 static StickyPistonQcSetArm place(B173WireClient a,RemoteChunkSnapshot initial,BlockPosition support,int cx,int cz)throws Exception{
  BlockPosition piston=BlockFace.UP.adjacent(support),head=BlockFace.WEST.adjacent(piston),pushed=BlockFace.WEST.adjacent(head),qc=BlockFace.UP.adjacent(piston),lever=BlockFace.EAST.adjacent(qc);
  require(at(initial,piston,cx,cz).legacyId()==0&&at(initial,head,cx,cz).legacyId()==0&&at(initial,pushed,cx,cz).legacyId()==0&&at(initial,qc,cx,cz).legacyId()==0&&at(initial,lever,cx,cz).legacyId()==0,"sticky qc targets were not initial air");
  require(lever.y()==piston.y()+1&&lever.y()!=support.y(),"qc lever must power the block above, not the M144/M367 support cell");
  a.look(-90F,0F);a.selectHeldSlot(1);a.placeHeldBlock(support,BlockFace.UP);BlockState placed=worldline.test.WorldlineSmokeAwait.awaitBlock(a,piston,new BlockState(29,4),5).blockAt(piston.x(),piston.y(),piston.z());require(placed.equals(new BlockState(29,4)),"west sticky 29 absent: "+placed+" at "+cell(piston));
  a.selectHeldSlot(0);a.placeHeldBlock(piston,BlockFace.WEST);a.awaitBlock(head,new BlockState(1,0));
  a.placeHeldBlock(piston,BlockFace.UP);a.awaitBlock(qc,new BlockState(1,0));
  a.selectHeldSlot(2);a.placeHeldBlock(qc,BlockFace.EAST);require(worldline.test.WorldlineSmokeAwait.awaitBlock(a,lever,new BlockState(69,1),5).blockAt(lever.x(),lever.y(),lever.z()).equals(new BlockState(69,1)),"qc lever absent above piston");
  return new StickyPistonQcSetArm(support,piston,head,pushed,qc,lever);
 }
 RemoteWorldView pulse(B173WireClient a,int ticks,BlockState pistonWant,BlockState headWant,BlockState pushedWant,int leverMeta,String label)throws Exception{
  a.activateBlock(lever,BlockFace.UP);RemoteWorldView live=worldline.test.WorldlineSmokeAwait.observe(a,ticks);
  require(live.blockAt(qc.x(),qc.y(),qc.z()).equals(new BlockState(1,0))&&live.blockAt(lever.x(),lever.y(),lever.z()).equals(new BlockState(69,leverMeta))&&live.blockAt(piston.x(),piston.y(),piston.z()).equals(pistonWant)&&live.blockAt(head.x(),head.y(),head.z()).equals(headWant)&&live.blockAt(pushed.x(),pushed.y(),pushed.z()).equals(pushedWant),label+" absent: "+live.blockAt(piston.x(),piston.y(),piston.z())+"/"+live.blockAt(head.x(),head.y(),head.z())+"/"+live.blockAt(pushed.x(),pushed.y(),pushed.z()));
  return live;
 }
 void persist(RemoteChunkSnapshot after,int cx,int cz,BlockState pistonWant,BlockState headWant,BlockState pushedWant,String label){
  require(at(after,qc,cx,cz).equals(new BlockState(1,0))&&at(after,lever,cx,cz).equals(new BlockState(69,1))&&at(after,piston,cx,cz).equals(pistonWant)&&at(after,head,cx,cz).equals(headWant)&&at(after,pushed,cx,cz).equals(pushedWant),label);
 }
 static BlockPosition raise(B173WireClient a,RemoteChunkSnapshot initial,int cx,int cz,int[] column)throws Exception{
  BlockPosition top=foundation(initial,cx,cz);column[0]=0;a.selectHeldSlot(0);
  while(water(at(initial,BlockFace.UP.adjacent(top),cx,cz).legacyId())){top=place(a,top,BlockFace.UP,1);a.moveAndObserve(0D,1D,0D,1);require(++column[0]<=15,"water column exceeded sticky-qc fixture");}
  top=place(a,top,BlockFace.UP,1);a.moveAndObserve(0D,1D,2D,1);column[0]++;return top;
 }
 static BlockPosition place(B173WireClient a,BlockPosition support,BlockFace face,int id)throws Exception{BlockPosition target=face.adjacent(support);a.placeHeldBlock(support,face);a.awaitBlock(target,new BlockState(id,0));return target;}
 static BlockPosition foundation(RemoteChunkSnapshot c,int cx,int cz){for(int x=4;x<=11;x++)for(int z=4;z<=11;z++)for(int y=126;y>=1;y--)if(c.blockAt(x,y,z).legacyId()==3&&water(c.blockAt(x,y+1,z).legacyId()))return new BlockPosition(cx*16+x,y,cz*16+z);throw new IllegalStateException("no deterministic sticky-qc foundation");}
 static BlockState at(RemoteChunkSnapshot c,BlockPosition p,int cx,int cz){return c.blockAt(p.x()-cx*16,p.y(),p.z()-cz*16);}
 static boolean water(int id){return id==8||id==9;}
 static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}
 static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}
 static String cell(BlockPosition p){return p.x()+":"+p.y()+":"+p.z();}
 static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
