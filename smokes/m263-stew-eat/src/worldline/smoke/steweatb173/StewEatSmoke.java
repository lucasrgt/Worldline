package worldline.smoke.steweatb173;

import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.security.MessageDigest;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Eats official mushroom stew 282 through Packet15 air-use and freezes Packet8 heal plus bowl leftover. */
public final class StewEatSmoke{
 private StewEatSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=7)throw new IllegalArgumentException("usage: StewEatSmoke server.jar workspace port seed username chunkX chunkZ");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]);Duration timeout=Duration.ofSeconds(90);
  B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout),reader=null;RemoteInventoryView inv;RemoteItemStack stew=new RemoteItemStack(282,1,0);RemoteItemStack bowl=new RemoteItemStack(281,1,0);
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,new int[]{0},new int[]{282},new int[]{1},new int[]{0},12);actor.connect();actor.synchronizePose();inv=actor.awaitInventory();require(inv.occupiedSlots()==1&&inv.slot(36).item().equals(stew),"stew inventory drift");require(actor.awaitHealth(12)==12,"seeded stew health drift");actor.awaitRemoteChunk(cx,cz);
   actor.selectHeldSlot(0);require(actor.inventory().slot(36).item().equals(stew)&&actor.health()==12,"pre-eat stew fixture drift");actor.look(0F,0F);actor.useSelectedItemInAir();actor.sustainTicks(20);require(actor.awaitHealth(20)==20,"stew eat health drift");require(actor.inventory().slot(36).item().equals(bowl),"stew bowl leftover drift");actor.close();awaitPlayers(server,0);server.save();
   reader=new B173WireClient("127.0.0.1",port,user,timeout);reader.connect();reader.synchronizePose();require(reader.awaitHealth(20)==20&&reader.awaitInventory().slot(36).item().equals(bowl),"persisted stew eat drift");
   String evidence="health=12->20,heal=8,stew=282:1->281:1,persisted=true,clients=2,disconnect=clean";String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=stew282|cause=packet15-dir255-item282|wire=packet8-health12->20+packet103-stew-bowl281|oracle=itemfood-stew-heal8+bowl-leftover|"+evidence;System.out.println("WORLDLINE_M263_STEW="+evidence);System.out.println("WORLDLINE_M263_TRACE="+trace);System.out.println("WORLDLINE_M263_SIGNATURE="+sha(trace));
  }finally{actor.close();if(reader!=null)reader.close();server.close();}
 }
 private static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}private static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}private static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
