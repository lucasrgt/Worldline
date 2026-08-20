package worldline.smoke.dyewoolcraftsb173;

import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.security.MessageDigest;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Crafts three dyed wool damages from white wool 35:0 plus dyes in the personal 2x2 grid. */
public final class DyeWoolCraftsSmoke{
 private DyeWoolCraftsSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=7)throw new IllegalArgumentException("usage: DyeWoolCraftsSmoke server.jar workspace port seed username chunkX chunkZ");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]);Duration timeout=Duration.ofSeconds(90);
  B173DyeWoolCraft.verify();B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout),reader=null;
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,4.5D,72D,4.5D,new int[]{0,1,2,3,4,5},new int[]{35,35,35,351,351,351},new int[]{1,1,1,1,1,1},new int[]{0,0,0,1,2,4});actor.connect();actor.synchronizePose();RemoteInventoryView inv=actor.awaitInventory();require(inv.occupiedSlots()==6&&inv.slot(36).item().equals(new RemoteItemStack(35,1,0))&&inv.slot(39).item().equals(new RemoteItemStack(351,1,1))&&inv.slot(40).item().equals(new RemoteItemStack(351,1,2))&&inv.slot(41).item().equals(new RemoteItemStack(351,1,4)),"white wool and dye inventory drift");actor.awaitRemoteChunk(cx,cz);B173DyeWoolCraft.apply(actor);require(actor.inventory().slot(36).item().equals(new RemoteItemStack(35,1,14))&&actor.inventory().slot(37).item().equals(new RemoteItemStack(35,1,13))&&actor.inventory().slot(38).item().equals(new RemoteItemStack(35,1,11))&&actor.inventory().occupiedSlots()==3,"live dyed-wool 2x2 results drifted");actor.close();awaitPlayers(server,0);server.save();
   reader=new B173WireClient("127.0.0.1",port,user,timeout);reader.connect();reader.synchronizePose();RemoteInventoryView after=reader.awaitInventory();require(after.occupiedSlots()==3&&after.slot(36).item().equals(new RemoteItemStack(35,1,14))&&after.slot(37).item().equals(new RemoteItemStack(35,1,13))&&after.slot(38).item().equals(new RemoteItemStack(35,1,11)),"persisted dyed-wool crafts drifted");
   String evidence="wool=35:0,dyes=351:1+351:2+351:4,results=35:14+35:13+35:11,grid=2x2,actions=18,persisted=true,clients=2,disconnect=clean";String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=white-wool35:0+dyes351|cause=packet102-window0-2x2-shapeless|wire=packet106-accepted|oracle=three-wool-damages+fresh-login|"+evidence;System.out.println("WORLDLINE_M315_CRAFT="+evidence);System.out.println("WORLDLINE_M315_TRACE="+trace);System.out.println("WORLDLINE_M315_SIGNATURE="+sha(trace));
  }finally{actor.close();if(reader!=null)reader.close();server.close();}
 }
 private static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}private static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}private static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
