package worldline.smoke.playerdeathdropssetb173;

import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.security.MessageDigest;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Seeds three hotbar stacks, dies to vanilla void Packet8 health 0, and observes those Packet21 ids. */
public final class PlayerDeathDropsSetSmoke{
 private static final RemoteItemStack STONE=new RemoteItemStack(1,1,0),COBBLE=new RemoteItemStack(4,1,0),DIRT=new RemoteItemStack(3,1,0);
 private PlayerDeathDropsSetSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=6)throw new IllegalArgumentException("usage: PlayerDeathDropsSetSmoke server.jar workspace port seed username fixtureTicks");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int fixtureTicks=Integer.parseInt(a[5]);
  require(seed==17320110707L&&user.equals("DeathDrop453")&&user.length()<=16&&fixtureTicks>=1&&fixtureTicks<=400,"player-death-drops-set identity drift");
  Duration timeout=Duration.ofSeconds(90);B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout);
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,8.5D,-80D,8.5D,new int[]{0,1,2},new int[]{1,4,3},new int[]{1,1,1},new int[]{0,0,0});actor.connect();actor.synchronizePose();
   require(actor.awaitHealth(20)==20,"player-death-drops-set baseline Packet8 health 20 absent");
   require(actor.awaitInventory().occupiedSlots()==3&&has(actor,36,STONE)&&has(actor,37,COBBLE)&&has(actor,38,DIRT),"player-death-drops-set inventory drift");
   worldline.test.WorldlineSmokeAwait.awaitEntity(actor,actor::health,h->h<=0,"void death",fixtureTicks);require(actor.health()<=0,"vanilla void Packet8 health 0 absent: "+actor.health());
   if(actor.health()==0)require(actor.awaitHealth(0)==0,"packet8 health 0 drift");
   RemoteDroppedItem[] drops=B173PlayerDeathDrops.await(actor,STONE,COBBLE,DIRT);RemoteDroppedItem stone=drops[0],cobble=drops[1],dirt=drops[2];
   require(stone.item().equals(STONE)&&stone.item().legacyId()==1&&cobble.item().equals(COBBLE)&&cobble.item().legacyId()==4&&dirt.item().equals(DIRT)&&dirt.item().legacyId()==3&&stone.entityId()!=cobble.entityId()&&stone.entityId()!=dirt.entityId()&&cobble.entityId()!=dirt.entityId(),"player-death Packet21 hotbar drops absent");
   actor.close();awaitPlayers(server,0);server.save();
   String evidence="health=20->0,death=seeded-below-world+vanilla-void-damage+packet8-0,hotbar=1+4+3,drops=packet21-1+packet21-4+packet21-3,clients=1,disconnect=clean";
   String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=seeded-y-80+hotbar-stone1+cobble4+dirt3|cause=vanilla-void-damage|wire=packet8-health20->0+packet21-1+packet21-4+packet21-3|oracle=player-death-multi-item-drops-not-mob-drops|"+evidence;
   System.out.println("WORLDLINE_M453_SET="+evidence);System.out.println("WORLDLINE_M453_TRACE="+trace);System.out.println("WORLDLINE_M453_SIGNATURE="+sha(trace));
  }finally{actor.close();server.close();}
 }
 private static boolean has(B173WireClient a,int slot,RemoteItemStack item){return !a.inventory().slot(slot).empty()&&a.inventory().slot(slot).item().equals(item);}
 private static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}
 private static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}
 private static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
