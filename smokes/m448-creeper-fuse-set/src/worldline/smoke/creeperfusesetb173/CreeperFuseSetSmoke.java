package worldline.smoke.creeperfusesetb173;

import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.security.MessageDigest;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Spawns Packet24 type 50, stays in proximity fuse, and requires Packet60 after Packet40 ignited state. */
public final class CreeperFuseSetSmoke{
 private CreeperFuseSetSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=7)throw new IllegalArgumentException("usage: CreeperFuseSetSmoke server.jar workspace port seed username chunkX chunkZ");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]);
  require(seed==17320110707L&&user.equals("CreeperFuse448")&&user.length()<=16,"creeper-fuse-set identity drift");
  Duration timeout=Duration.ofSeconds(180);B173DedicatedServer server=B173DedicatedServer.monsters(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout);
  BlockPosition top,dirt,wool,spawner;int column;RemoteExplosion explosion;PlayerPose pose;
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,new int[]{0,1,2,3},new int[]{1,3,35,52},new int[]{32,48,48,1},new int[]{0,0,0,0});actor.connect();pose=actor.synchronizePose();require(actor.awaitInventory().occupiedSlots()==4,"creeper-fuse-set inventory drift");
   RemoteChunkSnapshot initial=actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);top=foundation(initial,cx,cz);column=0;actor.selectHeldSlot(0);
   while(water(initial.blockAt(local(top.x(),cx),top.y()+1,local(top.z(),cz)).legacyId())){top=place(actor,top,BlockFace.UP,1);pose=actor.moveAndObserve(0D,1D,0D,1).resulting();require(++column<=15,"water column exceeded creeper-fuse-set fixture");}for(int lift=0;lift<8;lift++){top=place(actor,top,BlockFace.UP,1);pose=actor.moveAndObserve(0D,1D,0D,1).resulting();column++;}
   pad(actor,top);dirt=new BlockPosition(top.x()-1,top.y(),top.z());wool=new BlockPosition(top.x()+1,top.y(),top.z());
   require(actor.sustainTicks(2).blockAt(dirt.x(),dirt.y(),dirt.z()).equals(new BlockState(3,0))&&actor.sustainTicks(1).blockAt(wool.x(),wool.y(),wool.z()).equals(new BlockState(35,0)),"wool/dirt pad drift");
   actor.selectHeldSlot(3);spawner=place(actor,new BlockPosition(top.x(),top.y(),top.z()-1),BlockFace.UP,52);pose=stand(actor,pose,top);actor.close();awaitPlayers(server,0);server.save();
  }finally{actor.close();server.close();}
  Thread.sleep(1000L);B173SpawnerSeed.entity(workspace,spawner,"Creeper");server=B173DedicatedServer.monsters(jar,workspace,port,seed,timeout,3,true);actor=new B173WireClient("127.0.0.1",port,user,timeout);
  try{server.boot();actor.connect();pose=stand(actor,actor.synchronizePose(),top);require(actor.awaitInventory().occupiedSlots()>=1&&Math.abs(pose.y()-(top.y()+1.0D))<2D,"creeper-fuse-set reload pose drift");server.setTime(14000L);
   RemoteMobSpawn creeper=near(actor,spawner);require(creeper.legacyType()==50&&creeper.entityId()!=actor.state().entityId()&&creeper.legacyType()!=90,"creeper Packet24 type50 identity drift");
   explosion=Creeper.stayUntilExplode(actor,creeper);require(explosion!=null&&explosion.strength()>0F,"Packet60 did not follow creeper proximity fuse");
   actor.close();awaitPlayers(server,0);server.save();
   String evidence="column="+column+",support="+top.x()+":"+top.y()+":"+top.z()+":1:0,pad="+dirt.x()+":"+dirt.y()+":"+dirt.z()+"+"+wool.x()+":"+wool.y()+":"+wool.z()+",spawner="+spawner.x()+":"+spawner.y()+":"+spawner.z()+":52:0,mob=type50,fuse=proximity-stay+packet40-state1,order=fuse-then-packet60,packet60=followed,stay=true,night=14000,clients=2,disconnect=clean";
   String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=raised-7x7-dirt+wool-pad+creeper-spawner52|cause=nbt-entityid-creeper+time-14000+proximity-stay|wire=packet24-type50+packet40-state1+packet60|oracle=creeper-fuse-then-packet60-not-crater-not-drop289-not-leave|"+evidence;
   WorldlineEvidence run=Creeper.evidence(evidence,sha(trace));require(run.behavior()==WorldlineBehavior.CREEPER_FUSE&&!run.compare(WorldlineEvidence.pin(WorldlineBehavior.CREEPER_FUSE,evidence,run.signature())).diverged(),"creeper-fuse evidence drift");
   System.out.println("WORLDLINE_M448_SET="+evidence);System.out.println("WORLDLINE_M448_TRACE="+trace);System.out.println("WORLDLINE_M448_SIGNATURE="+run.signature());System.out.println("WORLDLINE_BEHAVIOR="+run.token());
  }finally{actor.close();server.close();}
 }
 private static void pad(B173WireClient a,BlockPosition t)throws Exception{for(int r=1;r<=3;r++){for(int z=-r+1;z<r;z++){cell(a,new BlockPosition(t.x()-r+1,t.y(),t.z()+z),BlockFace.WEST,1,3);cell(a,new BlockPosition(t.x()+r-1,t.y(),t.z()+z),BlockFace.EAST,2,35);}for(int x=-r+1;x<r;x++){cell(a,new BlockPosition(t.x()+x,t.y(),t.z()-r+1),BlockFace.NORTH,x<=0?1:2,x<=0?3:35);cell(a,new BlockPosition(t.x()+x,t.y(),t.z()+r-1),BlockFace.SOUTH,x<=0?1:2,x<=0?3:35);}cell(a,new BlockPosition(t.x()-r,t.y(),t.z()-r+1),BlockFace.NORTH,1,3);cell(a,new BlockPosition(t.x()-r,t.y(),t.z()+r-1),BlockFace.SOUTH,1,3);cell(a,new BlockPosition(t.x()+r,t.y(),t.z()-r+1),BlockFace.NORTH,2,35);cell(a,new BlockPosition(t.x()+r,t.y(),t.z()+r-1),BlockFace.SOUTH,2,35);}}
 private static void cell(B173WireClient a,BlockPosition s,BlockFace f,int slot,int id)throws Exception{a.selectHeldSlot(slot);place(a,s,f,id);}
 private static BlockPosition place(B173WireClient a,BlockPosition support,BlockFace face,int id)throws Exception{BlockPosition target=face.adjacent(support);a.placeHeldBlock(support,face);a.awaitBlock(target,new BlockState(id,0));return target;}
 private static RemoteMobSpawn near(B173WireClient a,BlockPosition p){for(int n=0;n<32;n++){RemoteMobSpawn s=a.awaitMobSpawn(50);double dx=s.x()-(p.x()+0.5D),dz=s.z()-(p.z()+0.5D);if(dx*dx+dz*dz<=100D&&Math.abs(s.y()-p.y())<=6D)return s;}throw new IllegalStateException("nearby creeper type 50 absent");}
 private static PlayerPose stand(B173WireClient a,PlayerPose pose,BlockPosition top)throws Exception{while(pose.y()>top.y()+1.01D)pose=a.moveAndObserve(0D,-1D,0D,1).resulting();require(Math.abs(pose.x()-(top.x()+0.5D))<3D&&Math.abs(pose.z()-(top.z()+0.5D))<3D,"actor missed creeper pad");return pose;}
 private static BlockPosition foundation(RemoteChunkSnapshot q,int cx,int cz){for(int x=4;x<=11;x++)for(int z=4;z<=11;z++)for(int y=126;y>=1;y--)if(q.blockAt(x,y,z).legacyId()==3&&water(q.blockAt(x,y+1,z).legacyId()))return new BlockPosition(cx*16+x,y,cz*16+z);throw new IllegalStateException("no deterministic creeper-fuse-set foundation");}
 private static boolean water(int id){return id==8||id==9;}private static int local(int v,int c){return v-c*16;}private static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}private static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}private static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
