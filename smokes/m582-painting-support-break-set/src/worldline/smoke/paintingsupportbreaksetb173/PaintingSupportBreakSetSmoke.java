package worldline.smoke.paintingsupportbreaksetb173;

import java.nio.file.*;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Places one official painting, Packet14-breaks its support, and correlates Packet29 plus Packet21. */
public final class PaintingSupportBreakSetSmoke{
 private static final RemoteItemStack PAINT=new RemoteItemStack(321,1,0);
 private PaintingSupportBreakSetSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=8)throw new IllegalArgumentException("usage: PaintingSupportBreakSetSmoke server.jar workspace port seed actor observer chunkX chunkZ");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String actorName=a[4],observerName=a[5];int cx=Integer.parseInt(a[6]),cz=Integer.parseInt(a[7]);Duration timeout=Duration.ofSeconds(90);
  PaintingSupportBreakSetArm.require(seed==17320110707L&&actorName.equals("PaintBrk582")&&observerName.equals("PaintOb582")&&actorName.length()<=16&&observerName.length()<=16&&!actorName.equals(observerName),"painting-support-break identity drift");
  B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,actorName,timeout),observer=new B173WireClient("127.0.0.1",port,observerName,timeout);
  try{server.boot();B173PlayerSeed.writeInventory(workspace,actorName,4.5D,60D,4.5D,new int[]{0,1,2},new int[]{1,321,257},new int[]{32,1,1},new int[]{0,0,0});B173PlayerSeed.write(workspace,observerName,4.5D,80D,4.5D);actor.connect();actor.synchronizePose();PaintingSupportBreakSetArm.require(actor.awaitInventory().occupiedSlots()==3,"painting-support-break inventory drift");
   RemoteChunkSnapshot initial=actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);int[] column=new int[1];BlockPosition top=PaintingSupportBreakSetArm.raise(actor,initial,cx,cz,column);BlockPosition[] wall=PaintingSupportBreakSetArm.wall(actor,top);
   observer.connect();observer.synchronizePose();observer.awaitRemoteChunk(cx,cz);actor.selectHeldSlot(1);actor.look(-90F,0F);actor.useHeldItemOnBlock(wall[0],BlockFace.WEST);
   RemotePaintingSpawn spawn=B173PaintingAccess.await(actor),peer=B173PaintingAccess.await(observer);
   PaintingSupportBreakSetArm.require(spawn.equals(peer)&&spawn.entityId()!=actor.state().entityId()&&spawn.entityId()!=observer.state().entityId()&&spawn.packet()==25,"peer painting spawn drift");
   BlockPosition support=new BlockPosition(spawn.x(),spawn.y(),spawn.z());PaintingSupportBreakSetArm.harvest(actor,support);
   int gone=PaintingSupportBreakSetArm.gone(actor,spawn.entityId()),gonePeer=PaintingSupportBreakSetArm.gone(observer,spawn.entityId());
   RemoteDroppedItem drop=PaintingSupportBreakSetArm.drop(actor,PAINT),dropPeer=PaintingSupportBreakSetArm.drop(observer,PAINT);
   PaintingSupportBreakSetArm.require(gone==spawn.entityId()&&gonePeer==spawn.entityId()&&drop.item().equals(PAINT)&&dropPeer.item().equals(PAINT)&&drop.entityId()==dropPeer.entityId()&&drop.entityId()!=spawn.entityId(),"painting support-break Packet29/Packet21 drift");
   actor.close();observer.close();PaintingSupportBreakSetArm.awaitPlayers(server,0);server.save();
   String evidence="column="+column[0]+",wall="+wall[0].x()+":"+wall[0].y()+":"+wall[0].z()+"-"+wall[1].x()+":"+wall[1].y()+":"+wall[2].z()+":1:0,support="+support.x()+":"+support.y()+":"+support.z()+":1:0->0:0,painting="+spawn.x()+":"+spawn.y()+":"+spawn.z()+":dir"+spawn.direction()+",packet25+packet29+packet21-321,shared-id,clients=2,disconnect=clean";
   String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=raised-2x2-stone-wall|cause=packet15-item321-west+packet14-ironpick257-support|wire=packet25+packet29+packet21-321|oracle=support-break-destroys-painting-not-spawn-not-orient-not-motives|"+evidence;
   System.out.println("WORLDLINE_M582_SET="+evidence);System.out.println("WORLDLINE_M582_TRACE="+trace);System.out.println("WORLDLINE_M582_SIGNATURE="+PaintingSupportBreakSetArm.sha(trace));
  }finally{actor.close();observer.close();server.close();}
 }
}
