package worldline.smoke.remainingfoodeatb173;

import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.security.MessageDigest;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Packet15 air-use eat of apple 260, cooked pork 320, and golden apple 322 as one SET. */
public final class RemainingFoodEatSmoke{
 private RemainingFoodEatSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=7)throw new IllegalArgumentException("usage: RemainingFoodEatSmoke server.jar workspace port seed username chunkX chunkZ");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]);
  require(seed==17320110707L&&user.equals("FoodEat374")&&user.length()<=16&&cx==0&&cz==0,"remaining-food-eat identity drift");
  Duration timeout=Duration.ofSeconds(90);B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=null,reader=null;
  RemoteItemStack apple=new RemoteItemStack(260,1,0),pork=new RemoteItemStack(320,1,0),golden=new RemoteItemStack(322,1,0);
  try{server.boot();
   actor=session(workspace,user,port,timeout,new int[]{0,1,2},new int[]{260,320,322},new int[]{1,1,1},new int[]{0,0,0},16);
   require(actor.awaitInventory().occupiedSlots()==3&&actor.inventory().slot(36).item().equals(apple)&&actor.inventory().slot(37).item().equals(pork)&&actor.inventory().slot(38).item().equals(golden),"remaining-food-eat inventory drift");
   eat(actor,0,apple,16,20);actor.close();awaitPlayers(server,0);
   actor=session(workspace,user,port,timeout,new int[]{1,2},new int[]{320,322},new int[]{1,1},new int[]{0,0},12);
   eat(actor,1,pork,12,20);actor.close();awaitPlayers(server,0);
   actor=session(workspace,user,port,timeout,new int[]{2},new int[]{322},new int[]{1},new int[]{0},10);
   eat(actor,2,golden,10,20);actor.close();awaitPlayers(server,0);server.save();
   reader=new B173WireClient("127.0.0.1",port,user,timeout);reader.connect();reader.synchronizePose();RemoteInventoryView after=reader.awaitInventory();
   require(reader.awaitHealth(20)==20&&after.slot(36).empty()&&after.slot(37).empty()&&after.slot(38).empty(),"persisted remaining-food-eat drift");
   String evidence="apple=260:1:0->empty,health=16->20,heal=4,pork=320:1:0->empty,health=12->20,heal=8,golden=322:1:0->empty,health=10->20,heal=20,persisted=true,clients=2,disconnect=clean";
   String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=apple260+cookedpork320+golden322|cause=packet15-dir255-item260+packet15-dir255-item320+packet15-dir255-item322|wire=packet8-health16->20+12->20+10->20+packet103-empty-260+320+322|oracle=itemfood-apple-heal4+pork-heal8+golden-heal20+stack-consume+fresh-login|"+evidence;
   System.out.println("WORLDLINE_M374_SET="+evidence);System.out.println("WORLDLINE_M374_TRACE="+trace);System.out.println("WORLDLINE_M374_SIGNATURE="+sha(trace));
  }finally{if(actor!=null)actor.close();if(reader!=null)reader.close();server.close();}
 }
 private static B173WireClient session(Path workspace,String user,int port,Duration timeout,int[]slots,int[]ids,int[]counts,int[]damages,int health)throws Exception{
  B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,slots,ids,counts,damages,health);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout);actor.connect();actor.synchronizePose();actor.awaitInventory();require(actor.awaitHealth(health)==health,"seeded remaining-food-eat health drift health="+actor.health());return actor;}
 private static void eat(B173WireClient a,int slot,RemoteItemStack item,int from,int to)throws Exception{a.selectHeldSlot(slot);require(a.health()==from&&a.inventory().slot(36+slot).item().equals(item),"pre-eat "+item.legacyId()+" drift health="+a.health());a.look(0F,0F);a.useSelectedItemInAir();a.sustainTicks(20);require(a.awaitHealth(to)==to,"eat "+item.legacyId()+" health drift health="+a.health());for(int n=0;n<40&&!a.inventory().slot(36+slot).empty();n++)a.sustainTicks(1);require(a.inventory().slot(36+slot).empty()&&a.health()==to,"eat "+item.legacyId()+" consume drift");}
 private static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}private static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}private static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
