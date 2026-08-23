package worldline.smoke.farmlandtramplesetb173;

import java.nio.file.*;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Hoes official dirt into dry farmland 60:0, then jump-fall trampling converts it to dirt 3. */
public final class FarmlandTrampleSetSmoke{
 private FarmlandTrampleSetSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=7)throw new IllegalArgumentException("usage: FarmlandTrampleSetSmoke server.jar workspace port seed username chunkX chunkZ");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]);
  FarmlandTrampleSetArm.require(seed==17320110707L&&user.equals("FarmTramp576")&&user.length()<=16,"farmland-trample-set identity drift");
  Duration timeout=Duration.ofSeconds(90);B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout),reader=null;
  BlockPosition top,dirt;int[] column=new int[1];PlayerPose pose;BlockState farm=new BlockState(60,0),trampled=new BlockState(3,0);
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,new int[]{0,1,2},new int[]{1,3,290},new int[]{64,1,1},new int[]{0,0,0});
   actor.connect();actor.synchronizePose();FarmlandTrampleSetArm.require(actor.awaitInventory().occupiedSlots()==3,"farmland-trample inventory drift");
   RemoteChunkSnapshot initial=actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);top=FarmlandTrampleSetArm.raise(actor,initial,cx,cz,column);
   actor.selectHeldSlot(1);dirt=FarmlandTrampleSetArm.place(actor,top,BlockFace.UP,3);actor.selectHeldSlot(2);FarmlandTrampleSetArm.till(actor,dirt);
   pose=actor.moveAndObserve(0D,0D,0D,1).resulting();pose=actor.moveAndObserve(dirt.x()+0.5D-pose.x(),dirt.y()+1.0D-pose.y(),dirt.z()+0.5D-pose.z(),2).resulting();
   FarmlandTrampleSetArm.require(worldline.test.WorldlineSmokeAwait.observe(actor,1).blockAt(dirt.x(),dirt.y(),dirt.z()).equals(farm),"live farmland 60:0 drift");
   FarmlandTrampleSetArm.trample(actor,dirt);
   FarmlandTrampleSetArm.require(worldline.test.WorldlineSmokeAwait.observe(actor,2).blockAt(dirt.x(),dirt.y(),dirt.z()).equals(trampled),"live trample 60->3 drift");
   actor.close();FarmlandTrampleSetArm.awaitPlayers(server,0);server.save();
   reader=new B173WireClient("127.0.0.1",port,user,timeout);reader.connect();reader.synchronizePose();RemoteChunkSnapshot after=reader.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);
   FarmlandTrampleSetArm.persist(after,cx,cz,top,dirt,trampled);
   String evidence="column="+column[0]+",support="+FarmlandTrampleSetArm.token(top,1,0)+",cell="+FarmlandTrampleSetArm.token(dirt,3,0)+",hoe=290,farmland=60:0,trample=60->3,persisted=true,clients=2,disconnect=clean";
   String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=raised-farmland60|cause=packet13-jump-fall|wire=packet53-dirt3|oracle=live-trample-60->3+fresh-login-dirt3:0|"+evidence;
   System.out.println("WORLDLINE_M576_TRAMPLE="+evidence);System.out.println("WORLDLINE_M576_TRACE="+trace);System.out.println("WORLDLINE_M576_SIGNATURE="+FarmlandTrampleSetArm.sha(trace));
  }finally{actor.close();if(reader!=null)reader.close();server.close();}
 }
}
