package worldline.smoke.remainingslabplaceb173;

import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.security.MessageDigest;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Places remaining slabs 44:1, 44:2, 44:3 and double slab 43:0 on raised stone as one SET. */
public final class RemainingSlabPlaceSmoke{
 private RemainingSlabPlaceSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=7)throw new IllegalArgumentException("usage: RemainingSlabPlaceSmoke server.jar workspace port seed username chunkX chunkZ");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]);
  require(seed==17320110707L&&user.equals("SlabPlace394")&&user.length()<=16,"remaining-slab-place identity drift");
  Duration timeout=Duration.ofSeconds(90);B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout),reader=null;
  BlockPosition top,east,west,south,sandstone,wood,cobble,dbl;int column;BlockState ss=new BlockState(44,1),wd=new BlockState(44,2),cb=new BlockState(44,3),ds=new BlockState(43,0),stone=new BlockState(1,0);
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,new int[]{0,1,2,3,4},new int[]{1,44,44,44,43},new int[]{32,1,1,1,1},new int[]{0,1,2,3,0});actor.connect();actor.synchronizePose();
   require(actor.awaitInventory().occupiedSlots()==5&&actor.awaitInventory().slot(37).item().equals(new RemoteItemStack(44,1,1))&&actor.awaitInventory().slot(38).item().equals(new RemoteItemStack(44,1,2))&&actor.awaitInventory().slot(39).item().equals(new RemoteItemStack(44,1,3))&&actor.awaitInventory().slot(40).item().equals(new RemoteItemStack(43,1,0)),"remaining-slab-place inventory drift");
   RemoteChunkSnapshot initial=actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);top=foundation(initial,cx,cz);column=0;actor.selectHeldSlot(0);
   while(water(initial.blockAt(local(top.x(),cx),top.y()+1,local(top.z(),cz)).legacyId())){top=place(actor,top,BlockFace.UP,1,0);actor.moveAndObserve(0D,1D,0D,1);require(++column<=15,"water column exceeded remaining-slab-place fixture");}for(int lift=0;lift<8;lift++){top=place(actor,top,BlockFace.UP,1,0);actor.moveAndObserve(0D,1D,0D,1);column++;}
   east=place(actor,top,BlockFace.EAST,1,0);west=place(actor,top,BlockFace.WEST,1,0);south=place(actor,top,BlockFace.SOUTH,1,0);
   actor.selectHeldSlot(1);sandstone=place(actor,top,BlockFace.UP,44,1);actor.selectHeldSlot(2);wood=place(actor,east,BlockFace.UP,44,2);actor.selectHeldSlot(3);cobble=place(actor,west,BlockFace.UP,44,3);actor.selectHeldSlot(4);dbl=place(actor,south,BlockFace.UP,43,0);
   RemoteWorldView live=actor.sustainTicks(5);require(live.blockAt(sandstone.x(),sandstone.y(),sandstone.z()).equals(ss)&&live.blockAt(wood.x(),wood.y(),wood.z()).equals(wd)&&live.blockAt(cobble.x(),cobble.y(),cobble.z()).equals(cb)&&live.blockAt(dbl.x(),dbl.y(),dbl.z()).equals(ds),"live remaining-slab-place drift");
   actor.close();awaitPlayers(server,0);server.save();reader=new B173WireClient("127.0.0.1",port,user,timeout);reader.connect();reader.synchronizePose();RemoteChunkSnapshot after=reader.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);
   require(after.blockAt(local(top.x(),cx),top.y(),local(top.z(),cz)).equals(stone)&&after.blockAt(local(sandstone.x(),cx),sandstone.y(),local(sandstone.z(),cz)).equals(ss)&&after.blockAt(local(wood.x(),cx),wood.y(),local(wood.z(),cz)).equals(wd)&&after.blockAt(local(cobble.x(),cx),cobble.y(),local(cobble.z(),cz)).equals(cb)&&after.blockAt(local(dbl.x(),cx),dbl.y(),local(dbl.z(),cz)).equals(ds),"persisted remaining-slab-place drift");
   String evidence="column="+column+",support="+cell(top,1,0)+",sandstone="+cell(sandstone,44,1)+",wood="+cell(wood,44,2)+",cobble="+cell(cobble,44,3)+",double="+cell(dbl,43,0)+",persisted=true,clients=2,disconnect=clean";
   String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=raised-stone+slab44:1+slab44:2+slab44:3+slab43:0|cause=packet15-item44:1+item44:2+item44:3+item43:0|wire=packet53-slab44:1+44:2+44:3+43:0|oracle=remaining-slab-place-metadata+fresh-login|"+evidence;
   System.out.println("WORLDLINE_M394_SET="+evidence);System.out.println("WORLDLINE_M394_TRACE="+trace);System.out.println("WORLDLINE_M394_SIGNATURE="+sha(trace));
  }finally{actor.close();if(reader!=null)reader.close();server.close();}
 }
 private static BlockPosition place(B173WireClient a,BlockPosition support,BlockFace face,int id,int meta)throws Exception{BlockPosition target=face.adjacent(support);a.placeHeldBlock(support,face);a.awaitBlock(target,new BlockState(id,meta));return target;}
 private static BlockPosition foundation(RemoteChunkSnapshot q,int cx,int cz){for(int x=4;x<=11;x++)for(int z=4;z<=11;z++)for(int y=126;y>=1;y--)if(q.blockAt(x,y,z).legacyId()==3&&water(q.blockAt(x,y+1,z).legacyId()))return new BlockPosition(cx*16+x,y,cz*16+z);throw new IllegalStateException("no deterministic remaining-slab-place foundation");}
 private static String cell(BlockPosition p,int id,int meta){return p.x()+":"+p.y()+":"+p.z()+":"+id+":"+meta;}
 private static boolean water(int id){return id==8||id==9;}private static int local(int v,int c){return v-c*16;}private static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}private static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}private static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
