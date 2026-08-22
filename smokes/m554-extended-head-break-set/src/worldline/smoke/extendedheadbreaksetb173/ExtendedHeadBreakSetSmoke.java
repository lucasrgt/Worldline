package worldline.smoke.extendedheadbreaksetb173;

import java.nio.file.*;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Official piston 33 extend then Packet14-break of the extended base, freezing head-34 leftover cleanup. */
public final class ExtendedHeadBreakSetSmoke{
 private ExtendedHeadBreakSetSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=9)throw new IllegalArgumentException("usage: ExtendedHeadBreakSetSmoke server.jar workspace port seed username chunkX chunkZ fixtureTicks signalTicks");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]),fixture=Integer.parseInt(a[7]),signal=Integer.parseInt(a[8]);Duration timeout=Duration.ofSeconds(90);
  ExtendedHeadBreakSetArm.require(seed==17320110707L&&user.equals("HeadBreak554")&&user.length()<=16,"extended-head-break identity drift");
  B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout),reader=null;
  ExtendedHeadBreakSetArm arm;int[] column=new int[1];
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,new int[]{0,1,2,3},new int[]{1,33,69,257},new int[]{32,1,1,1},new int[]{0,0,0,0});actor.connect();actor.synchronizePose();actor.look(-90F,0F);ExtendedHeadBreakSetArm.require(actor.awaitInventory().occupiedSlots()==4,"extended-head-break inventory drift");
   RemoteChunkSnapshot initial=actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);BlockPosition top=ExtendedHeadBreakSetArm.raise(actor,initial,cx,cz,column);
   arm=ExtendedHeadBreakSetArm.place(actor,initial,top,cx,cz);
   RemoteWorldView settled=actor.sustainTicks(fixture);
   ExtendedHeadBreakSetArm.require(settled.blockAt(arm.piston.x(),arm.piston.y(),arm.piston.z()).equals(new BlockState(33,4))&&settled.blockAt(arm.head.x(),arm.head.y(),arm.head.z()).equals(new BlockState(1,0))&&settled.blockAt(arm.pushed.x(),arm.pushed.y(),arm.pushed.z()).equals(new BlockState(0,0)),"piston 33 precondition drift");
   arm.extend(actor,signal);arm.breakBase(actor,signal);
   actor.close();ExtendedHeadBreakSetArm.awaitPlayers(server,0);server.save();
   reader=new B173WireClient("127.0.0.1",port,user,timeout);reader.connect();reader.synchronizePose();RemoteChunkSnapshot after=reader.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);
   arm.persist(after,cx,cz);
   String evidence="column="+column[0]+",extend=33:4->12,head-break=33:12->0,piston="+cell(arm.piston)+":33:4->12->0,head="+cell(arm.head)+":1:0->34:4->0:0,pushed="+cell(arm.pushed)+":0:0->1:0->1:0,lever="+cell(arm.lever)+":69:1->9,drops=packet21-33,persisted=true,clients=2,disconnect=clean";
   String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=piston33-west-extended|settle="+fixture+"+"+signal+"ticks|cause=packet15-lever-activate+packet14-ironpick257-base|effect=official-extended-piston-base-break+head34-removed|observation=fresh-login-packet51|"+evidence;
   System.out.println("WORLDLINE_M554_SET="+evidence);System.out.println("WORLDLINE_M554_TRACE="+trace);System.out.println("WORLDLINE_M554_SIGNATURE="+ExtendedHeadBreakSetArm.sha(trace));
  }finally{actor.close();if(reader!=null)reader.close();server.close();}
 }
 private static String cell(BlockPosition p){return p.x()+":"+p.y()+":"+p.z();}
}
