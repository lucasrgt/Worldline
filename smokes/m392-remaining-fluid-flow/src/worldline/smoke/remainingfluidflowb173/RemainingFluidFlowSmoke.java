package worldline.smoke.remainingfluidflowb173;

import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.security.MessageDigest;import java.time.Duration;import java.util.*;
import worldline.api.*;import worldline.b173server.*;

/** Opens adjacent air so still water 9 and still lava 11 each flow horizontally in one official set. */
public final class RemainingFluidFlowSmoke{
 private RemainingFluidFlowSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=9)throw new IllegalArgumentException("usage: RemainingFluidFlowSmoke server.jar workspace port seed username chunkX chunkZ fixtureTicks flowTicks");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]),fixtureTicks=Integer.parseInt(a[7]),flowTicks=Integer.parseInt(a[8]);
  require(seed==17320110707L&&user.equals("FluidFlow392")&&user.length()<=16,"remaining-fluid-flow identity drift");
  Duration timeout=Duration.ofSeconds(120);B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout),reader=null;
  BlockPosition top,lavaTop,waterSource,waterTarget,lavaSource,lavaTarget;BlockState waterSettled,lavaSettled;int column;
  BlockState air=new BlockState(0,0),dirt=new BlockState(3,0),stillWater=new BlockState(9,0),stillLava=new BlockState(11,0);
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,new int[]{0,1,2,3},new int[]{1,3,9,11},new int[]{64,2,1,1},new int[]{0,0,0,0});actor.connect();actor.synchronizePose();require(actor.awaitInventory().occupiedSlots()==4,"remaining-fluid-flow inventory drift");
   RemoteChunkSnapshot initial=actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);top=foundation(initial,cx,cz);column=0;actor.selectHeldSlot(0);
   while(water(initial.blockAt(local(top.x(),cx),top.y()+1,local(top.z(),cz)).legacyId())){top=place(actor,top,BlockFace.UP,1);actor.moveAndObserve(0D,1D,0D,1);require(++column<=15,"water column exceeded remaining-fluid-flow fixture");}for(int lift=0;lift<8;lift++){top=place(actor,top,BlockFace.UP,1);actor.moveAndObserve(0D,1D,0D,1);column++;}
   BlockPosition[] water=trench(actor,top);waterSource=water[0];waterTarget=water[1];lavaTop=top;for(int step=0;step<4;step++)lavaTop=place(actor,lavaTop,BlockFace.SOUTH,1);BlockPosition[] lava=trench(actor,lavaTop);lavaSource=lava[0];lavaTarget=lava[1];
   actor.selectHeldSlot(1);place(actor,BlockFace.EAST.adjacent(top),BlockFace.UP,3);place(actor,BlockFace.EAST.adjacent(lavaTop),BlockFace.UP,3);
   actor.selectHeldSlot(2);place(actor,top,BlockFace.UP,9);actor.selectHeldSlot(3);place(actor,lavaTop,BlockFace.UP,11);
   RemoteWorldView filled=actor.sustainTicks(5);require(filled.blockAt(waterSource.x(),waterSource.y(),waterSource.z()).equals(stillWater)&&filled.blockAt(waterTarget.x(),waterTarget.y(),waterTarget.z()).equals(dirt)&&filled.blockAt(lavaSource.x(),lavaSource.y(),lavaSource.z()).equals(stillLava)&&filled.blockAt(lavaTarget.x(),lavaTarget.y(),lavaTarget.z()).equals(dirt),"bounded remaining-fluid-flow baseline drift");
   actor.selectHeldSlot(4);actor.moveAndObserve(0D,-2D,0D,2);actor.sustainTicks(fixtureTicks);
   require(open(actor,waterTarget).equals(air)&&open(actor,lavaTarget).equals(air),"horizontal air absent");
   RemoteWorldView live=actor.sustainTicks(flowTicks);waterSettled=live.blockAt(waterTarget.x(),waterTarget.y(),waterTarget.z());lavaSettled=live.blockAt(lavaTarget.x(),lavaTarget.y(),lavaTarget.z());
   require(water(waterSettled.legacyId())&&lava(lavaSettled.legacyId())&&live.blockAt(waterSource.x(),waterSource.y(),waterSource.z()).equals(stillWater)&&live.blockAt(lavaSource.x(),lavaSource.y(),lavaSource.z()).equals(stillLava),"remaining fluid flow absent: "+waterSettled+" / "+lavaSettled);
   actor.close();awaitPlayers(server,0);server.save();reader=new B173WireClient("127.0.0.1",port,user,timeout);reader.connect();reader.synchronizePose();RemoteChunkSnapshot after=reader.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);
   require(after.blockAt(local(waterSource.x(),cx),waterSource.y(),local(waterSource.z(),cz)).equals(stillWater)&&after.blockAt(local(waterTarget.x(),cx),waterTarget.y(),local(waterTarget.z(),cz)).equals(waterSettled)&&after.blockAt(local(lavaSource.x(),cx),lavaSource.y(),local(lavaSource.z(),cz)).equals(stillLava)&&after.blockAt(local(lavaTarget.x(),cx),lavaTarget.y(),local(lavaTarget.z(),cz)).equals(lavaSettled),"fresh remaining-fluid-flow drift");
   String evidence="column="+column+",water-source="+waterSource.x()+":"+waterSource.y()+":"+waterSource.z()+":9:0,water-target="+waterTarget.x()+":"+waterTarget.y()+":"+waterTarget.z()+":3:0->0:0->"+waterSettled.legacyId()+":"+waterSettled.metadata()+",lava-source="+lavaSource.x()+":"+lavaSource.y()+":"+lavaSource.z()+":11:0,lava-target="+lavaTarget.x()+":"+lavaTarget.y()+":"+lavaTarget.z()+":3:0->0:0->"+lavaSettled.legacyId()+":"+lavaSettled.metadata()+",persisted=true,clients=2,disconnect=clean";
   String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=raised-stone-trenches+seeded-still-water9+seeded-still-lava11+dirt-gates3|settle="+fixtureTicks+"+"+flowTicks+"ticks|cause=packet14-open-horizontal-air-cells|confirmation=packet53-air|effect=official-horizontal-water-and-lava|observation=live-packet53+fresh-login-packet51|oracle=still-water9+still-lava11-horizontal-flow-set|"+evidence;
   System.out.println("WORLDLINE_M392_SET="+evidence);System.out.println("WORLDLINE_M392_TRACE="+trace);System.out.println("WORLDLINE_M392_SIGNATURE="+sha(trace));
  }finally{actor.close();if(reader!=null)reader.close();server.close();}
 }
 private static BlockPosition[] trench(B173WireClient a,BlockPosition top)throws Exception{
  BlockPosition west=place(a,top,BlockFace.WEST,1),east=place(a,top,BlockFace.EAST,1),east2=place(a,east,BlockFace.EAST,1);
  List<BlockPosition>floor=new ArrayList<>(Arrays.asList(west,top,east,east2));
  for(BlockPosition p:new ArrayList<>(floor)){floor.add(place(a,p,BlockFace.NORTH,1));floor.add(place(a,p,BlockFace.SOUTH,1));}
  for(BlockPosition p:floor){int dx=p.x()-top.x(),dz=p.z()-top.z();if(dx==-1||dx==2||dz==-1||dz==1)place(a,p,BlockFace.UP,1);}
  return new BlockPosition[]{BlockFace.UP.adjacent(top),BlockFace.UP.adjacent(east)};
 }
 private static BlockState open(B173WireClient a,BlockPosition target)throws Exception{a.beginBreak(target);Thread.sleep(3000L);a.finishBreak(target);return a.awaitBlock(target,new BlockState(0,0)).blockAt(target.x(),target.y(),target.z());}
 private static BlockPosition place(B173WireClient a,BlockPosition support,BlockFace face,int id)throws Exception{BlockPosition target=face.adjacent(support);a.placeHeldBlock(support,face);a.awaitBlock(target,new BlockState(id,0));return target;}
 private static BlockPosition foundation(RemoteChunkSnapshot q,int cx,int cz){for(int x=4;x<=11;x++)for(int z=4;z<=11;z++)for(int y=126;y>=1;y--)if(q.blockAt(x,y,z).legacyId()==3&&water(q.blockAt(x,y+1,z).legacyId()))return new BlockPosition(cx*16+x,y,cz*16+z);throw new IllegalStateException("no deterministic remaining-fluid-flow foundation");}
 private static boolean water(int id){return id==8||id==9;}private static boolean lava(int id){return id==10||id==11;}private static int local(int v,int c){return v-c*16;}
 private static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}
 private static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}
 private static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
