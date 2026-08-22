package worldline.smoke.pistonqcsetb173;

import java.nio.file.*;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Official piston 33 QC-extends from a powered above-block, then retracts. */
public final class PistonQcSetSmoke{
 private PistonQcSetSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=9)throw new IllegalArgumentException("usage: PistonQcSetSmoke server.jar workspace port seed username chunkX chunkZ fixtureTicks signalTicks");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]),fixture=Integer.parseInt(a[7]),signal=Integer.parseInt(a[8]);Duration timeout=Duration.ofSeconds(90);
  PistonQcSetArm.require(user.length()<=16,"username exceeds 16");
  B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout),reader=null;
  PistonQcSetArm arm;int[] column=new int[1];
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,new int[]{0,1,2},new int[]{1,33,69},new int[]{32,1,1},new int[]{0,0,0});actor.connect();actor.synchronizePose();actor.look(-90F,0F);PistonQcSetArm.require(actor.awaitInventory().occupiedSlots()==3,"piston-qc inventory drift");
   RemoteChunkSnapshot initial=actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);BlockPosition top=PistonQcSetArm.raise(actor,initial,cx,cz,column);
   arm=PistonQcSetArm.place(actor,initial,top,cx,cz);actor.selectHeldSlot(3);
   RemoteWorldView settled=actor.sustainTicks(fixture);
   PistonQcSetArm.require(settled.blockAt(arm.piston.x(),arm.piston.y(),arm.piston.z()).equals(new BlockState(33,4))&&settled.blockAt(arm.head.x(),arm.head.y(),arm.head.z()).equals(new BlockState(0,0))&&settled.blockAt(arm.above.x(),arm.above.y(),arm.above.z()).equals(new BlockState(1,0))&&settled.blockAt(arm.lever.x(),arm.lever.y(),arm.lever.z()).equals(new BlockState(69,1))&&!PistonQcSetArm.directPower(settled,arm.piston),"piston 33 QC precondition drift");
   arm.pulse(actor,signal,new BlockState(33,12),new BlockState(34,4),9,"piston 33 QC extend");
   arm.pulse(actor,signal,new BlockState(33,4),new BlockState(0,0),1,"piston 33 QC retract");
   actor.close();PistonQcSetArm.awaitPlayers(server,0);server.save();
   reader=new B173WireClient("127.0.0.1",port,user,timeout);reader.connect();reader.synchronizePose();RemoteChunkSnapshot after=reader.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);
   arm.persist(after,cx,cz,new BlockState(33,4),new BlockState(0,0),"fresh piston 33 QC retract drift");
   String evidence="column="+column[0]+",qc-extend=33:4->12,qc-retract=33:12->4,piston="+PistonQcSetArm.cell(arm.piston)+":33:4->12->4,head="+PistonQcSetArm.cell(arm.head)+":0:0->34:4->0:0,above="+PistonQcSetArm.cell(arm.above)+":1:0,lever="+PistonQcSetArm.cell(arm.lever)+":69:1->9->1,direct-power=false,persisted=true,clients=2,disconnect=clean";
   String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=piston33-west+above-stone+lever69-above-east|settle="+fixture+"+"+signal+"ticks|cause=packet15-lever-on-above-block+deactivate|effect=official-piston33-qc-extend+head34+qc-retract|observation=fresh-login-packet51|"+evidence;
   System.out.println("WORLDLINE_M546_SET="+evidence);System.out.println("WORLDLINE_M546_TRACE="+trace);System.out.println("WORLDLINE_M546_SIGNATURE="+PistonQcSetArm.sha(trace));
  }finally{actor.close();if(reader!=null)reader.close();server.close();}
 }
}
