package worldline.smoke.cookedfisheatb173;

import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.security.MessageDigest;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Eats official cooked fish 350 through Packet15 air-use and freezes Packet8 heal plus stack consume. */
public final class CookedFishEatSmoke{
 private CookedFishEatSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=7)throw new IllegalArgumentException("usage: CookedFishEatSmoke server.jar workspace port seed username chunkX chunkZ");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]);Duration timeout=Duration.ofSeconds(90);
  B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout),reader=null;RemoteInventoryView inv;RemoteItemStack fish=new RemoteItemStack(350,1,0);
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,new int[]{0},new int[]{350},new int[]{1},new int[]{0},15);actor.connect();actor.synchronizePose();inv=actor.awaitInventory();require(inv.occupiedSlots()==1&&inv.slot(36).item().equals(fish),"cooked fish inventory drift");require(actor.awaitHealth(15)==15,"seeded cooked fish health drift");actor.awaitRemoteChunk(cx,cz);
   actor.selectHeldSlot(0);require(actor.inventory().slot(36).item().equals(fish)&&actor.health()==15,"pre-eat cooked fish fixture drift");actor.look(0F,0F);actor.useSelectedItemInAir();actor.sustainTicks(20);require(actor.awaitHealth(20)==20,"cooked fish eat health drift");require(actor.inventory().slot(36).empty(),"cooked fish stack consume drift");actor.close();awaitPlayers(server,0);server.save();
   reader=new B173WireClient("127.0.0.1",port,user,timeout);reader.connect();reader.synchronizePose();require(reader.awaitHealth(20)==20&&reader.awaitInventory().slot(36).empty(),"persisted cooked fish eat drift");
   String evidence="health=15->20,heal=5,cookedfish=350:1->0,persisted=true,clients=2,disconnect=clean";String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=cookedfish350|cause=packet15-dir255-item350|wire=packet8-health15->20+packet103-cookedfish-empty|oracle=itemfood-cookedfish-heal5+stack-consume|"+evidence;System.out.println("WORLDLINE_M266_COOKED="+evidence);System.out.println("WORLDLINE_M266_TRACE="+trace);System.out.println("WORLDLINE_M266_SIGNATURE="+sha(trace));
  }finally{actor.close();if(reader!=null)reader.close();server.close();}
 }
 private static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}private static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}private static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
