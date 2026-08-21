package worldline.smoke.remainingclockmapsetb173;

import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.security.MessageDigest;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Crafts and holds clock 347, then Packet15 air-uses empty map 358 which official protocol-14 does not fill. */
public final class RemainingClockMapSetSmoke{
 private static final RemoteItemStack MAP=new RemoteItemStack(358,1,0);
 private RemainingClockMapSetSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=7)throw new IllegalArgumentException("usage: RemainingClockMapSetSmoke server.jar workspace port seed username chunkX chunkZ");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]);
  require(seed==17320110707L&&user.equals("ClockMap438")&&user.length()<=16&&B173RemainingClockCraft.CLOCK.legacyId()==347&&MAP.legacyId()==358,"remaining-clock-map identity drift");
  Duration timeout=Duration.ofSeconds(90);B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout),reader=null;
  BlockPosition top,bench;int column;String before,filled;
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,new int[]{0,1,2,3,4},new int[]{1,58,266,331,358},new int[]{32,1,4,1,1},new int[]{0,0,0,0,0});actor.connect();actor.synchronizePose();
   require(actor.awaitInventory().occupiedSlots()==5&&actor.awaitInventory().slot(38).item().equals(new RemoteItemStack(266,4,0))&&actor.awaitInventory().slot(39).item().equals(new RemoteItemStack(331,1,0))&&actor.awaitInventory().slot(40).item().equals(MAP),"remaining-clock-map inventory seed drift");
   RemoteChunkSnapshot initial=actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);top=foundation(initial,cx,cz);column=0;actor.selectHeldSlot(0);
   while(water(initial.blockAt(local(top.x(),cx),top.y()+1,local(top.z(),cz)).legacyId())){top=place(actor,top,BlockFace.UP,1);actor.moveAndObserve(0D,1D,0D,1);require(++column<=15,"water column exceeded remaining-clock-map fixture");}for(int lift=0;lift<8;lift++){top=place(actor,top,BlockFace.UP,1);actor.moveAndObserve(0D,1D,0D,1);column++;}
   actor.selectHeldSlot(1);bench=place(actor,top,BlockFace.UP,58);actor.sustainTicks(5);actor.selectHeldSlot(1);actor.openWorkbench(bench,BlockFace.UP);B173RemainingClockCraft.apply(actor);requireCraft(actor.inventory());actor.closeWindow();
   actor.selectHeldSlot(1);requireHeld(actor.inventory());actor.selectHeldSlot(4);require(actor.inventory().slot(40).item().equals(MAP),"pre-use remaining empty map 358 absent");before=stack(actor.inventory().slot(40));actor.look(0F,0F);actor.useSelectedItemInAir();
   filled=before;for(int n=0;n<40;n++){actor.sustainTicks(1);filled=stack(actor.inventory().slot(40));if(!filled.equals(before))break;}
   require(before.equals("358:1:0")&&filled.equals(before),"official server filled empty map 358; freeze the no-fill fact");
   actor.selectHeldSlot(1);requireHeld(actor.inventory());actor.close();awaitPlayers(server,0);server.save();require(server.player(user).inventoryItems()==3,"remaining-clock-map persistence count drift");
   reader=new B173WireClient("127.0.0.1",port,user,timeout);reader.connect();reader.synchronizePose();requireHeld(reader.awaitInventory());require(stack(reader.inventory().slot(40)).equals(filled),"persisted remaining empty-map 358 drift");
   RemoteChunkSnapshot after=reader.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);require(after.blockAt(local(top.x(),cx),top.y(),local(top.z(),cz)).equals(new BlockState(1,0))&&after.blockAt(local(bench.x(),cx),bench.y(),local(bench.z(),cz)).equals(new BlockState(58,0)),"persisted remaining-clock-map workbench drift");
   String evidence="column="+column+",support="+top.x()+":"+top.y()+":"+top.z()+":1:0,workbench="+bench.x()+":"+bench.y()+":"+bench.z()+":58:0,clock=347,held=347,map=358,filled="+before+"->"+filled+",persisted=true,clients=2,disconnect=clean";
   String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=workbench58+gold266x4+redstone331x1+emptymap358|cause=packet102-craft-347+packet16-hold-347+packet15-dir255-item358|wire=result347+packet103-held-347+packet103-held-358:1:0|oracle=clock-craft-hold+map-air-use-unfilled+fresh-login-not-m365-not-m366|"+evidence;
   System.out.println("WORLDLINE_M438_SET="+evidence);System.out.println("WORLDLINE_M438_TRACE="+trace);System.out.println("WORLDLINE_M438_SIGNATURE="+sha(trace));
  }finally{actor.close();if(reader!=null)reader.close();server.close();}
 }
 private static void requireCraft(RemoteInventoryView view){require(view.slot(37).item().equals(B173RemainingClockCraft.CLOCK)&&view.slot(40).item().equals(MAP)&&view.occupiedSlots()==3,"remaining-clock crafted inventory drift");}
 private static void requireHeld(RemoteInventoryView view){require(!view.slot(37).empty()&&view.slot(37).item().equals(B173RemainingClockCraft.CLOCK)&&B173RemainingClockCraft.CLOCK.legacyId()==347,"held clock 347 drift");}
 private static String stack(RemoteInventorySlot s){return s.empty()?"empty":s.item().legacyId()+":"+s.item().count()+":"+s.item().damage();}
 private static BlockPosition place(B173WireClient a,BlockPosition support,BlockFace face,int id)throws Exception{BlockPosition target=face.adjacent(support);a.placeHeldBlock(support,face);a.awaitBlock(target,new BlockState(id,0));return target;}
 private static BlockPosition foundation(RemoteChunkSnapshot q,int cx,int cz){for(int x=4;x<=11;x++)for(int z=4;z<=11;z++)for(int y=126;y>=1;y--)if(q.blockAt(x,y,z).legacyId()==3&&water(q.blockAt(x,y+1,z).legacyId()))return new BlockPosition(cx*16+x,y,cz*16+z);throw new IllegalStateException("no deterministic remaining-clock-map foundation");}
 private static boolean water(int id){return id==8||id==9;}private static int local(int v,int c){return v-c*16;}private static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}private static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}private static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
