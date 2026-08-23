package worldline.smoke.remainingbucketrestsetb173;

import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.security.MessageDigest;import java.time.Duration;import java.util.*;
import worldline.api.*;import worldline.b173server.*;

/** Empty bucket 325 picks up source water 9 and lava 11, and rejects flowing 8/9 and 10/11. */
public final class RemainingBucketRestSetSmoke{
 private RemainingBucketRestSetSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=9)throw new IllegalArgumentException("usage: RemainingBucketRestSetSmoke server.jar workspace port seed username chunkX chunkZ fixtureTicks flowTicks");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]),fixtureTicks=Integer.parseInt(a[7]),flowTicks=Integer.parseInt(a[8]);
  require(seed==17320110707L&&user.equals("BucketRest443")&&user.length()<=16,"remaining-bucket-rest-set identity drift");
  Duration timeout=Duration.ofSeconds(180);B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout),reader=null;
  BlockPosition top,lavaTop,waterSource,waterTarget,lavaSource,lavaTarget;BlockState waterFlow,lavaFlow;int column;PlayerPose pose;
  BlockState air=new BlockState(0,0),dirt=new BlockState(3,0),stillWater=new BlockState(9,0),stillLava=new BlockState(11,0);
  RemoteItemStack empty=new RemoteItemStack(325,1,0),waterBucket=new RemoteItemStack(326,1,0),lavaBucket=new RemoteItemStack(327,1,0);
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,new int[]{0,1,2,3,4,5,6},new int[]{1,1,3,9,11,325,325},new int[]{64,64,2,1,1,1,1},new int[]{0,0,0,0,0,0,0});actor.connect();pose=actor.synchronizePose();require(actor.awaitInventory().occupiedSlots()==7,"remaining-bucket-rest-set inventory drift");
   RemoteChunkSnapshot initial=actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);top=foundation(initial,cx,cz);column=0;actor.selectHeldSlot(0);
   while(water(initial.blockAt(local(top.x(),cx),top.y()+1,local(top.z(),cz)).legacyId())){top=place(actor,top,BlockFace.UP,1);pose=actor.moveAndObserve(0D,1D,0D,1).resulting();require(++column<=15,"water column exceeded remaining-bucket-rest-set fixture");}for(int lift=0;lift<8;lift++){top=place(actor,top,BlockFace.UP,1);pose=actor.moveAndObserve(0D,1D,0D,1).resulting();column++;}
   BlockPosition[] water=trench(actor,top);waterSource=water[0];waterTarget=water[1];actor.selectHeldSlot(1);lavaTop=top;for(int step=0;step<4;step++)lavaTop=place(actor,lavaTop,BlockFace.SOUTH,1);BlockPosition[] lava=trench(actor,lavaTop);lavaSource=lava[0];lavaTarget=lava[1];
   BlockPosition rail=place(actor,new BlockPosition(top.x()+2,top.y(),top.z()),BlockFace.EAST,1);for(int step=0,n=lavaTop.z()-top.z()+1;step<n;step++)rail=place(actor,rail,BlockFace.SOUTH,1);
   actor.selectHeldSlot(2);place(actor,BlockFace.EAST.adjacent(top),BlockFace.UP,3);place(actor,BlockFace.EAST.adjacent(lavaTop),BlockFace.UP,3);
   actor.selectHeldSlot(3);place(actor,top,BlockFace.UP,9);actor.selectHeldSlot(4);place(actor,lavaTop,BlockFace.UP,11);
   RemoteWorldView filled=worldline.test.WorldlineSmokeAwait.observe(actor,5);require(filled.blockAt(waterSource.x(),waterSource.y(),waterSource.z()).equals(stillWater)&&filled.blockAt(waterTarget.x(),waterTarget.y(),waterTarget.z()).equals(dirt)&&filled.blockAt(lavaSource.x(),lavaSource.y(),lavaSource.z()).equals(stillLava)&&filled.blockAt(lavaTarget.x(),lavaTarget.y(),lavaTarget.z()).equals(dirt),"bounded remaining-bucket-rest-set baseline drift");
   actor.selectHeldSlot(7);pose=actor.moveAndObserve(0D,-2D,0D,2).resulting();worldline.test.WorldlineSmokeAwait.observe(actor,fixtureTicks);
   require(open(actor,waterTarget).equals(air)&&open(actor,lavaTarget).equals(air),"horizontal air absent");
   RemoteWorldView live=worldline.test.WorldlineSmokeAwait.observe(actor,flowTicks);waterFlow=live.blockAt(waterTarget.x(),waterTarget.y(),waterTarget.z());lavaFlow=live.blockAt(lavaTarget.x(),lavaTarget.y(),lavaTarget.z());
   require(flowing(waterFlow,true)&&flowing(lavaFlow,false)&&live.blockAt(waterSource.x(),waterSource.y(),waterSource.z()).equals(stillWater)&&live.blockAt(lavaSource.x(),lavaSource.y(),lavaSource.z()).equals(stillLava),"source vs flowing fixture absent: "+waterFlow+" / "+lavaFlow);
   actor.selectHeldSlot(5);pose=walk(actor,pose,waterTarget.x()+0.5D,top.y()+1.2D,waterTarget.z()+0.5D);scoop(actor,waterTarget,0F,90F);
   RemoteWorldView waterReject=worldline.test.WorldlineSmokeAwait.observe(actor,5);require(waterReject.blockAt(waterTarget.x(),waterTarget.y(),waterTarget.z()).equals(waterFlow)&&waterReject.blockAt(waterSource.x(),waterSource.y(),waterSource.z()).equals(stillWater)&&held(actor,5).equals(empty),"flowing water 8/9 pickup must reject");
   pose=walk(actor,pose,waterSource.x()+0.5D,top.y()+1.2D,waterSource.z()+0.5D);scoop(actor,waterSource,0F,90F);actor.awaitBlock(waterSource,air);
   require(worldline.test.WorldlineSmokeAwait.observe(actor,5).blockAt(waterSource.x(),waterSource.y(),waterSource.z()).equals(air)&&held(actor,5).equals(waterBucket),"source water 9:0 pickup drift");
   actor.selectHeldSlot(6);pose=walk(actor,pose,top.x()-0.5D,top.y()+2.0D,top.z()+0.5D);pose=walk(actor,pose,top.x()-0.5D,top.y()+2.0D,top.z()-0.7D);pose=walk(actor,pose,top.x()+2.5D,top.y()+2.0D,top.z()-0.7D);pose=walk(actor,pose,top.x()+2.5D,top.y()+2.0D,top.z()+0.5D);pose=walk(actor,pose,top.x()+3.5D,top.y()+1.2D,top.z()+0.5D);pose=walk(actor,pose,top.x()+3.5D,top.y()+1.2D,lavaTarget.z()+1.5D);pose=walk(actor,pose,top.x()+2.5D,top.y()+2.0D,lavaTarget.z()+1.5D);pose=walk(actor,pose,lavaTarget.x()+0.5D,top.y()+2.0D,lavaTarget.z()+1.5D);scoop(actor,lavaTarget,180F,70F);
   RemoteWorldView lavaReject=worldline.test.WorldlineSmokeAwait.observe(actor,5);require(lavaReject.blockAt(lavaTarget.x(),lavaTarget.y(),lavaTarget.z()).equals(lavaFlow)&&lavaReject.blockAt(lavaSource.x(),lavaSource.y(),lavaSource.z()).equals(stillLava)&&held(actor,6).equals(empty)&&actor.health()==20,"flowing lava 10/11 pickup must reject");
   pose=walk(actor,pose,lavaSource.x()+0.5D,top.y()+2.0D,lavaSource.z()+1.5D);scoop(actor,lavaSource,180F,70F);actor.awaitBlock(lavaSource,air);
   require(worldline.test.WorldlineSmokeAwait.observe(actor,5).blockAt(lavaSource.x(),lavaSource.y(),lavaSource.z()).equals(air)&&held(actor,6).equals(lavaBucket)&&actor.health()==20,"source lava 11:0 pickup drift");
   actor.close();awaitPlayers(server,0);server.save();reader=new B173WireClient("127.0.0.1",port,user,timeout);reader.connect();reader.synchronizePose();RemoteChunkSnapshot after=reader.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);RemoteInventoryView persisted=reader.awaitInventory();
   require(after.blockAt(local(waterSource.x(),cx),waterSource.y(),local(waterSource.z(),cz)).equals(air)&&after.blockAt(local(lavaSource.x(),cx),lavaSource.y(),local(lavaSource.z(),cz)).equals(air)&&persisted.slot(41).item().equals(waterBucket)&&persisted.slot(42).item().equals(lavaBucket)&&reader.awaitHealth(20)==20,"fresh remaining-bucket-rest-set drift");
   String evidence="column="+column+",water-source="+cell(waterSource)+":9:0->0:0,water-flow="+cell(waterTarget)+":"+id(waterFlow)+"->"+id(waterFlow)+",held-water=325:1:0->325:1:0->326:1:0,lava-source="+cell(lavaSource)+":11:0->0:0,lava-flow="+cell(lavaTarget)+":"+id(lavaFlow)+"->"+id(lavaFlow)+",held-lava=325:1:0->325:1:0->327:1:0,water=8/9,lava=10/11,persisted=true,clients=2,disconnect=clean";
   String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=raised-stone-trenches+seeded-still-water9+seeded-still-lava11+dirt-gates3+empty-bucket325x2|settle="+fixtureTicks+"+"+flowTicks+"ticks|cause=packet15-dir255-bucket325-flowing-then-source|wire=packet53-flow-keep+packet103-bucket325+packet53-air0+packet103-bucket326+packet53-flow-keep+packet103-bucket325+packet53-air0+packet103-bucket327|oracle=source-vs-flowing-pickup-8/9+10/11-not-m344-place-pickup|"+evidence;
   System.out.println("WORLDLINE_M443_SET="+evidence);System.out.println("WORLDLINE_M443_TRACE="+trace);System.out.println("WORLDLINE_M443_SIGNATURE="+sha(trace));
  }finally{actor.close();if(reader!=null)reader.close();server.close();}
 }
 private static BlockPosition[] trench(B173WireClient a,BlockPosition top)throws Exception{
  BlockPosition west=place(a,top,BlockFace.WEST,1),east=place(a,top,BlockFace.EAST,1),east2=place(a,east,BlockFace.EAST,1);
  List<BlockPosition>floor=new ArrayList<>(Arrays.asList(west,top,east,east2));
  for(BlockPosition p:new ArrayList<>(floor)){floor.add(place(a,p,BlockFace.NORTH,1));floor.add(place(a,p,BlockFace.SOUTH,1));}
  for(BlockPosition p:floor){int dx=p.x()-top.x(),dz=p.z()-top.z();if(dx==-1||dx==2||dz==-1||dz==1)place(a,p,BlockFace.UP,1);}
  return new BlockPosition[]{BlockFace.UP.adjacent(top),BlockFace.UP.adjacent(east)};
 }
 private static PlayerPose walk(B173WireClient a,PlayerPose p,double x,double y,double z)throws Exception{int guard=0;while(Math.abs(p.x()-x)>0.3D||Math.abs(p.y()-y)>0.3D||Math.abs(p.z()-z)>0.3D){require(++guard<=48,"walk stuck at "+p.x()+","+p.y()+","+p.z());p=a.moveAndObserve(clamp(x-p.x()),clamp(y-p.y()),clamp(z-p.z()),1).resulting();}return p;}
 private static double clamp(double v){return v>1D?1D:v<-1D?-1D:v;}
 private static void scoop(B173WireClient a,BlockPosition cell,float yaw,float pitch)throws Exception{a.look(yaw,pitch);a.useHeldItemOnBlock(cell,BlockFace.UP);a.useSelectedItemInAir();}
 private static BlockState open(B173WireClient a,BlockPosition target)throws Exception{a.beginBreak(target);Thread.sleep(3000L);a.finishBreak(target);return a.awaitBlock(target,new BlockState(0,0)).blockAt(target.x(),target.y(),target.z());}
 private static BlockPosition place(B173WireClient a,BlockPosition support,BlockFace face,int id)throws Exception{BlockPosition target=face.adjacent(support);a.placeHeldBlock(support,face);a.awaitBlock(target,new BlockState(id,0));return target;}
 private static BlockPosition foundation(RemoteChunkSnapshot q,int cx,int cz){for(int x=4;x<=11;x++)for(int z=4;z<=11;z++)for(int y=126;y>=1;y--)if(q.blockAt(x,y,z).legacyId()==3&&water(q.blockAt(x,y+1,z).legacyId()))return new BlockPosition(cx*16+x,y,cz*16+z);throw new IllegalStateException("no deterministic remaining-bucket-rest-set foundation");}
 private static RemoteItemStack held(B173WireClient a,int hotbar){RemoteInventorySlot slot=a.inventory().slot(36+hotbar);require(!slot.empty(),"hotbar "+hotbar+" empty");return slot.item();}
 private static boolean flowing(BlockState s,boolean water){return (water?water(s.legacyId()):lava(s.legacyId()))&&s.metadata()>0;}
 private static boolean water(int id){return id==8||id==9;}private static boolean lava(int id){return id==10||id==11;}private static int local(int v,int c){return v-c*16;}
 private static String cell(BlockPosition p){return p.x()+":"+p.y()+":"+p.z();}private static String id(BlockState s){return s.legacyId()+":"+s.metadata();}
 private static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}
 private static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}
 private static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
