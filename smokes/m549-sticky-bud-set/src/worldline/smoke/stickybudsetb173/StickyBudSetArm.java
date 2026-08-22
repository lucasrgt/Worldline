package worldline.smoke.stickybudsetb173;

import java.nio.charset.StandardCharsets;import java.security.MessageDigest;
import worldline.api.*;import worldline.b173server.*;

/** West sticky 29 primed by a diagonal-above lever-powered block, then a neighbor update extends. */
public final class StickyBudSetArm{
 final BlockPosition support,piston,head,pushed,powered,lever,update;final int leverOff,leverOn;
 private StickyBudSetArm(BlockPosition s,BlockPosition p,BlockPosition h,BlockPosition u,BlockPosition pw,BlockPosition l,BlockPosition n,int off,int on){support=s;piston=p;head=h;pushed=u;powered=pw;lever=l;update=n;leverOff=off;leverOn=on;}
 static StickyBudSetArm place(B173WireClient a,RemoteChunkSnapshot initial,BlockPosition support,int cx,int cz)throws Exception{
  BlockPosition piston=BlockFace.UP.adjacent(support),head=BlockFace.WEST.adjacent(piston),pushed=BlockFace.WEST.adjacent(head),update=BlockFace.NORTH.adjacent(piston);
  BlockPosition e1=BlockFace.EAST.adjacent(support),e2=BlockFace.EAST.adjacent(e1),e2up=BlockFace.UP.adjacent(e2),e2up2=BlockFace.UP.adjacent(e2up),powered=BlockFace.WEST.adjacent(e2up2),lever=BlockFace.NORTH.adjacent(powered);
  require(air(initial,cx,cz,piston,head,pushed,update,e1,e2,e2up,e2up2,powered,lever),"sticky BUD targets were not initial air");
  require(manhattan(piston,lever)>1&&manhattan(piston,powered)>1,"BUD lever and powered block must not touch the piston cell");
  a.look(-90F,0F);a.selectHeldSlot(1);a.placeHeldBlock(support,BlockFace.UP);BlockState placed=a.sustainTicks(5).blockAt(piston.x(),piston.y(),piston.z());require(placed.equals(new BlockState(29,4)),"west sticky 29 absent: "+placed+" at "+cell(piston));
  a.selectHeldSlot(0);a.placeHeldBlock(piston,BlockFace.WEST);a.awaitBlock(head,new BlockState(1,0));
  a.placeHeldBlock(support,BlockFace.EAST);a.awaitBlock(e1,new BlockState(1,0));a.placeHeldBlock(e1,BlockFace.EAST);a.awaitBlock(e2,new BlockState(1,0));
  a.placeHeldBlock(e2,BlockFace.UP);a.awaitBlock(e2up,new BlockState(1,0));a.placeHeldBlock(e2up,BlockFace.UP);a.awaitBlock(e2up2,new BlockState(1,0));
  a.placeHeldBlock(e2up2,BlockFace.WEST);a.awaitBlock(powered,new BlockState(1,0));
  a.selectHeldSlot(2);a.placeHeldBlock(powered,BlockFace.NORTH);RemoteWorldView off=a.sustainTicks(5);BlockState leverPlaced=off.blockAt(lever.x(),lever.y(),lever.z());require(leverOff(leverPlaced),"north lever absent: "+leverPlaced);
  a.selectHeldSlot(4);a.activateBlock(lever,BlockFace.UP);RemoteWorldView on=a.sustainTicks(5);BlockState leverPowered=on.blockAt(lever.x(),lever.y(),lever.z());require(leverOn(leverPowered)&&on.blockAt(piston.x(),piston.y(),piston.z()).equals(new BlockState(29,4)),"sticky 29 QC-extended on diagonal lever: "+on.blockAt(piston.x(),piston.y(),piston.z())+" lever="+leverPowered);
  return new StickyBudSetArm(support,piston,head,pushed,powered,lever,update,leverPlaced.metadata(),leverPowered.metadata());
 }
 RemoteWorldView primed(B173WireClient a,int ticks)throws Exception{
  RemoteWorldView live=a.sustainTicks(ticks);
  require(live.blockAt(lever.x(),lever.y(),lever.z()).equals(new BlockState(69,leverOn))&&live.blockAt(powered.x(),powered.y(),powered.z()).equals(new BlockState(1,0))&&live.blockAt(piston.x(),piston.y(),piston.z()).equals(new BlockState(29,4))&&live.blockAt(head.x(),head.y(),head.z()).equals(new BlockState(1,0))&&live.blockAt(pushed.x(),pushed.y(),pushed.z()).equals(new BlockState(0,0))&&live.blockAt(update.x(),update.y(),update.z()).equals(new BlockState(0,0))&&!directPower(live,piston),"sticky 29 BUD primed drift: "+live.blockAt(piston.x(),piston.y(),piston.z())+" lever="+live.blockAt(lever.x(),lever.y(),lever.z()));
  return live;
 }
 RemoteWorldView neighborExtend(B173WireClient a,int ticks)throws Exception{
  a.selectHeldSlot(0);a.placeHeldBlock(piston,BlockFace.NORTH);a.awaitBlock(update,new BlockState(1,0));RemoteWorldView live=a.sustainTicks(ticks);
  require(live.blockAt(update.x(),update.y(),update.z()).equals(new BlockState(1,0))&&live.blockAt(lever.x(),lever.y(),lever.z()).equals(new BlockState(69,leverOn))&&live.blockAt(piston.x(),piston.y(),piston.z()).equals(new BlockState(29,12))&&live.blockAt(head.x(),head.y(),head.z()).equals(new BlockState(34,12))&&live.blockAt(pushed.x(),pushed.y(),pushed.z()).equals(new BlockState(1,0))&&!directPower(live,piston),"sticky 29 BUD extend absent: "+live.blockAt(piston.x(),piston.y(),piston.z())+"/"+live.blockAt(head.x(),head.y(),head.z())+"/"+live.blockAt(pushed.x(),pushed.y(),pushed.z()));
  return live;
 }
 RemoteWorldView pulsePull(B173WireClient a,int ticks)throws Exception{
  a.selectHeldSlot(4);a.activateBlock(lever,BlockFace.UP);RemoteWorldView live=a.sustainTicks(ticks);
  require(live.blockAt(lever.x(),lever.y(),lever.z()).equals(new BlockState(69,leverOff)),"diagonal lever failed to unpower: "+live.blockAt(lever.x(),lever.y(),lever.z()));
  if(!pulled(live)){a.selectHeldSlot(0);a.placeHeldBlock(piston,BlockFace.SOUTH);a.awaitBlock(BlockFace.SOUTH.adjacent(piston),new BlockState(1,0));live=a.sustainTicks(ticks);}
  require(pulled(live)&&!directPower(live,piston),"sticky 29 BUD pull absent: "+live.blockAt(piston.x(),piston.y(),piston.z())+"/"+live.blockAt(head.x(),head.y(),head.z())+"/"+live.blockAt(pushed.x(),pushed.y(),pushed.z()));
  return live;
 }
 void persist(RemoteChunkSnapshot after,int cx,int cz){
  require(at(after,lever,cx,cz).equals(new BlockState(69,leverOff))&&at(after,piston,cx,cz).equals(new BlockState(29,4))&&at(after,head,cx,cz).equals(new BlockState(1,0))&&at(after,pushed,cx,cz).equals(new BlockState(0,0))&&!directPower(after,piston,cx,cz),"fresh sticky 29 BUD pull drift");
 }
 boolean pulled(RemoteWorldView live){return live.blockAt(piston.x(),piston.y(),piston.z()).equals(new BlockState(29,4))&&live.blockAt(head.x(),head.y(),head.z()).equals(new BlockState(1,0))&&live.blockAt(pushed.x(),pushed.y(),pushed.z()).equals(new BlockState(0,0));}
 static boolean leverOn(BlockState s){return s.legacyId()==69&&(s.metadata()&8)!=0;}
 static boolean leverOff(BlockState s){return s.legacyId()==69&&(s.metadata()&8)==0;}
 static BlockPosition raise(B173WireClient a,RemoteChunkSnapshot initial,int cx,int cz,int[] column)throws Exception{
  BlockPosition top=foundation(initial,cx,cz);column[0]=0;a.selectHeldSlot(0);
  while(water(at(initial,BlockFace.UP.adjacent(top),cx,cz).legacyId())){top=place(a,top,BlockFace.UP,1);a.moveAndObserve(0D,1D,0D,1);require(++column[0]<=15,"water column exceeded sticky-bud fixture");}
  top=place(a,top,BlockFace.UP,1);a.moveAndObserve(0D,1D,2D,1);column[0]++;return top;
 }
 static BlockPosition place(B173WireClient a,BlockPosition support,BlockFace face,int id)throws Exception{BlockPosition target=face.adjacent(support);a.placeHeldBlock(support,face);a.awaitBlock(target,new BlockState(id,0));return target;}
 static BlockPosition foundation(RemoteChunkSnapshot c,int cx,int cz){for(int x=4;x<=11;x++)for(int z=4;z<=11;z++)for(int y=126;y>=1;y--)if(c.blockAt(x,y,z).legacyId()==3&&water(c.blockAt(x,y+1,z).legacyId()))return new BlockPosition(cx*16+x,y,cz*16+z);throw new IllegalStateException("no deterministic sticky-bud foundation");}
 static BlockState at(RemoteChunkSnapshot c,BlockPosition p,int cx,int cz){return c.blockAt(p.x()-cx*16,p.y(),p.z()-cz*16);}
 static boolean air(RemoteChunkSnapshot c,int cx,int cz,BlockPosition... cells){for(BlockPosition p:cells)if(at(c,p,cx,cz).legacyId()!=0)return false;return true;}
 static boolean water(int id){return id==8||id==9;}
 static boolean directPower(RemoteWorldView w,BlockPosition p){int[][]d={{0,-1,0},{0,1,0},{0,0,-1},{0,0,1},{-1,0,0},{1,0,0}};for(int[]v:d){int id=w.blockAt(p.x()+v[0],p.y()+v[1],p.z()+v[2]).legacyId();if(id==55||id==69||id==75||id==76)return true;}return false;}
 static boolean directPower(RemoteChunkSnapshot c,BlockPosition p,int cx,int cz){int[][]d={{0,-1,0},{0,1,0},{0,0,-1},{0,0,1},{-1,0,0},{1,0,0}};for(int[]v:d){int id=at(c,new BlockPosition(p.x()+v[0],p.y()+v[1],p.z()+v[2]),cx,cz).legacyId();if(id==55||id==69||id==75||id==76)return true;}return false;}
 static int manhattan(BlockPosition a,BlockPosition b){return Math.abs(a.x()-b.x())+Math.abs(a.y()-b.y())+Math.abs(a.z()-b.z());}
 static String cell(BlockPosition p){return p.x()+":"+p.y()+":"+p.z();}
 static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}
 static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}
 static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
