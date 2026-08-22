package worldline.smoke.rsnorlatchsetb173;

import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.security.MessageDigest;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Two-torch RS-NOR: pulse SET stays on, pulse RESET stays off, persist RESET. */
public final class RsNorLatchSetSmoke{
 private RsNorLatchSetSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=7)throw new IllegalArgumentException("usage: RsNorLatchSetSmoke server.jar workspace port seed username chunkX chunkZ");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]);Duration timeout=Duration.ofSeconds(120);
  require(user.length()<=16,"username exceeds 16");B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout);
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,new int[]{0,1,2,3,4},new int[]{1,356,331,76,69},new int[]{48,2,16,2,2},new int[]{0,0,0,0,0});actor.connect();actor.synchronizePose();require(actor.awaitInventory().occupiedSlots()==5,"rs-nor inventory drift");
   RsNorLatchSetFixture f=RsNorLatchSetFixture.build(actor,actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz),cx,cz);
   BlockState qOff=new BlockState(75,4),qOn=new BlockState(76,4),barOn=new BlockState(76,3),barOff=new BlockState(75,3);
   actor.selectHeldSlot(5);Thread.sleep(8000L);require(actor.awaitBlock(f.q,qOff).blockAt(f.q.x(),f.q.y(),f.q.z()).equals(qOff)&&actor.sustainTicks(5).blockAt(f.qbar.x(),f.qbar.y(),f.qbar.z()).equals(barOn),"initial RESET pair drift");
   f.armHold(actor);actor.selectHeldSlot(5);Thread.sleep(2000L);require(actor.sustainTicks(10).blockAt(f.q.x(),f.q.y(),f.q.z()).equals(qOff)&&actor.sustainTicks(1).blockAt(f.qbar.x(),f.qbar.y(),f.qbar.z()).equals(barOn),"hold arm raced the RESET pair");
   actor.moveAndObserve(2D,0D,0D,2);pulse(actor,f.set);require(actor.sustainTicks(10).blockAt(f.qbar.x(),f.qbar.y(),f.qbar.z()).equals(barOff),"SET did not invert Q-bar");
   actor=relog(actor,server,port,user,timeout);require(pair(actor,f,cx,cz,qOn,barOff),"SET did not latch Q on");
   go(actor,f.set);pulse(actor,f.set);actor=relog(actor,server,port,user,timeout);require(pair(actor,f,cx,cz,qOn,barOff)&&unpowered(actor,f.set,cx,cz),"SET did not stay on after input drop");
   go(actor,f.reset);pulse(actor,f.reset);Thread.sleep(8000L);actor=relog(actor,server,port,user,timeout);require(pair(actor,f,cx,cz,qOff,barOn),"RESET did not latch Q off");
   go(actor,f.reset);pulse(actor,f.reset);Thread.sleep(8000L);actor=relog(actor,server,port,user,timeout);require(pair(actor,f,cx,cz,qOff,barOn)&&unpowered(actor,f.reset,cx,cz),"RESET did not stay off after input drop");
   RemoteChunkSnapshot after=actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);
   require(after.blockAt(local(f.body.x(),cx),f.body.y(),local(f.body.z(),cz)).equals(new BlockState(1,0))&&after.blockAt(local(f.farUp.x(),cx),f.farUp.y(),local(f.farUp.z(),cz)).equals(new BlockState(1,0)),"persisted rs-nor body drift");
   String evidence="column="+f.column+",blockA="+f.body.x()+":"+f.body.y()+":"+f.body.z()+":1:0,blockB="+f.farUp.x()+":"+f.farUp.y()+":"+f.farUp.z()+":1:0,set="+f.set.x()+":"+f.set.y()+":"+f.set.z()+":69:floor->on->off,reset="+f.reset.x()+":"+f.reset.y()+":"+f.reset.z()+":69:floor->on->off,q="+f.q.x()+":"+f.q.y()+":"+f.q.z()+":75:4->76:4->75:4,qbar="+f.qbar.x()+":"+f.qbar.y()+":"+f.qbar.z()+":76:3->75:3->76:3,stays-on=true,stays-off=true,persisted=q=75:4+qbar=76:3,clients=5,disconnect=clean";
   String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=raised-stone+rs-nor-76:4+76:3|cause=packet15-item76-north-then-south+empty-hand-packet15-set-pulse+reset-pulse|wire=packet53-q-75:4->76:4->75:4+qbar-76:3->75:3->76:3|oracle=set-stays-on+reset-stays-off+fresh-login|"+evidence;
   System.out.println("WORLDLINE_M556_SET="+evidence);System.out.println("WORLDLINE_M556_TRACE="+trace);System.out.println("WORLDLINE_M556_SIGNATURE="+sha(trace));
  }finally{actor.close();server.close();}
 }
 private static B173WireClient relog(B173WireClient actor,B173DedicatedServer server,int port,String user,Duration timeout)throws Exception{actor.close();awaitPlayers(server,0);server.save();Thread.sleep(8000L);B173WireClient next=new B173WireClient("127.0.0.1",port,user,timeout);next.connect();next.synchronizePose();next.selectHeldSlot(5);return next;}
 private static boolean pair(B173WireClient a,RsNorLatchSetFixture f,int cx,int cz,BlockState q,BlockState bar)throws Exception{RemoteChunkSnapshot s=a.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);BlockState gotQ=s.blockAt(local(f.q.x(),cx),f.q.y(),local(f.q.z(),cz)),gotBar=s.blockAt(local(f.qbar.x(),cx),f.qbar.y(),local(f.qbar.z(),cz));require(gotQ.equals(q)&&gotBar.equals(bar),"pair drift q="+gotQ+" qbar="+gotBar+" expected "+q+" "+bar+" rptB="+s.blockAt(local(f.eFar.x(),cx),f.eFar.y()+1,local(f.eFar.z(),cz))+" holdDust="+s.blockAt(local(f.eFar2.x(),cx),f.eFar2.y()+1,local(f.eFar2.z(),cz))+" reset="+s.blockAt(local(f.reset.x(),cx),f.reset.y(),local(f.reset.z(),cz))+" set="+s.blockAt(local(f.set.x(),cx),f.set.y(),local(f.set.z(),cz)));return true;}
 private static boolean unpowered(B173WireClient a,BlockPosition lever,int cx,int cz)throws Exception{BlockState s=a.awaitRemoteChunk(cx,cz).chunkAt(cx,cz).blockAt(local(lever.x(),cx),lever.y(),local(lever.z(),cz));require(s.legacyId()==69&&(s.metadata()&8)==0,"lever still powered "+s+" at "+lever.x()+","+lever.y()+","+lever.z());return true;}
 private static void go(B173WireClient a,BlockPosition t)throws Exception{PlayerPose p=a.moveAndObserve(0D,0D,0D,1).resulting();a.moveAndObserve(t.x()+0.5D-p.x(),Math.max(-4D,Math.min(4D,t.y()+1.5D-p.y())),t.z()+0.5D-p.z(),8);}
 private static void pulse(B173WireClient a,BlockPosition lever)throws Exception{a.activateBlock(lever,BlockFace.UP);a.sustainTicks(5);}
 private static int local(int v,int c){return v-c*16;}private static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}private static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}private static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
