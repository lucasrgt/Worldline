package worldline.smoke.gravityblocksetb173;

import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.security.MessageDigest;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Places supported sand 12 and gravel 13, removes each stone support, and freezes both one-cell settlements. */
public final class GravityBlockSetSmoke{
 private GravityBlockSetSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=9)throw new IllegalArgumentException("usage: GravityBlockSetSmoke server.jar workspace port seed username chunkX chunkZ fixtureTicks gravityTicks");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]);int fixtureTicks=Integer.parseInt(a[7]),gravityTicks=Integer.parseInt(a[8]);Duration timeout=Duration.ofSeconds(90);
  require(user.length()<=16,"username exceeds 16");B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,5,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout),reader=null;
  RemoteChunkSnapshot before,after;BlockPosition sandSupport,sand,gravelSupport,gravel;int column;RemoteObjectSpawn sandFall,gravelFall;
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,new int[]{0,1,2},new int[]{1,12,13},new int[]{32,1,1},new int[]{0,0,0});actor.connect();actor.synchronizePose();require(actor.awaitInventory().occupiedSlots()==3,"gravity-set inventory drift");
   RemoteChunkSnapshot initial=actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);BlockPosition support=foundation(initial,cx,cz);column=0;actor.selectHeldSlot(0);
   while(water(initial.blockAt(local(support.x(),cx),support.y()+1,local(support.z(),cz)).legacyId())){support=place(actor,support,BlockFace.UP,1);actor.moveAndObserve(0D,1D,0D,1);require(++column<=15,"water column exceeded gravity-set fixture");}
   sandSupport=place(actor,support,BlockFace.UP,1);actor.moveAndObserve(0D,1D,0D,1);column++;sand=BlockFace.UP.adjacent(sandSupport);
   require(initial.blockAt(local(sand.x(),cx),sand.y(),local(sand.z(),cz)).legacyId()==0,"sand target was not initial air");
   actor.selectHeldSlot(1);actor.placeHeldBlock(sandSupport,BlockFace.UP);actor.awaitBlock(sand,new BlockState(12,0));
   actor.selectHeldSlot(0);BlockPosition pad=place(actor,support,BlockFace.EAST,1);gravelSupport=place(actor,pad,BlockFace.UP,1);gravel=BlockFace.UP.adjacent(gravelSupport);
   require(initial.blockAt(local(gravel.x(),cx),gravel.y(),local(gravel.z(),cz)).legacyId()==0,"gravel target was not initial air");
   actor.selectHeldSlot(2);actor.placeHeldBlock(gravelSupport,BlockFace.UP);actor.awaitBlock(gravel,new BlockState(13,0));
   actor.selectHeldSlot(3);actor.moveAndObserve(0D,-2D,0D,2);before=actor.sustainTicks(fixtureTicks).chunkAt(cx,cz);
   require(before.blockAt(local(sandSupport.x(),cx),sandSupport.y(),local(sandSupport.z(),cz)).equals(new BlockState(1,0))&&before.blockAt(local(sand.x(),cx),sand.y(),local(sand.z(),cz)).equals(new BlockState(12,0)),"stable sand 12:0 fixture drift");
   require(before.blockAt(local(gravelSupport.x(),cx),gravelSupport.y(),local(gravelSupport.z(),cz)).equals(new BlockState(1,0))&&before.blockAt(local(gravel.x(),cx),gravel.y(),local(gravel.z(),cz)).equals(new BlockState(13,0)),"stable gravel 13:0 fixture drift");
   sandFall=drop(actor,sandSupport,sand,12,70,gravityTicks);gravelFall=drop(actor,gravelSupport,gravel,13,71,gravityTicks);
   require(sandFall.type()==70&&gravelFall.type()==71&&sandFall.entityId()!=gravelFall.entityId()&&sandFall.entityId()!=actor.state().entityId(),"Packet23 type 70/71 pair drift");
   actor.close();awaitPlayers(server,0);server.save();reader=new B173WireClient("127.0.0.1",port,user,timeout);reader.connect();reader.synchronizePose();after=reader.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);
   require(after.blockAt(local(sandSupport.x(),cx),sandSupport.y(),local(sandSupport.z(),cz)).equals(new BlockState(12,0))&&after.blockAt(local(sand.x(),cx),sand.y(),local(sand.z(),cz)).equals(new BlockState(0,0)),"fresh settled sand 12:0 drift");
   require(after.blockAt(local(gravelSupport.x(),cx),gravelSupport.y(),local(gravelSupport.z(),cz)).equals(new BlockState(13,0))&&after.blockAt(local(gravel.x(),cx),gravel.y(),local(gravel.z(),cz)).equals(new BlockState(0,0)),"fresh settled gravel 13:0 drift");
  }finally{actor.close();if(reader!=null)reader.close();server.close();}
  String evidence="column="+column+",sand="+sandSupport.x()+":"+sandSupport.y()+":"+sandSupport.z()+":1:0->12:0,sandUpper="+sand.x()+":"+sand.y()+":"+sand.z()+":12:0->0:0,gravel="+gravelSupport.x()+":"+gravelSupport.y()+":"+gravelSupport.z()+":1:0->13:0,gravelUpper="+gravel.x()+":"+gravel.y()+":"+gravel.z()+":13:0->0:0,packet23=70+71,persisted=true,clients=2,disconnect=clean";
  String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=stone-column+supported-sand12+supported-gravel13|settle="+fixtureTicks+"+"+gravityTicks+"ticks|cause=packet14-remove-support|confirmation=packet53-air|effect=official-falling-sand-and-gravel-settle|observation=packet23-type70+packet23-type71+live-packet53+fresh-login-packet51|"+evidence;
  System.out.println("WORLDLINE_M342_SET="+evidence);System.out.println("WORLDLINE_M342_TRACE="+trace);System.out.println("WORLDLINE_M342_SIGNATURE="+sha(trace));
 }
 private static RemoteObjectSpawn drop(B173WireClient a,BlockPosition support,BlockPosition upper,int id,int type,int ticks)throws Exception{
  a.beginBreak(support);Thread.sleep(3000L);a.finishBreak(support);
  BlockState opened=a.awaitBlock(support,new BlockState(0,0)).blockAt(support.x(),support.y(),support.z());
  RemoteObjectSpawn fall=a.awaitObjectSpawn(type);require(fall.type()==type&&fall.entityId()!=a.state().entityId(),"Packet23 type "+type+" absent for "+id);
  a.awaitBlock(upper,new BlockState(0,0));RemoteWorldView live=a.awaitBlock(support,new BlockState(id,0));a.sustainTicks(Math.max(1,ticks));
  BlockState settled=live.blockAt(support.x(),support.y(),support.z()),cleared=a.sustainTicks(1).blockAt(upper.x(),upper.y(),upper.z());
  require(opened.equals(new BlockState(0,0))&&settled.equals(new BlockState(id,0))&&cleared.equals(new BlockState(0,0)),id+" did not settle one block: "+opened+" / "+settled+" / "+cleared+" at "+support+"/"+upper);return fall;
 }
 private static BlockPosition place(B173WireClient a,BlockPosition support,BlockFace face,int id)throws Exception{BlockPosition target=face.adjacent(support);a.placeHeldBlock(support,face);a.awaitBlock(target,new BlockState(id,0));return target;}
 private static BlockPosition foundation(RemoteChunkSnapshot q,int cx,int cz){for(int x=4;x<=11;x++)for(int z=4;z<=11;z++)for(int y=126;y>=1;y--)if(q.blockAt(x,y,z).legacyId()==3&&water(q.blockAt(x,y+1,z).legacyId()))return new BlockPosition(cx*16+x,y,cz*16+z);throw new IllegalStateException("no deterministic gravity-set foundation");}
 private static boolean water(int id){return id==8||id==9;}private static int local(int v,int c){return v-c*16;}
 private static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}
 private static String sha(String s)throws Exception{return hex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));}private static String hex(byte[]b){StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}
 private static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
