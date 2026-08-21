package worldline.smoke.trapdoorfamilysetb173;

import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.security.MessageDigest;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Places official trapdoor 96 on south, north, east, and west faces and toggles each open then closed. */
public final class TrapdoorFamilySetSmoke{
 private TrapdoorFamilySetSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=7)throw new IllegalArgumentException("usage: TrapdoorFamilySetSmoke server.jar workspace port seed username chunkX chunkZ");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]);
  require(seed==17320110707L&&user.equals("TrapFam380")&&user.length()<=16,"trapdoor-family-set identity drift");
  Duration timeout=Duration.ofSeconds(90);B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout),reader=null;BlockPosition top,south,north,east,west;int column;
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,new int[]{0,1},new int[]{1,96},new int[]{32,4},new int[]{0,0});actor.connect();actor.synchronizePose();require(actor.awaitInventory().occupiedSlots()==2,"trapdoor-family inventory drift");RemoteChunkSnapshot initial=actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);top=foundation(initial,cx,cz);column=0;actor.selectHeldSlot(0);
   while(water(initial.blockAt(local(top.x(),cx),top.y()+1,local(top.z(),cz)).legacyId())){top=place(actor,top,BlockFace.UP,1);actor.moveAndObserve(0D,1D,0D,1);require(++column<=15,"water column exceeded trapdoor-family fixture");}for(int lift=0;lift<8;lift++){top=place(actor,top,BlockFace.UP,1);actor.moveAndObserve(0D,1D,0D,1);column++;}
   south=trap(actor,top,BlockFace.SOUTH,1);north=trap(actor,top,BlockFace.NORTH,0);east=trap(actor,top,BlockFace.EAST,3);west=trap(actor,top,BlockFace.WEST,2);actor.close();awaitPlayers(server,0);server.save();
   reader=new B173WireClient("127.0.0.1",port,user,timeout);reader.connect();reader.synchronizePose();RemoteChunkSnapshot after=reader.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);
   require(cell(after,south,cx,cz,1)&&cell(after,north,cx,cz,0)&&cell(after,east,cx,cz,3)&&cell(after,west,cx,cz,2),"persisted four-face trapdoor closed drift");
   String evidence="column="+column+",support="+top.x()+":"+top.y()+":"+top.z()+":1:0,south="+token(south,1)+",north="+token(north,0)+",east="+token(east,3)+",west="+token(west,2)+",persisted=true,clients=2,disconnect=clean";
   String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=raised-stone+trapdoor96-south-north-east-west|cause=packet15-item96-place-south-north-east-west+empty-hand-packet15-open-then-close|wire=packet53-trapdoor96:1->5->1+96:0->4->0+96:3->7->3+96:2->6->2|oracle=live-four-face-toggle+fresh-login-closed-trapdoors|"+evidence;
   System.out.println("WORLDLINE_M380_SET="+evidence);System.out.println("WORLDLINE_M380_TRACE="+trace);System.out.println("WORLDLINE_M380_SIGNATURE="+sha(trace));
  }finally{actor.close();if(reader!=null)reader.close();server.close();}
 }
 private static BlockPosition trap(B173WireClient a,BlockPosition support,BlockFace face,int closed)throws Exception{BlockPosition t=face.adjacent(support);int open=closed+4;a.selectHeldSlot(1);a.placeHeldBlock(support,face);require(a.awaitBlock(t,new BlockState(96,closed)).blockAt(t.x(),t.y(),t.z()).equals(new BlockState(96,closed)),"placed trapdoor 96:"+closed+" absent");a.selectHeldSlot(2);a.activateBlock(t,face);require(a.awaitBlock(t,new BlockState(96,open)).blockAt(t.x(),t.y(),t.z()).equals(new BlockState(96,open)),"open trapdoor 96:"+open+" absent");a.activateBlock(t,face);require(a.awaitBlock(t,new BlockState(96,closed)).blockAt(t.x(),t.y(),t.z()).equals(new BlockState(96,closed)),"closed trapdoor 96:"+closed+" absent");return t;}
 private static boolean cell(RemoteChunkSnapshot q,BlockPosition p,int cx,int cz,int meta){return q.blockAt(local(p.x(),cx),p.y(),local(p.z(),cz)).equals(new BlockState(96,meta));}
 private static String token(BlockPosition p,int closed){return p.x()+":"+p.y()+":"+p.z()+":96:"+closed+"->"+(closed+4)+"->"+closed;}
 private static BlockPosition place(B173WireClient a,BlockPosition support,BlockFace face,int id)throws Exception{BlockPosition target=face.adjacent(support);a.placeHeldBlock(support,face);a.awaitBlock(target,new BlockState(id,0));return target;}
 private static BlockPosition foundation(RemoteChunkSnapshot q,int cx,int cz){for(int x=4;x<=11;x++)for(int z=4;z<=11;z++)for(int y=126;y>=1;y--)if(q.blockAt(x,y,z).legacyId()==3&&water(q.blockAt(x,y+1,z).legacyId()))return new BlockPosition(cx*16+x,y,cz*16+z);throw new IllegalStateException("no deterministic trapdoor-family foundation");}
 private static boolean water(int id){return id==8||id==9;}private static int local(int v,int c){return v-c*16;}private static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}private static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}private static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
