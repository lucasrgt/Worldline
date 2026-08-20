package worldline.smoke.fallinggravelb173;

import java.nio.ByteBuffer;import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.security.MessageDigest;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Places supported gravel 13:0, removes the stone support, and freezes one-cell settlement. */
public final class FallingGravelSmoke{
 private FallingGravelSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=9)throw new IllegalArgumentException("usage: FallingGravelSmoke server.jar workspace port seed username chunkX chunkZ fixtureTicks gravityTicks");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]);int fixtureTicks=Integer.parseInt(a[7]),gravityTicks=Integer.parseInt(a[8]);Duration timeout=Duration.ofSeconds(90);
  B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout),reader=null;
  RemoteChunkSnapshot before,after;BlockPosition support,gravel;BlockState opened,settled,cleared;int column;
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,new int[]{0,1},new int[]{1,13},new int[]{16,1},new int[]{0,0});actor.connect();actor.synchronizePose();require(actor.awaitInventory().occupiedSlots()==2,"gravel fixture inventory drift");
   RemoteChunkSnapshot initial=actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);support=foundation(initial,cx,cz);column=0;actor.selectHeldSlot(0);
   while(water(initial.blockAt(local(support.x(),cx),support.y()+1,local(support.z(),cz)).legacyId())){support=place(actor,support,BlockFace.UP,1);actor.moveAndObserve(0D,1D,0D,1);require(++column<=15,"water column exceeded falling-gravel fixture");}
   support=place(actor,support,BlockFace.UP,1);actor.moveAndObserve(0D,1D,0D,1);column++;gravel=BlockFace.UP.adjacent(support);
   require(initial.blockAt(local(gravel.x(),cx),gravel.y(),local(gravel.z(),cz)).legacyId()==0,"gravel target was not initial air");
   actor.selectHeldSlot(1);actor.placeHeldBlock(support,BlockFace.UP);actor.awaitBlock(gravel,new BlockState(13,0));
   actor.selectHeldSlot(2);actor.moveAndObserve(0D,-2D,0D,2);before=actor.sustainTicks(fixtureTicks).chunkAt(cx,cz);
   require(before.blockAt(local(support.x(),cx),support.y(),local(support.z(),cz)).equals(new BlockState(1,0))&&before.blockAt(local(gravel.x(),cx),gravel.y(),local(gravel.z(),cz)).equals(new BlockState(13,0)),"stable gravel 13:0 fixture drift");
   actor.beginBreak(support);Thread.sleep(3000L);actor.finishBreak(support);opened=actor.awaitBlock(support,new BlockState(0,0)).blockAt(support.x(),support.y(),support.z());
   RemoteWorldView live=actor.sustainTicks(gravityTicks);settled=live.blockAt(support.x(),support.y(),support.z());cleared=live.blockAt(gravel.x(),gravel.y(),gravel.z());
   require(opened.equals(new BlockState(0,0))&&settled.equals(new BlockState(13,0))&&cleared.equals(new BlockState(0,0)),"gravel did not settle one block: "+opened+" / "+settled+" / "+cleared);
   actor.close();awaitPlayers(server,0);server.save();reader=new B173WireClient("127.0.0.1",port,user,timeout);reader.connect();reader.synchronizePose();after=reader.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);
   require(after.blockAt(local(support.x(),cx),support.y(),local(support.z(),cz)).equals(settled)&&after.blockAt(local(gravel.x(),cx),gravel.y(),local(gravel.z(),cz)).equals(cleared),"fresh settled gravel 13:0 drift");
  }finally{actor.close();if(reader!=null)reader.close();server.close();}
  StateDelta delta=delta(before,after);require(delta.changed==2,"falling gravel changed unrelated states: "+delta);
  String evidence="column="+column+",lower="+support.x()+":"+support.y()+":"+support.z()+":1:0->13:0,upper="+gravel.x()+":"+gravel.y()+":"+gravel.z()+":13:0->0:0,states="+delta;
  String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=stone-column+supported-gravel13|settle="+fixtureTicks+"+"+gravityTicks+"ticks|cause=packet14-remove-support|confirmation=packet53-air|effect=official-falling-gravel-settle|observation=live-packet53+fresh-login-packet51|"+evidence+"|disconnect=clean";
  System.out.println("WORLDLINE_M274_GRAVITY="+evidence);System.out.println("WORLDLINE_M274_TRACE="+trace);System.out.println("WORLDLINE_M274_SIGNATURE="+sha(trace));
 }
 private static BlockPosition place(B173WireClient a,BlockPosition support,BlockFace face,int id)throws Exception{BlockPosition target=face.adjacent(support);a.placeHeldBlock(support,face);a.awaitBlock(target,new BlockState(id,0));return target;}
 private static BlockPosition foundation(RemoteChunkSnapshot q,int cx,int cz){for(int x=4;x<=11;x++)for(int z=4;z<=11;z++)for(int y=126;y>=1;y--)if(q.blockAt(x,y,z).legacyId()==3&&water(q.blockAt(x,y+1,z).legacyId()))return new BlockPosition(cx*16+x,y,cz*16+z);throw new IllegalStateException("no deterministic falling-gravel foundation");}
 private static StateDelta delta(RemoteChunkSnapshot before,RemoteChunkSnapshot after)throws Exception{MessageDigest digest=MessageDigest.getInstance("SHA-256");ByteBuffer row=ByteBuffer.allocate(10);int changed=0;for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=0;y<128;y++){BlockState a=before.blockAt(x,y,z),b=after.blockAt(x,y,z);if(!a.equals(b)){changed++;row.clear();row.putShort((short)x).putShort((short)y).putShort((short)z).put((byte)a.legacyId()).put((byte)a.metadata()).put((byte)b.legacyId()).put((byte)b.metadata());digest.update(row.array());}}return new StateDelta(changed,hex(digest.digest()));}
 private static boolean water(int id){return id==8||id==9;}private static int local(int v,int c){return v-c*16;}
 private static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}
 private static String sha(String s)throws Exception{return hex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));}private static String hex(byte[]b){StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}
 private static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}private static final class StateDelta{final int changed;final String hash;StateDelta(int c,String h){changed=c;hash=h;}@Override public String toString(){return changed+":"+hash;}}
}
