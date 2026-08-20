package worldline.smoke.remainingoreplacesetb173;

import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.security.MessageDigest;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Places remaining coal 16, lapis 21, and redstone 73 ores on raised stone as one Packet15 family. */
public final class RemainingOrePlaceSetSmoke{
 private RemainingOrePlaceSetSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=7)throw new IllegalArgumentException("usage: RemainingOrePlaceSetSmoke server.jar workspace port seed username chunkX chunkZ");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]);
  require(seed==17320110707L&&user.equals("OrePlace439")&&user.length()<=16,"remaining-ore-place-set identity drift");
  Duration timeout=Duration.ofSeconds(90);B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout),reader=null;
  BlockPosition top,east,west,coal,lapis,redstone;int column;BlockState coalPlaced=new BlockState(16,0),lapisPlaced=new BlockState(21,0),redstonePlaced=new BlockState(73,0),stone=new BlockState(1,0);
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,new int[]{0,1,2,3},new int[]{1,16,21,73},new int[]{32,1,1,1},new int[]{0,0,0,0});actor.connect();actor.synchronizePose();
   require(actor.awaitInventory().occupiedSlots()==4,"remaining-ore-place-set inventory drift");
   RemoteChunkSnapshot initial=actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);top=foundation(initial,cx,cz);column=0;actor.selectHeldSlot(0);
   while(water(initial.blockAt(local(top.x(),cx),top.y()+1,local(top.z(),cz)).legacyId())){top=place(actor,top,BlockFace.UP,1);actor.moveAndObserve(0D,1D,0D,1);require(++column<=15,"water column exceeded remaining-ore-place-set fixture");}for(int lift=0;lift<8;lift++){top=place(actor,top,BlockFace.UP,1);actor.moveAndObserve(0D,1D,0D,1);column++;}
   east=place(actor,top,BlockFace.EAST,1);west=place(actor,top,BlockFace.WEST,1);
   actor.selectHeldSlot(1);coal=place(actor,top,BlockFace.UP,16);actor.selectHeldSlot(2);lapis=place(actor,east,BlockFace.UP,21);actor.selectHeldSlot(3);redstone=place(actor,west,BlockFace.UP,73);
   RemoteWorldView live=actor.sustainTicks(5);require(live.blockAt(coal.x(),coal.y(),coal.z()).equals(coalPlaced)&&live.blockAt(lapis.x(),lapis.y(),lapis.z()).equals(lapisPlaced)&&live.blockAt(redstone.x(),redstone.y(),redstone.z()).equals(redstonePlaced),"live remaining-ore-place-set drift");
   actor.close();awaitPlayers(server,0);server.save();reader=new B173WireClient("127.0.0.1",port,user,timeout);reader.connect();reader.synchronizePose();RemoteChunkSnapshot after=reader.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);
   require(after.blockAt(local(top.x(),cx),top.y(),local(top.z(),cz)).equals(stone)&&after.blockAt(local(coal.x(),cx),coal.y(),local(coal.z(),cz)).equals(coalPlaced)&&after.blockAt(local(lapis.x(),cx),lapis.y(),local(lapis.z(),cz)).equals(lapisPlaced)&&after.blockAt(local(redstone.x(),cx),redstone.y(),local(redstone.z(),cz)).equals(redstonePlaced),"persisted remaining-ore-place-set drift");
   String evidence="column="+column+",support="+cell(top,1,0)+",coal="+cell(coal,16,0)+",lapis="+cell(lapis,21,0)+",redstone="+cell(redstone,73,0)+",persisted=true,clients=2,disconnect=clean";
   String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=raised-stone+ore16+ore21+ore73|cause=packet15-item16+item21+item73|wire=packet53-ore16:0+ore21:0+ore73:0|oracle=ore-place-family+fresh-login|"+evidence;
   System.out.println("WORLDLINE_M439_SET="+evidence);System.out.println("WORLDLINE_M439_TRACE="+trace);System.out.println("WORLDLINE_M439_SIGNATURE="+sha(trace));
  }finally{actor.close();if(reader!=null)reader.close();server.close();}
 }
 private static BlockPosition place(B173WireClient a,BlockPosition support,BlockFace face,int id)throws Exception{BlockPosition target=face.adjacent(support);a.placeHeldBlock(support,face);a.awaitBlock(target,new BlockState(id,0));return target;}
 private static BlockPosition foundation(RemoteChunkSnapshot q,int cx,int cz){for(int x=4;x<=11;x++)for(int z=4;z<=11;z++)for(int y=126;y>=1;y--)if(q.blockAt(x,y,z).legacyId()==3&&water(q.blockAt(x,y+1,z).legacyId()))return new BlockPosition(cx*16+x,y,cz*16+z);throw new IllegalStateException("no deterministic remaining-ore-place-set foundation");}
 private static String cell(BlockPosition p,int id,int meta){return p.x()+":"+p.y()+":"+p.z()+":"+id+":"+meta;}
 private static boolean water(int id){return id==8||id==9;}private static int local(int v,int c){return v-c*16;}private static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}private static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}private static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
