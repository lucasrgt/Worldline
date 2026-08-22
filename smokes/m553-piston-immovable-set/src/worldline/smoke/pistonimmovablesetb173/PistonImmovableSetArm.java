package worldline.smoke.pistonimmovablesetb173;

import java.nio.charset.StandardCharsets;import java.security.MessageDigest;
import worldline.api.*;import worldline.b173server.*;

/** Cloned west-facing piston-33 arm with one immovable payload from the M146 family. */
public final class PistonImmovableSetArm{
 final BlockPosition support,piston,payload,destination,lever;final BlockState payloadState;
 private PistonImmovableSetArm(BlockPosition s,BlockPosition p,BlockPosition u,BlockPosition d,BlockPosition l,BlockState payloadState){support=s;piston=p;payload=u;destination=d;lever=l;this.payloadState=payloadState;}
 static PistonImmovableSetArm place(B173WireClient a,RemoteChunkSnapshot initial,BlockPosition support,int cx,int cz,int pistonSlot,int payloadSlot,int payloadId,int leverSlot)throws Exception{
  BlockPosition piston=BlockFace.UP.adjacent(support),payload=BlockFace.WEST.adjacent(piston),destination=BlockFace.WEST.adjacent(payload),lever=BlockFace.EAST.adjacent(support);
  require(at(initial,piston,cx,cz).legacyId()==0&&at(initial,payload,cx,cz).legacyId()==0&&at(initial,destination,cx,cz).legacyId()==0&&at(initial,lever,cx,cz).legacyId()==0,"piston 33 payload "+payloadId+" targets were not initial air");
  a.look(-90F,0F);a.selectHeldSlot(pistonSlot);a.placeHeldBlock(support,BlockFace.UP);BlockState placed=a.sustainTicks(5).blockAt(piston.x(),piston.y(),piston.z());require(placed.equals(new BlockState(33,4)),"west piston 33 absent: "+placed+" at "+piston.x()+":"+piston.y()+":"+piston.z());
  a.selectHeldSlot(payloadSlot);a.placeHeldBlock(piston,BlockFace.WEST);BlockState payloadState=a.sustainTicks(5).blockAt(payload.x(),payload.y(),payload.z());require(payloadState.legacyId()==payloadId,"immovable payload "+payloadId+" absent: "+payloadState+" at "+payload.x()+":"+payload.y()+":"+payload.z());
  a.selectHeldSlot(leverSlot);a.placeHeldBlock(support,BlockFace.EAST);require(a.sustainTicks(5).blockAt(lever.x(),lever.y(),lever.z()).equals(new BlockState(69,1)),"lever absent for payload "+payloadId);
  return new PistonImmovableSetArm(support,piston,payload,destination,lever,payloadState);
 }
 RemoteWorldView pulse(B173WireClient a,int ticks)throws Exception{
  a.activateBlock(lever,BlockFace.UP);RemoteWorldView live=a.sustainTicks(ticks);
  BlockState pistonLive=live.blockAt(piston.x(),piston.y(),piston.z()),payloadLive=live.blockAt(payload.x(),payload.y(),payload.z()),destLive=live.blockAt(destination.x(),destination.y(),destination.z());
  require(live.blockAt(lever.x(),lever.y(),lever.z()).equals(new BlockState(69,9))&&pistonLive.equals(new BlockState(33,4))&&payloadLive.equals(payloadState)&&destLive.equals(new BlockState(0,0)),"immovable "+payloadState.legacyId()+" rejection absent: lever="+live.blockAt(lever.x(),lever.y(),lever.z())+"/"+pistonLive+"/"+payloadLive+"/"+destLive);
  return live;
 }
 void persist(RemoteChunkSnapshot after,int cx,int cz){
  require(at(after,lever,cx,cz).equals(new BlockState(69,9))&&at(after,piston,cx,cz).equals(new BlockState(33,4))&&at(after,payload,cx,cz).equals(payloadState)&&at(after,destination,cx,cz).equals(new BlockState(0,0)),"fresh immovable "+payloadState.legacyId()+" rejection drift");
 }
 String token(){return payloadState.legacyId()+":"+payloadState.metadata()+"->"+payloadState.legacyId()+":"+payloadState.metadata();}
 String arm(){return cell(piston)+":33:4->4,payload="+cell(payload)+":"+token()+",dest="+cell(destination)+":0:0->0:0";}
 static BlockPosition raise(B173WireClient a,RemoteChunkSnapshot initial,int cx,int cz,int[] column)throws Exception{
  BlockPosition top=foundation(initial,cx,cz);column[0]=0;a.selectHeldSlot(0);
  while(water(at(initial,BlockFace.UP.adjacent(top),cx,cz).legacyId())){top=place(a,top,BlockFace.UP,1);a.moveAndObserve(0D,1D,0D,1);require(++column[0]<=15,"water column exceeded piston-immovable fixture");}
  top=place(a,top,BlockFace.UP,1);a.moveAndObserve(0D,1D,2D,1);column[0]++;return top;
 }
 static BlockPosition padSouth(B173WireClient a,BlockPosition from)throws Exception{
  a.selectHeldSlot(0);BlockPosition support=place(a,from,BlockFace.SOUTH,1);support=place(a,support,BlockFace.SOUTH,1);a.moveAndObserve(0D,0D,2D,2);return support;
 }
 static BlockPosition place(B173WireClient a,BlockPosition support,BlockFace face,int id)throws Exception{BlockPosition target=face.adjacent(support);a.placeHeldBlock(support,face);a.awaitBlock(target,new BlockState(id,0));return target;}
 static BlockPosition foundation(RemoteChunkSnapshot c,int cx,int cz){for(int x=4;x<=11;x++)for(int z=4;z<=11;z++)for(int y=126;y>=1;y--)if(c.blockAt(x,y,z).legacyId()==3&&water(c.blockAt(x,y+1,z).legacyId()))return new BlockPosition(cx*16+x,y,cz*16+z);throw new IllegalStateException("no deterministic piston-immovable foundation");}
 static BlockState at(RemoteChunkSnapshot c,BlockPosition p,int cx,int cz){return c.blockAt(p.x()-cx*16,p.y(),p.z()-cz*16);}
 static boolean water(int id){return id==8||id==9;}
 static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}
 static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}
 static String cell(BlockPosition p){return p.x()+":"+p.y()+":"+p.z();}
 static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
