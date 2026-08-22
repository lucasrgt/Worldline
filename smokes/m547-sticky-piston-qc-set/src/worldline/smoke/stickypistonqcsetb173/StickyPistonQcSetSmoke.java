package worldline.smoke.stickypistonqcsetb173;

import java.nio.file.*;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Official sticky piston 29 extends by powering the block above, then pulls on retract. */
public final class StickyPistonQcSetSmoke{
 private StickyPistonQcSetSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=9)throw new IllegalArgumentException("usage: StickyPistonQcSetSmoke server.jar workspace port seed username chunkX chunkZ fixtureTicks signalTicks");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]),fixture=Integer.parseInt(a[7]),signal=Integer.parseInt(a[8]);Duration timeout=Duration.ofSeconds(90);
  StickyPistonQcSetArm.require(seed==17320110707L&&user.equals("StickyQc547")&&user.length()<=16,"sticky-piston-qc-set identity drift");
  B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout),reader=null;
  StickyPistonQcSetArm arm;int[] column=new int[1];
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,new int[]{0,1,2},new int[]{1,29,69},new int[]{32,1,1},new int[]{0,0,0});actor.connect();actor.synchronizePose();actor.look(-90F,0F);StickyPistonQcSetArm.require(actor.awaitInventory().occupiedSlots()==3,"sticky-qc inventory drift");
   RemoteChunkSnapshot initial=actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);BlockPosition top=StickyPistonQcSetArm.raise(actor,initial,cx,cz,column);
   arm=StickyPistonQcSetArm.place(actor,initial,top,cx,cz);actor.selectHeldSlot(3);
   RemoteWorldView settled=actor.sustainTicks(fixture);
   StickyPistonQcSetArm.require(settled.blockAt(arm.piston.x(),arm.piston.y(),arm.piston.z()).equals(new BlockState(29,4))&&settled.blockAt(arm.head.x(),arm.head.y(),arm.head.z()).equals(new BlockState(1,0))&&settled.blockAt(arm.pushed.x(),arm.pushed.y(),arm.pushed.z()).equals(new BlockState(0,0))&&settled.blockAt(arm.qc.x(),arm.qc.y(),arm.qc.z()).equals(new BlockState(1,0))&&settled.blockAt(arm.lever.x(),arm.lever.y(),arm.lever.z()).equals(new BlockState(69,1)),"sticky 29 qc precondition drift");
   arm.pulse(actor,signal,new BlockState(29,12),new BlockState(34,12),new BlockState(1,0),9,"sticky 29 qc extend");
   arm.pulse(actor,signal,new BlockState(29,4),new BlockState(1,0),new BlockState(0,0),1,"sticky 29 qc pull");
   actor.close();StickyPistonQcSetArm.awaitPlayers(server,0);server.save();
   reader=new B173WireClient("127.0.0.1",port,user,timeout);reader.connect();reader.synchronizePose();RemoteChunkSnapshot after=reader.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);
   arm.persist(after,cx,cz,new BlockState(29,4),new BlockState(1,0),new BlockState(0,0),"fresh sticky 29 qc pull drift");
   String evidence="column="+column[0]+",qc-extend=29:4->12,qc-pull=29:12->4,piston="+StickyPistonQcSetArm.cell(arm.piston)+":29:4->12->4,head="+StickyPistonQcSetArm.cell(arm.head)+":1:0->34:12->1:0,pushed="+StickyPistonQcSetArm.cell(arm.pushed)+":0:0->1:0->0:0,qc="+StickyPistonQcSetArm.cell(arm.qc)+":1:0,lever="+StickyPistonQcSetArm.cell(arm.lever)+":69:1->9->1,persisted=true,clients=2,disconnect=clean";
   String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=sticky29-west+qc-stone-above|settle="+fixture+"+"+signal+"ticks|cause=packet15-lever-activate-on-block-above-piston|effect=official-sticky29-qc-extend+pull|observation=fresh-login-packet51|"+evidence;
   System.out.println("WORLDLINE_M547_SET="+evidence);System.out.println("WORLDLINE_M547_TRACE="+trace);System.out.println("WORLDLINE_M547_SIGNATURE="+StickyPistonQcSetArm.sha(trace));
  }finally{actor.close();if(reader!=null)reader.close();server.close();}
 }
}
