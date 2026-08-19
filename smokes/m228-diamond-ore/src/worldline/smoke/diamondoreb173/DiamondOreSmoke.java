package worldline.smoke.diamondoreb173;

import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.security.MessageDigest;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Places official diamond ore 56 on raised stone and freezes live block 56:0. */
public final class DiamondOreSmoke{
 private DiamondOreSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=7)throw new IllegalArgumentException("usage: DiamondOreSmoke server.jar workspace port seed username chunkX chunkZ");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]);Duration timeout=Duration.ofSeconds(90);
  B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout),reader=null;BlockPosition top,ore;int column;BlockState placed;
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,new int[]{0,1},new int[]{1,56},new int[]{32,1},new int[]{0,0});actor.connect();actor.synchronizePose();require(actor.awaitInventory().occupiedSlots()==2,"diamond ore inventory drift");RemoteChunkSnapshot initial=actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);top=foundation(initial,cx,cz);column=0;actor.selectHeldSlot(0);
   while(water(initial.blockAt(local(top.x(),cx),top.y()+1,local(top.z(),cz)).legacyId())){top=place(actor,top,BlockFace.UP,1);actor.moveAndObserve(0D,1D,0D,1);require(++column<=15,"water column exceeded diamond ore fixture");}for(int lift=0;lift<8;lift++){top=place(actor,top,BlockFace.UP,1);actor.moveAndObserve(0D,1D,0D,1);column++;}
   actor.selectHeldSlot(1);ore=BlockFace.UP.adjacent(top);actor.placeHeldBlock(top,BlockFace.UP);placed=new BlockState(56,0);actor.awaitBlock(ore,placed);require(actor.sustainTicks(5).blockAt(ore.x(),ore.y(),ore.z()).equals(placed),"live diamond ore drift");actor.close();awaitPlayers(server,0);server.save();
   reader=new B173WireClient("127.0.0.1",port,user,timeout);reader.connect();reader.synchronizePose();RemoteChunkSnapshot after=reader.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);require(after.blockAt(local(top.x(),cx),top.y(),local(top.z(),cz)).equals(new BlockState(1,0))&&after.blockAt(local(ore.x(),cx),ore.y(),local(ore.z(),cz)).equals(placed),"persisted diamond ore drift");
   String evidence="column="+column+",support="+top.x()+":"+top.y()+":"+top.z()+":1:0,ore="+ore.x()+":"+ore.y()+":"+ore.z()+":56:"+placed.metadata()+",persisted=true,clients=2,disconnect=clean";String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=raised-stone+ore56|cause=packet15-item56|wire=packet53-ore56:"+placed.metadata()+"|oracle=live-block56:0+fresh-login|"+evidence;System.out.println("WORLDLINE_M228_ORE="+evidence);System.out.println("WORLDLINE_M228_TRACE="+trace);System.out.println("WORLDLINE_M228_SIGNATURE="+sha(trace));
  }finally{actor.close();if(reader!=null)reader.close();server.close();}
 }
 private static BlockPosition place(B173WireClient a,BlockPosition support,BlockFace face,int id)throws Exception{BlockPosition target=face.adjacent(support);a.placeHeldBlock(support,face);a.awaitBlock(target,new BlockState(id,0));return target;}
 private static BlockPosition foundation(RemoteChunkSnapshot q,int cx,int cz){for(int x=4;x<=11;x++)for(int z=4;z<=11;z++)for(int y=126;y>=1;y--)if(q.blockAt(x,y,z).legacyId()==3&&water(q.blockAt(x,y+1,z).legacyId()))return new BlockPosition(cx*16+x,y,cz*16+z);throw new IllegalStateException("no deterministic diamond ore foundation");}
 private static boolean water(int id){return id==8||id==9;}private static int local(int v,int c){return v-c*16;}private static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}private static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}private static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
