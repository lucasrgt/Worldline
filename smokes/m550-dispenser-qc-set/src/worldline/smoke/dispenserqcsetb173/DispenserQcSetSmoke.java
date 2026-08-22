package worldline.smoke.dispenserqcsetb173;

import java.nio.file.*;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Places dispenser 23, loads cobble 4, and proves a top-lever on the block above ejects Packet21 without a side lever. */
public final class DispenserQcSetSmoke{
 private DispenserQcSetSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=7)throw new IllegalArgumentException("usage: DispenserQcSetSmoke server.jar workspace port seed username chunkX chunkZ");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]);Duration timeout=Duration.ofSeconds(90);
  DispenserQcSetArm.require(user.equals("DispenserQc550")&&user.length()<=16,"actor username drift");
  B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout);int[] column=new int[1];
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,new int[]{0,1,2,3},new int[]{1,23,69,4},new int[]{32,1,1,1},new int[]{0,0,0,0});actor.connect();actor.synchronizePose();actor.look(-90F,0F);DispenserQcSetArm.require(actor.awaitInventory().occupiedSlots()==4,"dispenser-qc inventory drift");
   RemoteChunkSnapshot initial=actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);BlockPosition top=DispenserQcSetArm.raise(actor,initial,cx,cz,column);
   DispenserQcSetArm arm=DispenserQcSetArm.assemble(actor,top);actor.selectHeldSlot(1);RemoteDroppedItem drop=arm.pulse(actor);DispenserQcSetArm.require(drop.item().equals(DispenserQcSetArm.COBBLE),"qc Packet21 identity drift");
   arm.remain(actor);actor.close();DispenserQcSetArm.awaitPlayers(server,0);server.save();
   DispenserQcSetArm.require((arm.leverOff&7)==5||(arm.leverOff&7)==6,"qc floor lever facing drift");
   String evidence="column="+column[0]+",disp="+DispenserQcSetArm.cell(arm.disp)+":23:4,qc="+DispenserQcSetArm.cell(arm.qc)+":1:0,lever="+DispenserQcSetArm.cell(arm.lever)+":floor:0->8,load=4x1,drop=packet21-4x1,remain=empty,power=qc-above,adjacent=none,clients=1,disconnect=clean";
   String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=raised-dispenser23-west+qc-stone-above+top-lever69|cause=packet15-item23+packet102-load-4+packet15-qc-lever-activate|wire=packet53-dispenser23+packet21-4|oracle=official-dispenser-qc-eject|"+evidence;
   System.out.println("WORLDLINE_M550_SET="+evidence);System.out.println("WORLDLINE_M550_TRACE="+trace);System.out.println("WORLDLINE_M550_SIGNATURE="+DispenserQcSetArm.sha(trace));
  }finally{actor.close();server.close();}
 }
}
