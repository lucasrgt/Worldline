package worldline.smoke.remaininglightsetb173;

import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.security.MessageDigest;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Places official glowstone 89, jack-o-lantern 91, and floor torch 50 together and freezes persistence. */
public final class RemainingLightSetSmoke{
 private RemainingLightSetSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=7)throw new IllegalArgumentException("usage: RemainingLightSetSmoke server.jar workspace port seed username chunkX chunkZ");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]);
  require(seed==17320110707L&&user.equals("LightSet387")&&user.length()<=16,"remaining-light-set identity drift");
  Duration timeout=Duration.ofSeconds(90);B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout),reader=null;
  BlockPosition top,west,east,glow,lantern,torch;int column;BlockState glowPlaced=new BlockState(89,0),jackPlaced=new BlockState(91,1),torchPlaced=new BlockState(50,5);
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,new int[]{0,1,2,3},new int[]{1,89,91,50},new int[]{32,1,1,1},new int[]{0,0,0,0});actor.connect();actor.synchronizePose();require(actor.awaitInventory().occupiedSlots()==4,"remaining-light-set inventory drift");RemoteChunkSnapshot initial=actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);top=foundation(initial,cx,cz);column=0;actor.selectHeldSlot(0);
   while(water(initial.blockAt(local(top.x(),cx),top.y()+1,local(top.z(),cz)).legacyId())){top=place(actor,top,BlockFace.UP,1);actor.moveAndObserve(0D,1D,0D,1);require(++column<=15,"water column exceeded remaining-light-set fixture");}for(int lift=0;lift<8;lift++){top=place(actor,top,BlockFace.UP,1);actor.moveAndObserve(0D,1D,0D,1);column++;}
   west=place(actor,top,BlockFace.WEST,1);east=place(actor,top,BlockFace.EAST,1);
   actor.selectHeldSlot(1);glow=BlockFace.UP.adjacent(top);actor.placeHeldBlock(top,BlockFace.UP);actor.awaitBlock(glow,glowPlaced);
   actor.selectHeldSlot(2);actor.look(-90F,0F);lantern=BlockFace.UP.adjacent(west);actor.placeHeldBlock(west,BlockFace.UP);actor.awaitBlock(lantern,jackPlaced);
   actor.selectHeldSlot(3);torch=BlockFace.UP.adjacent(east);actor.placeHeldBlock(east,BlockFace.UP);actor.awaitBlock(torch,torchPlaced);
   RemoteWorldView live=actor.sustainTicks(5);require(live.blockAt(glow.x(),glow.y(),glow.z()).equals(glowPlaced)&&live.blockAt(lantern.x(),lantern.y(),lantern.z()).equals(jackPlaced)&&live.blockAt(torch.x(),torch.y(),torch.z()).equals(torchPlaced),"live remaining-light-set drift");
   actor.close();awaitPlayers(server,0);server.save();
   reader=new B173WireClient("127.0.0.1",port,user,timeout);reader.connect();reader.synchronizePose();RemoteChunkSnapshot after=reader.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);
   require(after.blockAt(local(top.x(),cx),top.y(),local(top.z(),cz)).equals(new BlockState(1,0))&&after.blockAt(local(west.x(),cx),west.y(),local(west.z(),cz)).equals(new BlockState(1,0))&&after.blockAt(local(east.x(),cx),east.y(),local(east.z(),cz)).equals(new BlockState(1,0))&&after.blockAt(local(glow.x(),cx),glow.y(),local(glow.z(),cz)).equals(glowPlaced)&&after.blockAt(local(lantern.x(),cx),lantern.y(),local(lantern.z(),cz)).equals(jackPlaced)&&after.blockAt(local(torch.x(),cx),torch.y(),local(torch.z(),cz)).equals(torchPlaced),"persisted remaining-light-set drift");
   String evidence="column="+column+",support="+top.x()+":"+top.y()+":"+top.z()+":1:0,glowstone="+glow.x()+":"+glow.y()+":"+glow.z()+":89:0,west="+west.x()+":"+west.y()+":"+west.z()+":1:0,jackolantern="+lantern.x()+":"+lantern.y()+":"+lantern.z()+":91:1,east="+east.x()+":"+east.y()+":"+east.z()+":1:0,torch="+torch.x()+":"+torch.y()+":"+torch.z()+":50:5,look=-90:0,persisted=true,clients=2,disconnect=clean";
   String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=raised-stone+glowstone89+jackolantern91+torch50|cause=packet15-item89+packet15-item91+look-90+packet15-item50|wire=packet53-glowstone89:0+packet53-jackolantern91:1+packet53-torch50:5|oracle=live-light-set89+91+50+fresh-login|"+evidence;
   System.out.println("WORLDLINE_M387_SET="+evidence);System.out.println("WORLDLINE_M387_TRACE="+trace);System.out.println("WORLDLINE_M387_SIGNATURE="+sha(trace));
  }finally{actor.close();if(reader!=null)reader.close();server.close();}
 }
 private static BlockPosition place(B173WireClient a,BlockPosition support,BlockFace face,int id)throws Exception{BlockPosition target=face.adjacent(support);a.placeHeldBlock(support,face);a.awaitBlock(target,new BlockState(id,0));return target;}
 private static BlockPosition foundation(RemoteChunkSnapshot q,int cx,int cz){for(int x=4;x<=11;x++)for(int z=4;z<=11;z++)for(int y=126;y>=1;y--)if(q.blockAt(x,y,z).legacyId()==3&&water(q.blockAt(x,y+1,z).legacyId()))return new BlockPosition(cx*16+x,y,cz*16+z);throw new IllegalStateException("no deterministic remaining-light-set foundation");}
 private static boolean water(int id){return id==8||id==9;}private static int local(int v,int c){return v-c*16;}private static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}private static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}private static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
