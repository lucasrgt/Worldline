package worldline.smoke.onetickpulsesetb173;

import java.nio.charset.StandardCharsets;import java.security.MessageDigest;
import worldline.api.*;import worldline.b173server.*;

/** Raised west piston-33 arm plus 1-tick repeater pulse limiter. */
public final class OneTickPulseSetArm{
 final BlockPosition piston,head,pushed,repeater,lever;
 private OneTickPulseSetArm(BlockPosition p,BlockPosition h,BlockPosition u,BlockPosition r,BlockPosition v){piston=p;head=h;pushed=u;repeater=r;lever=v;}
 static OneTickPulseSetArm place(B173WireClient a,RemoteChunkSnapshot initial,BlockPosition support,int cx,int cz)throws Exception{
  BlockPosition rpad=place(a,support,BlockFace.EAST,1),lpad=place(a,rpad,BlockFace.EAST,1);
  BlockPosition piston=BlockFace.UP.adjacent(support),head=BlockFace.WEST.adjacent(piston),pushed=BlockFace.WEST.adjacent(head);
  BlockPosition repeater=BlockFace.UP.adjacent(rpad),lever=BlockFace.UP.adjacent(lpad);
  require(air(initial,cx,cz,piston,head,pushed,repeater,lever),"one-tick pulse targets were not initial air");
  a.look(-90F,0F);a.sustainTicks(2);a.selectHeldSlot(1);a.placeHeldBlock(support,BlockFace.UP);require(a.sustainTicks(5).blockAt(piston.x(),piston.y(),piston.z()).equals(new BlockState(33,4)),"west piston 33:4 absent");
  a.selectHeldSlot(0);a.placeHeldBlock(piston,BlockFace.WEST);a.awaitBlock(head,new BlockState(1,0));
  a.look(90F,0F);a.sustainTicks(2);a.selectHeldSlot(3);a.useHeldItemOnBlock(rpad,BlockFace.UP);require(a.awaitBlock(repeater,new BlockState(93,3)).blockAt(repeater.x(),repeater.y(),repeater.z()).equals(new BlockState(93,3)),"west 1-tick repeater 93:3 absent");
  a.selectHeldSlot(2);a.placeHeldBlock(lpad,BlockFace.UP);BlockState leverOff=a.sustainTicks(5).blockAt(lever.x(),lever.y(),lever.z());require(leverOff.legacyId()==69&&(leverOff.metadata()&8)==0,"limiter floor lever absent: "+leverOff);
  return new OneTickPulseSetArm(piston,head,pushed,repeater,lever);
 }
 void idle(RemoteWorldView live,String label){require(live.blockAt(piston.x(),piston.y(),piston.z()).equals(new BlockState(33,4))&&live.blockAt(head.x(),head.y(),head.z()).equals(new BlockState(1,0))&&live.blockAt(pushed.x(),pushed.y(),pushed.z()).equals(new BlockState(0,0))&&live.blockAt(lever.x(),lever.y(),lever.z()).legacyId()==69&&(live.blockAt(lever.x(),lever.y(),lever.z()).metadata()&8)==0&&live.blockAt(repeater.x(),repeater.y(),repeater.z()).equals(new BlockState(93,3)),label+": "+cell(live));}
 void dropped(RemoteWorldView live,String label){require(live.blockAt(piston.x(),piston.y(),piston.z()).legacyId()==33&&live.blockAt(pushed.x(),pushed.y(),pushed.z()).equals(new BlockState(1,0))&&live.blockAt(lever.x(),lever.y(),lever.z()).legacyId()==69&&(live.blockAt(lever.x(),lever.y(),lever.z()).metadata()&8)==0&&!live.blockAt(head.x(),head.y(),head.z()).equals(new BlockState(1,0)),label+": "+cell(live));}
 String persist(RemoteChunkSnapshot after,int cx,int cz){BlockState p=at(after,piston,cx,cz),h=at(after,head,cx,cz),u=at(after,pushed,cx,cz),v=at(after,lever,cx,cz);require(p.equals(new BlockState(33,4))&&h.equals(new BlockState(0,0))&&u.equals(new BlockState(1,0))&&v.legacyId()==69&&(v.metadata()&8)==0,"fresh one-tick pulse drop drift "+p+"/"+h+"/"+u+"/"+v);return "piston="+cell(piston)+":"+p.legacyId()+":"+p.metadata()+",head="+cell(head)+":"+h.legacyId()+":"+h.metadata()+",pushed="+cell(pushed)+":1:0";}
 String cell(RemoteWorldView live){return "piston="+live.blockAt(piston.x(),piston.y(),piston.z())+",head="+live.blockAt(head.x(),head.y(),head.z())+",pushed="+live.blockAt(pushed.x(),pushed.y(),pushed.z())+",lever="+live.blockAt(lever.x(),lever.y(),lever.z())+",repeater="+live.blockAt(repeater.x(),repeater.y(),repeater.z());}
 static BlockPosition raise(B173WireClient a,RemoteChunkSnapshot initial,int cx,int cz,int[] column)throws Exception{
  BlockPosition top=foundation(initial,cx,cz);column[0]=0;a.selectHeldSlot(0);
  while(water(at(initial,BlockFace.UP.adjacent(top),cx,cz).legacyId())){top=place(a,top,BlockFace.UP,1);a.moveAndObserve(0D,1D,0D,1);require(++column[0]<=15,"water column exceeded one-tick pulse fixture");}
  top=place(a,top,BlockFace.UP,1);a.moveAndObserve(0D,1D,2D,1);column[0]++;return top;
 }
 static BlockPosition place(B173WireClient a,BlockPosition support,BlockFace face,int id)throws Exception{BlockPosition target=face.adjacent(support);a.placeHeldBlock(support,face);a.awaitBlock(target,new BlockState(id,0));return target;}
 static BlockPosition foundation(RemoteChunkSnapshot c,int cx,int cz){for(int x=4;x<=11;x++)for(int z=4;z<=11;z++)for(int y=126;y>=1;y--)if(c.blockAt(x,y,z).legacyId()==3&&water(c.blockAt(x,y+1,z).legacyId()))return new BlockPosition(cx*16+x,y,cz*16+z);throw new IllegalStateException("no deterministic one-tick pulse foundation");}
 static BlockState at(RemoteChunkSnapshot c,BlockPosition p,int cx,int cz){return c.blockAt(p.x()-cx*16,p.y(),p.z()-cz*16);}
 static boolean air(RemoteChunkSnapshot c,int cx,int cz,BlockPosition...p){for(BlockPosition x:p)if(at(c,x,cx,cz).legacyId()!=0)return false;return true;}
 static boolean water(int id){return id==8||id==9;}
 static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}
 static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}
 static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
 static String cell(BlockPosition p){return p.x()+":"+p.y()+":"+p.z();}
}
