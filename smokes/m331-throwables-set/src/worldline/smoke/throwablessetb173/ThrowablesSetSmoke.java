package worldline.smoke.throwablessetb173;

import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.security.MessageDigest;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Air-uses snowball 332, egg 344, and fishing rod 346, correlating Packet23 types 61, 62, and 90. */
public final class ThrowablesSetSmoke{
 private ThrowablesSetSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=8)throw new IllegalArgumentException("usage: ThrowablesSetSmoke server.jar workspace port seed actor observer chunkX chunkZ");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String actorName=a[4],observerName=a[5];int cx=Integer.parseInt(a[6]),cz=Integer.parseInt(a[7]);Duration timeout=Duration.ofSeconds(90);
  require(actorName.length()<=16&&observerName.length()<=16,"username exceeds 16");B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,actorName,timeout),observer=new B173WireClient("127.0.0.1",port,observerName,timeout);BlockPosition top;int column;
  try{server.boot();B173PlayerSeed.writeInventory(workspace,actorName,4.5D,60D,4.5D,new int[]{0,1,2,3},new int[]{1,332,344,346},new int[]{32,16,1,1},new int[]{0,0,0,0});B173PlayerSeed.write(workspace,observerName,4.5D,80D,4.5D);actor.connect();actor.synchronizePose();require(actor.awaitInventory().occupiedSlots()==4,"throwables inventory drift");RemoteChunkSnapshot initial=actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);top=foundation(initial,cx,cz);column=0;actor.selectHeldSlot(0);
   while(water(initial.blockAt(local(top.x(),cx),top.y()+1,local(top.z(),cz)).legacyId())){top=place(actor,top,BlockFace.UP,1);actor.moveAndObserve(0D,1D,0D,1);require(++column<=15,"water column exceeded throwables fixture");}for(int lift=0;lift<8;lift++){top=place(actor,top,BlockFace.UP,1);actor.moveAndObserve(0D,1D,0D,1);column++;}
   observer.connect();observer.synchronizePose();observer.awaitRemoteChunk(cx,cz);
   RemoteObjectSpawn snow=air(actor,observer,1,61,top),egg=air(actor,observer,2,62,top),hook=air(actor,observer,3,90,top);
   require(snow.entityId()!=egg.entityId()&&egg.entityId()!=hook.entityId()&&snow.entityId()!=hook.entityId(),"throwable identity collision");
   actor.close();observer.close();awaitPlayers(server,0);server.save();
   String evidence="column="+column+",support="+top.x()+":"+top.y()+":"+top.z()+":1:0,snow=type61+shared-id+"+token(snow)+",egg=type62+shared-id+"+token(egg)+",hook=type90+shared-id+"+token(hook)+",items=332+344+346,clients=2,disconnect=clean";String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=raised-stone+snowball332+egg344+rod346|cause=packet15-dir255-item332+item344+item346|wire=packet23-type61+type62+type90|oracle=two-peer-identical-throwable-objects|"+evidence;System.out.println("WORLDLINE_M331_THROWABLES="+evidence);System.out.println("WORLDLINE_M331_TRACE="+trace);System.out.println("WORLDLINE_M331_SIGNATURE="+sha(trace));
  }finally{actor.close();observer.close();server.close();}
 }
 private static RemoteObjectSpawn air(B173WireClient a,B173WireClient o,int slot,int type,BlockPosition top){
  a.selectHeldSlot(slot);a.look(0F,0F);a.useSelectedItemInAir();RemoteObjectSpawn spawn=a.awaitObjectSpawn(type),peer=o.awaitObjectSpawn(type);
  require(spawn.equals(peer)&&spawn.type()==type&&spawn.entityId()!=a.state().entityId()&&spawn.entityId()!=o.state().entityId(),"peer throwable spawn drift type="+type);
  require(spawn.throwerId()==0||spawn.throwerId()==a.state().entityId(),"throwable thrower drift type="+type+",thrower="+spawn.throwerId()+",actor="+a.state().entityId());
  require(Math.abs(spawn.x()-(top.x()+0.5D))<=2D&&Math.abs(spawn.z()-(top.z()+0.5D))<=2D,"throwable packet pose escaped platform type="+type+" pose="+spawn.x()+":"+spawn.y()+":"+spawn.z()+",support="+top);
  return spawn;
 }
 private static String token(RemoteObjectSpawn s){return s.throwerId()==0?"thrower0":"thrower=actor";}
 private static BlockPosition place(B173WireClient a,BlockPosition support,BlockFace face,int id)throws Exception{BlockPosition target=face.adjacent(support);a.placeHeldBlock(support,face);a.awaitBlock(target,new BlockState(id,0));return target;}
 private static BlockPosition foundation(RemoteChunkSnapshot q,int cx,int cz){for(int x=4;x<=11;x++)for(int z=4;z<=11;z++)for(int y=126;y>=1;y--)if(q.blockAt(x,y,z).legacyId()==3&&water(q.blockAt(x,y+1,z).legacyId()))return new BlockPosition(cx*16+x,y,cz*16+z);throw new IllegalStateException("no deterministic throwables foundation");}
 private static boolean water(int id){return id==8||id==9;}private static int local(int v,int c){return v-c*16;}private static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}private static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}private static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
