package worldline.smoke.woodenbuttonsetb173;

import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.security.MessageDigest;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Places official stone button 77 on the four wall faces as one SET. Wooden button 143 does not exist in b1.7.3. */
public final class WoodenButtonSetSmoke{
 private WoodenButtonSetSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=7)throw new IllegalArgumentException("usage: WoodenButtonSetSmoke server.jar workspace port seed username chunkX chunkZ");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]);
  require(seed==17320110707L&&user.equals("StoneBtn399")&&user.length()<=16,"wooden-button-set identity drift");
  Duration timeout=Duration.ofSeconds(90);B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout),reader=null;
  BlockPosition top,east,west,south,north;int column;
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,new int[]{0,1},new int[]{1,77},new int[]{32,4},new int[]{0,0});actor.connect();actor.synchronizePose();require(actor.awaitInventory().occupiedSlots()==2,"wooden-button-set inventory drift");RemoteChunkSnapshot initial=actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);top=foundation(initial,cx,cz);column=0;actor.selectHeldSlot(0);
   while(water(initial.blockAt(local(top.x(),cx),top.y()+1,local(top.z(),cz)).legacyId())){top=place(actor,top,BlockFace.UP,1);actor.moveAndObserve(0D,1D,0D,1);require(++column<=15,"water column exceeded wooden-button-set fixture");}for(int lift=0;lift<8;lift++){top=place(actor,top,BlockFace.UP,1);actor.moveAndObserve(0D,1D,0D,1);column++;}
   east=BlockFace.EAST.adjacent(top);west=BlockFace.WEST.adjacent(top);south=BlockFace.SOUTH.adjacent(top);north=BlockFace.NORTH.adjacent(top);
   require(air(initial,east,cx,cz)&&air(initial,west,cx,cz)&&air(initial,south,cx,cz)&&air(initial,north,cx,cz),"button targets were not initial air");
   actor.selectHeldSlot(1);east=button(actor,top,BlockFace.EAST,1,-90F);west=button(actor,top,BlockFace.WEST,2,90F);south=button(actor,top,BlockFace.SOUTH,3,0F);north=button(actor,top,BlockFace.NORTH,4,180F);
   RemoteWorldView live=worldline.test.WorldlineSmokeAwait.observe(actor,5);require(live.blockAt(east.x(),east.y(),east.z()).equals(new BlockState(77,1))&&live.blockAt(west.x(),west.y(),west.z()).equals(new BlockState(77,2))&&live.blockAt(south.x(),south.y(),south.z()).equals(new BlockState(77,3))&&live.blockAt(north.x(),north.y(),north.z()).equals(new BlockState(77,4)),"live stone-button-face drift");
   actor.close();awaitPlayers(server,0);server.save();reader=new B173WireClient("127.0.0.1",port,user,timeout);reader.connect();reader.synchronizePose();RemoteChunkSnapshot after=reader.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);
   require(after.blockAt(local(top.x(),cx),top.y(),local(top.z(),cz)).equals(new BlockState(1,0))&&after.blockAt(local(east.x(),cx),east.y(),local(east.z(),cz)).equals(new BlockState(77,1))&&after.blockAt(local(west.x(),cx),west.y(),local(west.z(),cz)).equals(new BlockState(77,2))&&after.blockAt(local(south.x(),cx),south.y(),local(south.z(),cz)).equals(new BlockState(77,3))&&after.blockAt(local(north.x(),cx),north.y(),local(north.z(),cz)).equals(new BlockState(77,4)),"persisted stone-button-face drift");
   String evidence="column="+column+",support="+cell(top,1,0)+",east="+cell(east,77,1)+",west="+cell(west,77,2)+",south="+cell(south,77,3)+",north="+cell(north,77,4)+",look=-90+90+0+180,persisted=77:1+77:2+77:3+77:4,clients=2,disconnect=clean";
   String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=raised-stone+button77-east-west-south-north|cause=packet15-item77-east+west+south+north|wire=packet53-button77:1+77:2+77:3+77:4|oracle=wall-attachment-metadata-set+fresh-login|"+evidence;
   System.out.println("WORLDLINE_M399_SET="+evidence);System.out.println("WORLDLINE_M399_TRACE="+trace);System.out.println("WORLDLINE_M399_SIGNATURE="+sha(trace));
  }finally{actor.close();if(reader!=null)reader.close();server.close();}
 }
 private static BlockPosition button(B173WireClient a,BlockPosition support,BlockFace face,int meta,float yaw)throws Exception{BlockPosition target=face.adjacent(support);a.look(yaw,0F);a.placeHeldBlock(support,face);BlockState placed=a.awaitBlock(target,new BlockState(77,meta)).blockAt(target.x(),target.y(),target.z());require(placed.equals(new BlockState(77,meta))&&(placed.metadata()&8)==0,"stone button facing drift 77:"+meta+": "+placed);return target;}
 private static BlockPosition place(B173WireClient a,BlockPosition support,BlockFace face,int id)throws Exception{BlockPosition target=face.adjacent(support);a.placeHeldBlock(support,face);a.awaitBlock(target,new BlockState(id,0));return target;}
 private static BlockPosition foundation(RemoteChunkSnapshot q,int cx,int cz){for(int x=4;x<=11;x++)for(int z=4;z<=11;z++)for(int y=126;y>=1;y--)if(q.blockAt(x,y,z).legacyId()==3&&water(q.blockAt(x,y+1,z).legacyId()))return new BlockPosition(cx*16+x,y,cz*16+z);throw new IllegalStateException("no deterministic wooden-button-set foundation");}
 private static String cell(BlockPosition p,int id,int meta){return p.x()+":"+p.y()+":"+p.z()+":"+id+":"+meta;}
 private static boolean air(RemoteChunkSnapshot q,BlockPosition p,int cx,int cz){return q.blockAt(local(p.x(),cx),p.y(),local(p.z(),cz)).legacyId()==0;}
 private static boolean water(int id){return id==8||id==9;}private static int local(int v,int c){return v-c*16;}private static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}private static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}private static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
