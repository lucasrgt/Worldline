package worldline.smoke.remainingfoodrestsetb173;

import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.security.MessageDigest;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Packet15 air-use eat of cookie 357 and mushroom stew 282 as the remaining-food rest SET. */
public final class RemainingFoodRestSetSmoke{
 private RemainingFoodRestSetSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=7)throw new IllegalArgumentException("usage: RemainingFoodRestSetSmoke server.jar workspace port seed username chunkX chunkZ");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]);
  require(seed==17320110707L&&user.equals("FoodRst441")&&user.length()<=16&&cx==0&&cz==0,"remaining-food-rest-set identity drift");
  Duration timeout=Duration.ofSeconds(90);B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=null,reader=null;
  RemoteItemStack cookie=new RemoteItemStack(357,1,0),stew=new RemoteItemStack(282,1,0),bowl=new RemoteItemStack(281,1,0);
  try{server.boot();
   actor=session(workspace,user,port,timeout,new int[]{0,1},new int[]{357,282},new int[]{1,1},new int[]{0,0},19);
   require(actor.awaitInventory().occupiedSlots()==2&&actor.inventory().slot(36).item().equals(cookie)&&actor.inventory().slot(37).item().equals(stew),"remaining-food-rest-set inventory drift");
   eat(actor,0,cookie,null,19,20);actor.close();awaitPlayers(server,0);
   actor=session(workspace,user,port,timeout,new int[]{1},new int[]{282},new int[]{1},new int[]{0},12);
   eat(actor,1,stew,bowl,12,20);actor.close();awaitPlayers(server,0);server.save();
   reader=new B173WireClient("127.0.0.1",port,user,timeout);reader.connect();reader.synchronizePose();RemoteInventoryView after=reader.awaitInventory();
   require(reader.awaitHealth(20)==20&&after.slot(36).empty()&&after.slot(37).item().equals(bowl),"persisted remaining-food-rest-set drift");
   String evidence="cookie=357:1:0->empty,health=19->20,heal=1,stew=282:1:0->281:1:0,health=12->20,heal=8,persisted=true,clients=2,disconnect=clean";
   String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=cookie357+stew282|cause=packet15-dir255-item357+packet15-dir255-item282|wire=packet8-health19->20+12->20+packet103-empty-357+bowl-281|oracle=itemfood-cookie-heal1+stew-heal8+bowl-leftover+fresh-login|"+evidence;
   System.out.println("WORLDLINE_M441_SET="+evidence);System.out.println("WORLDLINE_M441_TRACE="+trace);System.out.println("WORLDLINE_M441_SIGNATURE="+sha(trace));
  }finally{if(actor!=null)actor.close();if(reader!=null)reader.close();server.close();}
 }
 private static B173WireClient session(Path workspace,String user,int port,Duration timeout,int[]slots,int[]ids,int[]counts,int[]damages,int health)throws Exception{
  B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,slots,ids,counts,damages,health);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout);actor.connect();actor.synchronizePose();actor.awaitInventory();require(actor.awaitHealth(health)==health,"seeded remaining-food-rest-set health drift health="+actor.health());return actor;}
 private static void eat(B173WireClient a,int slot,RemoteItemStack item,RemoteItemStack leftover,int from,int to)throws Exception{a.selectHeldSlot(slot);require(a.health()==from&&a.inventory().slot(36+slot).item().equals(item),"pre-eat "+item.legacyId()+" drift health="+a.health());a.look(0F,0F);a.useSelectedItemInAir();a.sustainTicks(20);require(a.awaitHealth(to)==to,"eat "+item.legacyId()+" health drift health="+a.health());if(leftover==null){for(int n=0;n<40&&!a.inventory().slot(36+slot).empty();n++)a.sustainTicks(1);require(a.inventory().slot(36+slot).empty()&&a.health()==to,"eat "+item.legacyId()+" consume drift");}else{for(int n=0;n<40&&(a.inventory().slot(36+slot).empty()||!a.inventory().slot(36+slot).item().equals(leftover));n++)a.sustainTicks(1);require(a.inventory().slot(36+slot).item().equals(leftover)&&a.health()==to,"eat "+item.legacyId()+" leftover drift");}}
 private static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}private static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}private static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
