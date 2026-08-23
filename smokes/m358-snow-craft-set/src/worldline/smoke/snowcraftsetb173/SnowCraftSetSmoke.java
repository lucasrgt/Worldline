package worldline.smoke.snowcraftsetb173;

import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.security.MessageDigest;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Crafts snow block 80 from 4 snowballs 332, then shovels snow layer 78 and snow block 80 to 332. */
public final class SnowCraftSetSmoke{
 private static final RemoteItemStack LAYER=new RemoteItemStack(78,1,0),SHOVEL=new RemoteItemStack(284,1,0),DROP1=new RemoteItemStack(332,1,0),DROP4=new RemoteItemStack(332,4,0);
 private static final BlockState AIR=new BlockState(0,0);
 private SnowCraftSetSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=7)throw new IllegalArgumentException("usage: SnowCraftSetSmoke server.jar workspace port seed username chunkX chunkZ");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]);Duration timeout=Duration.ofSeconds(90);
  require(user.length()<=16&&B173SnowCraftClick.SNOW_BLOCK.legacyId()==80&&B173SnowCraftClick.SNOWBALLS.legacyId()==332,"snow-craft-set identities drifted");
  B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout),reader=null;BlockPosition top;int column;String layer,block;
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,new int[]{0,1,2,3,4},new int[]{1,332,78,80,284},new int[]{32,4,1,1,1},new int[]{0,0,0,0,0});actor.connect();actor.synchronizePose();RemoteInventoryView inv=actor.awaitInventory();require(inv.occupiedSlots()==5&&inv.slot(36).item().equals(new RemoteItemStack(1,32,0))&&inv.slot(37).item().equals(B173SnowCraftClick.SNOWBALLS)&&inv.slot(38).item().equals(LAYER)&&inv.slot(39).item().equals(B173SnowCraftClick.SNOW_BLOCK)&&inv.slot(40).item().equals(SHOVEL)&&B173SnowCraftClick.emptyCraft(inv),"snow-craft-set inventory seed drift");
   RemoteChunkSnapshot initial=actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);top=foundation(initial,cx,cz);column=0;actor.selectHeldSlot(0);
   while(water(initial.blockAt(local(top.x(),cx),top.y()+1,local(top.z(),cz)).legacyId())){top=place(actor,top,BlockFace.UP,1);actor.moveAndObserve(0D,1D,0D,1);require(++column<=15,"water column exceeded snow-craft-set fixture");}for(int lift=0;lift<8;lift++){top=place(actor,top,BlockFace.UP,1);actor.moveAndObserve(0D,1D,0D,1);column++;}
   BlockPosition east=place(actor,top,BlockFace.EAST,1),west=place(actor,top,BlockFace.WEST,1);
   layer=harvest(actor,east,2,4,78,DROP1,"layer");block=harvest(actor,west,3,4,80,DROP1,"block");
   B173SnowCraftClick.apply(actor);requireCraft(actor.inventory());actor.close();awaitPlayers(server,0);server.save();require(server.player(user).inventoryItems()>=3,"snow-craft-set persistence count drift");
   reader=new B173WireClient("127.0.0.1",port,user,timeout);reader.connect();reader.synchronizePose();requireCraft(reader.awaitInventory());
   String evidence="column="+column+",support="+top.x()+":"+top.y()+":"+top.z()+":1:0,craft=80x1-from-332x4,"+layer+","+block+",shovel=284,persisted=true,clients=2,disconnect=clean";String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=personal-2x2-snowball332-to-snowblock80+snowlayer78+snowblock80|cause=packet102-window0-4x332-to-80+packet14-goldshovel284|wire=packet106-accepted+packet53-air+packet21-id332|oracle=craft-80+shovel-78-and-80-to-332+fresh-login|"+evidence;System.out.println("WORLDLINE_M358_SET="+evidence);System.out.println("WORLDLINE_M358_TRACE="+trace);System.out.println("WORLDLINE_M358_SIGNATURE="+sha(trace));
  }finally{actor.close();if(reader!=null)reader.close();server.close();}
 }
 private static String harvest(B173WireClient a,BlockPosition support,int placeSlot,int shovelSlot,int id,RemoteItemStack expected,String name)throws Exception{
  a.selectHeldSlot(placeSlot);BlockPosition cell=place(a,support,BlockFace.UP,id);require(worldline.test.WorldlineSmokeAwait.observe(a,5).blockAt(cell.x(),cell.y(),cell.z()).equals(new BlockState(id,0)),"live block "+id+" drift");
  a.selectHeldSlot(shovelSlot);worldline.test.WorldlineSmokeAwait.observe(a,2);a.beginBreak(cell);worldline.test.WorldlineSmokeAwait.observe(a,5);a.finishBreak(cell);a.awaitBlock(cell,AIR);
  RemoteDroppedItem drop=a.peekDroppedItem(expected);if(drop==null)drop=a.awaitDroppedItem(expected);
  require(drop.item().legacyId()==332&&drop.item().count()>=1&&worldline.test.WorldlineSmokeAwait.observe(a,1).blockAt(cell.x(),cell.y(),cell.z()).equals(AIR),"Packet21 332 drop or cell "+id+"->0 absent");
  return name+"="+cell.x()+":"+cell.y()+":"+cell.z()+":"+id+":0->0:0,drop=packet21-332:"+drop.item().count()+":"+drop.item().damage();
 }
 private static void requireCraft(RemoteInventoryView view){require(!view.slot(37).empty()&&view.slot(37).item().equals(B173SnowCraftClick.SNOW_BLOCK)&&view.slot(37).item().legacyId()==80&&!view.slot(40).empty()&&view.slot(40).item().legacyId()==284,"snow-craft-set crafted 80 inventory drift");}
 private static BlockPosition place(B173WireClient a,BlockPosition support,BlockFace face,int id)throws Exception{BlockPosition target=face.adjacent(support);a.placeHeldBlock(support,face);a.awaitBlock(target,new BlockState(id,0));return target;}
 private static BlockPosition foundation(RemoteChunkSnapshot q,int cx,int cz){for(int x=4;x<=11;x++)for(int z=4;z<=11;z++)for(int y=126;y>=1;y--)if(q.blockAt(x,y,z).legacyId()==3&&water(q.blockAt(x,y+1,z).legacyId()))return new BlockPosition(cx*16+x,y,cz*16+z);throw new IllegalStateException("no deterministic snow-craft-set foundation");}
 private static boolean water(int id){return id==8||id==9;}private static int local(int v,int c){return v-c*16;}private static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}private static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}private static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
