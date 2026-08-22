package worldline.smoke.stickybudsetb173;

import java.nio.file.*;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Official sticky 29 BUD: diagonal-above lever QC without extend, neighbor update extends, pulse pulls. */
public final class StickyBudSetSmoke{
 private StickyBudSetSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=9)throw new IllegalArgumentException("usage: StickyBudSetSmoke server.jar workspace port seed username chunkX chunkZ fixtureTicks signalTicks");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]),fixture=Integer.parseInt(a[7]),signal=Integer.parseInt(a[8]);Duration timeout=Duration.ofSeconds(90);
  StickyBudSetArm.require(user.length()<=16,"username exceeds 16");
  B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout),reader=null;
  StickyBudSetArm arm;int[] column=new int[1];
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,new int[]{0,1,2},new int[]{1,29,69},new int[]{32,1,1},new int[]{0,0,0});actor.connect();actor.synchronizePose();actor.look(-90F,0F);StickyBudSetArm.require(actor.awaitInventory().occupiedSlots()==3,"sticky-bud inventory drift");
   RemoteChunkSnapshot initial=actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);BlockPosition top=StickyBudSetArm.raise(actor,initial,cx,cz,column);
   arm=StickyBudSetArm.place(actor,initial,top,cx,cz);actor.selectHeldSlot(4);
   arm.primed(actor,fixture);
   arm.neighborExtend(actor,signal);
   arm.pulsePull(actor,signal);
   actor.close();StickyBudSetArm.awaitPlayers(server,0);server.save();
   reader=new B173WireClient("127.0.0.1",port,user,timeout);reader.connect();reader.synchronizePose();RemoteChunkSnapshot after=reader.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);
   arm.persist(after,cx,cz);
   String evidence="column="+column[0]+",primed=29:4,bud-extend=29:4->12,sticky-pull=29:12->4,piston="+StickyBudSetArm.cell(arm.piston)+":29:4->12->4,head="+StickyBudSetArm.cell(arm.head)+":1:0->34:12->1:0,pushed="+StickyBudSetArm.cell(arm.pushed)+":0:0->1:0->0:0,powered="+StickyBudSetArm.cell(arm.powered)+":1:0,lever="+StickyBudSetArm.cell(arm.lever)+":69:"+arm.leverOff+"->"+arm.leverOn+"->"+arm.leverOff+",update="+StickyBudSetArm.cell(arm.update)+":0:0->1:0,direct-power=false,continuous-power=false,persisted=true,clients=2,disconnect=clean";
   String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=sticky29-west+diagonal-above-lever69-qc+payload|settle="+fixture+"+"+signal+"ticks|cause=packet15-neighbor-stone-north|effect=official-sticky29-bud-extend+head34:12+sticky-pull|observation=fresh-login-packet51|"+evidence;
   System.out.println("WORLDLINE_M549_SET="+evidence);System.out.println("WORLDLINE_M549_TRACE="+trace);System.out.println("WORLDLINE_M549_SIGNATURE="+StickyBudSetArm.sha(trace));
  }finally{actor.close();if(reader!=null)reader.close();server.close();}
 }
}
