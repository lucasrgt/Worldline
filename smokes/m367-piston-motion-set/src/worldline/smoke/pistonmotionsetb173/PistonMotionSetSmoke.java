package worldline.smoke.pistonmotionsetb173;

import java.nio.file.*;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Official piston 33 extend and retract plus sticky 29 pull in one cycle. */
public final class PistonMotionSetSmoke{
 private PistonMotionSetSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=9)throw new IllegalArgumentException("usage: PistonMotionSetSmoke server.jar workspace port seed username chunkX chunkZ fixtureTicks signalTicks");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]),fixture=Integer.parseInt(a[7]),signal=Integer.parseInt(a[8]);Duration timeout=Duration.ofSeconds(90);
  PistonMotionSetArm.require(user.length()<=16,"username exceeds 16");
  B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout),reader=null;
  PistonMotionSetArm normal,sticky;int[] column=new int[1];
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,new int[]{0,1,2,3},new int[]{1,33,29,69},new int[]{32,1,1,2},new int[]{0,0,0,0});actor.connect();actor.synchronizePose();actor.look(-90F,0F);PistonMotionSetArm.require(actor.awaitInventory().occupiedSlots()==4,"piston-motion inventory drift");
   RemoteChunkSnapshot initial=actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);BlockPosition top=PistonMotionSetArm.raise(actor,initial,cx,cz,column);
   normal=PistonMotionSetArm.place(actor,initial,top,cx,cz,1,33);
   actor.selectHeldSlot(0);actor.moveAndObserve(2D,0D,0D,2);BlockPosition stickySupport=PistonMotionSetArm.place(actor,top,BlockFace.SOUTH,1);stickySupport=PistonMotionSetArm.place(actor,stickySupport,BlockFace.SOUTH,1);actor.moveAndObserve(0D,0D,2D,2);
   sticky=PistonMotionSetArm.place(actor,initial,stickySupport,cx,cz,2,29);actor.selectHeldSlot(4);
   RemoteWorldView settled=actor.sustainTicks(fixture);
   PistonMotionSetArm.require(settled.blockAt(normal.piston.x(),normal.piston.y(),normal.piston.z()).equals(new BlockState(33,4))&&settled.blockAt(normal.head.x(),normal.head.y(),normal.head.z()).equals(new BlockState(1,0))&&settled.blockAt(normal.pushed.x(),normal.pushed.y(),normal.pushed.z()).equals(new BlockState(0,0)),"piston 33 precondition drift");
   PistonMotionSetArm.require(settled.blockAt(sticky.piston.x(),sticky.piston.y(),sticky.piston.z()).equals(new BlockState(29,4))&&settled.blockAt(sticky.head.x(),sticky.head.y(),sticky.head.z()).equals(new BlockState(1,0))&&settled.blockAt(sticky.pushed.x(),sticky.pushed.y(),sticky.pushed.z()).equals(new BlockState(0,0)),"sticky 29 precondition drift");
   normal.pulse(actor,signal,new BlockState(33,12),new BlockState(34,4),new BlockState(1,0),9,"piston 33 extend");
   normal.pulse(actor,signal,new BlockState(33,4),new BlockState(0,0),new BlockState(1,0),1,"piston 33 retract");
   sticky.pulse(actor,signal,new BlockState(29,12),new BlockState(34,12),new BlockState(1,0),9,"sticky 29 extend");
   sticky.pulse(actor,signal,new BlockState(29,4),new BlockState(1,0),new BlockState(0,0),1,"sticky 29 pull");
   actor.close();PistonMotionSetArm.awaitPlayers(server,0);server.save();
   reader=new B173WireClient("127.0.0.1",port,user,timeout);reader.connect();reader.synchronizePose();RemoteChunkSnapshot after=reader.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);
   normal.persist(after,cx,cz,new BlockState(33,4),new BlockState(0,0),new BlockState(1,0),"fresh piston 33 retract drift");
   sticky.persist(after,cx,cz,new BlockState(29,4),new BlockState(1,0),new BlockState(0,0),"fresh sticky 29 pull drift");
   String evidence="column="+column[0]+",extend=33:4->12,retract=33:12->4,sticky-pull=29:12->4,piston="+cell(normal.piston)+":33:4->12->4,head="+cell(normal.head)+":1:0->34:4->0:0,pushed="+cell(normal.pushed)+":0:0->1:0->1:0,sticky="+cell(sticky.piston)+":29:4->12->4,sticky-head="+cell(sticky.head)+":1:0->34:12->1:0,sticky-pushed="+cell(sticky.pushed)+":0:0->1:0->0:0,persisted=true,clients=2,disconnect=clean";
   String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=piston33-west+sticky29-west|settle="+fixture+"+"+signal+"ticks|cause=packet15-lever-activate+deactivate|effect=official-piston33-extend+retract+sticky29-pull|observation=fresh-login-packet51|"+evidence;
   System.out.println("WORLDLINE_M367_SET="+evidence);System.out.println("WORLDLINE_M367_TRACE="+trace);System.out.println("WORLDLINE_M367_SIGNATURE="+PistonMotionSetArm.sha(trace));
  }finally{actor.close();if(reader!=null)reader.close();server.close();}
 }
 private static String cell(BlockPosition p){return p.x()+":"+p.y()+":"+p.z();}
}
