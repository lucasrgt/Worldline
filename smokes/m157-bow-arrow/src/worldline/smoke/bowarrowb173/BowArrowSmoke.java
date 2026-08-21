package worldline.smoke.bowarrowb173;

import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.security.MessageDigest;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Shoots an official bow and proves two peers observe the same Packet23 type-60 arrow. */
public final class BowArrowSmoke{
 private BowArrowSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=8)throw new IllegalArgumentException("usage: BowArrowSmoke server.jar workspace port seed actor observer chunkX chunkZ");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String actorName=a[4],observerName=a[5];int cx=Integer.parseInt(a[6]),cz=Integer.parseInt(a[7]);Duration timeout=Duration.ofSeconds(90);
  B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,actorName,timeout),observer=new B173WireClient("127.0.0.1",port,observerName,timeout);BlockPosition top;int column;RemoteObjectSpawn arrow;
  try{server.boot();B173PlayerSeed.writeInventory(workspace,actorName,4.5D,60D,4.5D,new int[]{0,1,2},new int[]{1,261,262},new int[]{32,1,16},new int[]{0,0,0});B173PlayerSeed.write(workspace,observerName,4.5D,80D,4.5D);actor.connect();actor.synchronizePose();require(actor.awaitInventory().occupiedSlots()==3,"bow inventory drift");RemoteChunkSnapshot initial=actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);top=foundation(initial,cx,cz);column=0;actor.selectHeldSlot(0);
   while(water(initial.blockAt(local(top.x(),cx),top.y()+1,local(top.z(),cz)).legacyId())){top=place(actor,top,BlockFace.UP,1);actor.moveAndObserve(0D,1D,0D,1);require(++column<=15,"water column exceeded bow fixture");}for(int lift=0;lift<8;lift++){top=place(actor,top,BlockFace.UP,1);actor.moveAndObserve(0D,1D,0D,1);column++;}
   observer.connect();observer.synchronizePose();observer.awaitRemoteChunk(cx,cz);actor.selectHeldSlot(1);actor.look(0F,0F);actor.useSelectedItemInAir();arrow=actor.awaitObjectSpawn(60);RemoteObjectSpawn peer=observer.awaitObjectSpawn(60);require(arrow.entityId()==peer.entityId()&&arrow.type()==60&&peer.type()==60&&arrow.throwerId()==peer.throwerId()&&arrow.throwerId()==actor.state().entityId(),"peer arrow object spawn drift");
   actor.close();observer.close();awaitPlayers(server,0);server.save();
   String evidence="column="+column+",bow=261,arrow=262,wire=packet23-type60,thrower=actor,shared-id=true,clients=2,disconnect=clean";String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=raised-stone-platform|cause=packet15-air-bow261+arrow262|wire=packet23-type60|oracle=two-peer-identical-arrow-object-spawn|"+evidence;System.out.println("WORLDLINE_M157_ARROW="+evidence);System.out.println("WORLDLINE_M157_TRACE="+trace);System.out.println("WORLDLINE_M157_SIGNATURE="+sha(trace));
  }finally{actor.close();observer.close();server.close();}
 }
 private static BlockPosition place(B173WireClient a,BlockPosition support,BlockFace face,int id)throws Exception{BlockPosition target=face.adjacent(support);a.placeHeldBlock(support,face);a.awaitBlock(target,new BlockState(id,0));return target;}
 private static BlockPosition foundation(RemoteChunkSnapshot q,int cx,int cz){for(int x=4;x<=11;x++)for(int z=4;z<=11;z++)for(int y=126;y>=1;y--)if(q.blockAt(x,y,z).legacyId()==3&&water(q.blockAt(x,y+1,z).legacyId()))return new BlockPosition(cx*16+x,y,cz*16+z);throw new IllegalStateException("no deterministic bow foundation");}
 private static boolean water(int id){return id==8||id==9;}private static int local(int v,int c){return v-c*16;}private static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}private static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}private static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
