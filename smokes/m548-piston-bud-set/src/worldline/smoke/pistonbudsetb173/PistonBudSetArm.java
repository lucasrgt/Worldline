package worldline.smoke.pistonbudsetb173;

import java.nio.charset.StandardCharsets;import java.security.MessageDigest;
import worldline.api.*;import worldline.b173server.*;

/** Cloned west-facing piston-33 arm whose payload torch place is one self-clearing BUD pulse. */
public final class PistonBudSetArm{
 final BlockPosition support,piston,head,pushed,torch;BlockState pulsePiston,pulseHead;
 private PistonBudSetArm(BlockPosition s,BlockPosition p,BlockPosition h,BlockPosition u,BlockPosition t){support=s;piston=p;head=h;pushed=u;torch=t;}
 static PistonBudSetArm place(B173WireClient a,RemoteChunkSnapshot initial,BlockPosition support,int cx,int cz)throws Exception{
  BlockPosition piston=BlockFace.UP.adjacent(support),head=BlockFace.WEST.adjacent(piston),pushed=BlockFace.WEST.adjacent(head),torch=BlockFace.UP.adjacent(head);
  require(at(initial,piston,cx,cz).legacyId()==0&&at(initial,head,cx,cz).legacyId()==0&&at(initial,pushed,cx,cz).legacyId()==0&&at(initial,torch,cx,cz).legacyId()==0,"BUD targets were not initial air");
  a.look(-90F,0F);a.selectHeldSlot(1);a.placeHeldBlock(support,BlockFace.UP);BlockState placed=worldline.test.WorldlineSmokeAwait.awaitBlock(a,piston,new BlockState(33,4),5).blockAt(piston.x(),piston.y(),piston.z());
  a.selectHeldSlot(0);a.placeHeldBlock(piston,BlockFace.WEST);a.awaitBlock(head,new BlockState(1,0));
  return new PistonBudSetArm(support,piston,head,pushed,torch);
 }
 RemoteWorldView pulse(B173WireClient a,int ticks)throws Exception{
  PlayerPose pose=a.moveAndObserve(0D,0D,0D,1).resulting();
  pose=a.moveAndObserve(piston.x()+0.5D-pose.x(),piston.y()+2D-pose.y(),piston.z()+0.5D-pose.z(),2).resulting();
  a.look(-90F,45F);a.selectHeldSlot(2);a.placeHeldBlock(head,BlockFace.UP);
  RemoteWorldView live=worldline.test.WorldlineSmokeAwait.awaitWorld(a,v->moving(v),"BUD extension",Math.max(ticks,24));pulsePiston=live.blockAt(piston.x(),piston.y(),piston.z());pulseHead=live.blockAt(head.x(),head.y(),head.z());
  live=worldline.test.WorldlineSmokeAwait.awaitWorld(a,v->retracted(v),"BUD retraction",Math.max(ticks,24));
  require(live.blockAt(piston.x(),piston.y(),piston.z()).equals(new BlockState(33,4))&&live.blockAt(head.x(),head.y(),head.z()).equals(new BlockState(0,0))&&live.blockAt(pushed.x(),pushed.y(),pushed.z()).equals(new BlockState(1,0))&&live.blockAt(torch.x(),torch.y(),torch.z()).equals(new BlockState(0,0)),"BUD did not pulse and retract (M546 remaining-on or no self-clear): "+state(live));
  return live;
 }
 void charged(RemoteWorldView v){require(v.blockAt(piston.x(),piston.y(),piston.z()).equals(new BlockState(33,4))&&v.blockAt(head.x(),head.y(),head.z()).equals(new BlockState(1,0))&&v.blockAt(pushed.x(),pushed.y(),pushed.z()).equals(new BlockState(0,0))&&v.blockAt(torch.x(),torch.y(),torch.z()).equals(new BlockState(0,0)),"BUD precondition drift (already moved or QC remaining-on): "+state(v));}
 boolean moving(RemoteWorldView v){BlockState p=v.blockAt(piston.x(),piston.y(),piston.z()),h=v.blockAt(head.x(),head.y(),head.z());return p.legacyId()==36||p.equals(new BlockState(33,12))||h.legacyId()==34;}
 boolean retracted(RemoteWorldView v){return v.blockAt(piston.x(),piston.y(),piston.z()).equals(new BlockState(33,4))&&v.blockAt(pushed.x(),pushed.y(),pushed.z()).equals(new BlockState(1,0));}
 void persist(RemoteChunkSnapshot after,int cx,int cz){require(at(after,piston,cx,cz).equals(new BlockState(33,4))&&at(after,head,cx,cz).equals(new BlockState(0,0))&&at(after,pushed,cx,cz).equals(new BlockState(1,0))&&at(after,torch,cx,cz).equals(new BlockState(0,0)),"fresh BUD pulse drift");}
 static BlockPosition raise(B173WireClient a,RemoteChunkSnapshot initial,int cx,int cz,int[] column)throws Exception{
  BlockPosition top=foundation(initial,cx,cz);column[0]=0;a.selectHeldSlot(0);
  while(water(at(initial,BlockFace.UP.adjacent(top),cx,cz).legacyId())){top=place(a,top,BlockFace.UP,1);a.moveAndObserve(0D,1D,0D,1);require(++column[0]<=15,"water column exceeded piston-BUD fixture");}
  top=place(a,top,BlockFace.UP,1);a.moveAndObserve(0D,1D,2D,1);column[0]++;return top;
 }
 static BlockPosition place(B173WireClient a,BlockPosition support,BlockFace face,int id)throws Exception{BlockPosition target=face.adjacent(support);a.placeHeldBlock(support,face);a.awaitBlock(target,new BlockState(id,0));return target;}
 static BlockPosition foundation(RemoteChunkSnapshot c,int cx,int cz){for(int x=4;x<=11;x++)for(int z=4;z<=11;z++)for(int y=126;y>=1;y--)if(c.blockAt(x,y,z).legacyId()==3&&water(c.blockAt(x,y+1,z).legacyId()))return new BlockPosition(cx*16+x,y,cz*16+z);throw new IllegalStateException("no deterministic piston-BUD foundation");}
 static BlockState at(RemoteChunkSnapshot c,BlockPosition p,int cx,int cz){return c.blockAt(p.x()-cx*16,p.y(),p.z()-cz*16);}
 static boolean water(int id){return id==8||id==9;}
 static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}
 static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}
 static String cell(BlockPosition p){return p.x()+":"+p.y()+":"+p.z();}
 String state(RemoteWorldView v){if(v==null)return "null";return "piston="+v.blockAt(piston.x(),piston.y(),piston.z())+" head="+v.blockAt(head.x(),head.y(),head.z())+" pushed="+v.blockAt(pushed.x(),pushed.y(),pushed.z())+" torch="+v.blockAt(torch.x(),torch.y(),torch.z());}
 static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
