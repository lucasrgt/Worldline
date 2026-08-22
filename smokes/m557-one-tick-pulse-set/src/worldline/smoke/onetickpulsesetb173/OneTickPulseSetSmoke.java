package worldline.smoke.onetickpulsesetb173;

import java.nio.file.*;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** One 1-tick lever pulse through a repeater/torch limiter drops piston 33's payload. */
public final class OneTickPulseSetSmoke{
 private OneTickPulseSetSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=9)throw new IllegalArgumentException("usage: OneTickPulseSetSmoke server.jar workspace port seed username chunkX chunkZ fixtureTicks signalTicks");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]),fixture=Integer.parseInt(a[7]),signal=Integer.parseInt(a[8]);Duration timeout=Duration.ofSeconds(90);
  OneTickPulseSetArm.require(user.length()<=16&&user.equals("OneTick557"),"actor username drift");
  B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout),reader=null;int[] column=new int[1];
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,new int[]{0,1,2,3},new int[]{1,33,69,356},new int[]{64,1,1,1},new int[]{0,0,0,0});actor.connect();actor.synchronizePose();actor.look(-90F,0F);OneTickPulseSetArm.require(actor.awaitInventory().occupiedSlots()==4,"one-tick-pulse inventory drift");
   RemoteChunkSnapshot initial=actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);BlockPosition top=OneTickPulseSetArm.raise(actor,initial,cx,cz,column);actor.moveAndObserve(2D,0D,2D,2);
   OneTickPulseSetArm arm=OneTickPulseSetArm.place(actor,initial,top,cx,cz);actor.selectHeldSlot(6);
   RemoteWorldView settled=actor.sustainTicks(fixture);arm.idle(settled,"one-tick pulse limiter idle drift");
   actor.activateBlock(arm.lever,BlockFace.UP);actor.sustainTicks(4);actor.activateBlock(arm.lever,BlockFace.UP);RemoteWorldView live=actor.sustainTicks(signal);arm.dropped(live,"one-tick pulse drop absent");
   actor.close();OneTickPulseSetArm.awaitPlayers(server,0);server.save();
   reader=new B173WireClient("127.0.0.1",port,user,timeout);reader.connect();reader.synchronizePose();RemoteChunkSnapshot after=reader.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);String cells=arm.persist(after,cx,cz);
   String evidence="column="+column[0]+",pulse=one-tick,drop=pushed-block,"+cells+",lever="+OneTickPulseSetArm.cell(arm.lever)+":69:off->on->off,repeater="+OneTickPulseSetArm.cell(arm.repeater)+":93:3,delay=1,facing=3,look=90:0,persisted=true,clients=2,disconnect=clean";
   String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=repeater-pulse-limiter+piston33-west|settle="+fixture+"+4+"+signal+"ticks|cause=packet15-lever-one-tick-pulse|effect=official-one-tick-pulse+piston33-drop|observation=fresh-login-packet51|"+evidence;
   System.out.println("WORLDLINE_M557_SET="+evidence);System.out.println("WORLDLINE_M557_TRACE="+trace);System.out.println("WORLDLINE_M557_SIGNATURE="+OneTickPulseSetArm.sha(trace));
  }finally{actor.close();if(reader!=null)reader.close();server.close();}
 }
}
