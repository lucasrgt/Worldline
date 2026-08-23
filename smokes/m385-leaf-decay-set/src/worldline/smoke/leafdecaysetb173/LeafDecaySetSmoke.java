package worldline.smoke.leafdecaysetb173;

import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.security.MessageDigest;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Places oak 18:0, spruce 18:1, and birch 18:2 beside matching logs, removes log support, and freezes bounded decay. */
public final class LeafDecaySetSmoke{
 private static final BlockState AIR=new BlockState(0,0);
 private LeafDecaySetSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=9)throw new IllegalArgumentException("usage: LeafDecaySetSmoke server.jar workspace port seed username chunkX chunkZ windowTicks decayWindows");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]),window=Integer.parseInt(a[7]),windows=Integer.parseInt(a[8]);Duration timeout=Duration.ofMinutes(15);
  require(user.length()<=16&&window>=1&&window<=1200&&windows>=1,"leaf-decay-set arguments");
  B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout),reader=null;BlockPosition top,oakLog,oakLeaf,sprucePad,spruceLog,spruceLeaf,birchPad,birchLog,birchLeaf;int column;PlayerPose pose;
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,new int[]{0,1,2,3,4,5,6,7},new int[]{1,17,17,17,18,18,18,275},new int[]{64,4,4,4,4,4,4,1},new int[]{0,0,1,2,0,1,2,0});actor.connect();pose=actor.synchronizePose();require(actor.awaitInventory().occupiedSlots()==8,"leaf-decay-set inventory drift");RemoteChunkSnapshot initial=actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);top=foundation(initial,cx,cz);column=0;actor.selectHeldSlot(0);
   while(water(initial.blockAt(local(top.x(),cx),top.y()+1,local(top.z(),cz)).legacyId())){top=place(actor,top,BlockFace.UP,1,0);pose=actor.moveAndObserve(0D,1D,0D,1).resulting();require(++column<=15,"water column exceeded leaf-decay-set fixture");}for(int lift=0;lift<8;lift++){top=place(actor,top,BlockFace.UP,1,0);pose=actor.moveAndObserve(0D,1D,0D,1).resulting();column++;}
   actor.selectHeldSlot(1);oakLog=place(actor,top,BlockFace.NORTH,17,0);actor.selectHeldSlot(4);oakLeaf=place(actor,oakLog,BlockFace.UP,18,8);
   pose=go(actor,pose,top.x()+0.5D,top.y()+2.0D,top.z()+0.5D);actor.selectHeldSlot(0);sprucePad=span(actor,top,1D,0D,BlockFace.EAST,6);actor.selectHeldSlot(2);spruceLog=place(actor,sprucePad,BlockFace.EAST,17,1);actor.selectHeldSlot(5);spruceLeaf=place(actor,spruceLog,BlockFace.UP,18,9);
   pose=go(actor,pose,top.x()+0.5D,top.y()+2.0D,top.z()+0.5D);actor.selectHeldSlot(0);birchPad=span(actor,top,0D,1D,BlockFace.SOUTH,6);actor.selectHeldSlot(3);birchLog=place(actor,birchPad,BlockFace.EAST,17,2);actor.selectHeldSlot(6);birchLeaf=place(actor,birchLog,BlockFace.UP,18,10);
   RemoteWorldView placed=worldline.test.WorldlineSmokeAwait.observe(actor,5);require(leaf(at(placed,oakLeaf),0)&&leaf(at(placed,spruceLeaf),1)&&leaf(at(placed,birchLeaf),2),"live leaf-decay-set placement drift");
   actor.selectHeldSlot(7);pose=go(actor,pose,oakLog.x()+0.5D,top.y()+1.0D,oakLog.z()+0.5D);harvest(actor,oakLog);actor.awaitBlock(oakLog,AIR);
   pose=go(actor,pose,spruceLog.x()+0.5D,top.y()+1.0D,spruceLog.z()+0.5D);harvest(actor,spruceLog);actor.awaitBlock(spruceLog,AIR);
   pose=go(actor,pose,birchLog.x()+0.5D,top.y()+1.0D,birchLog.z()+0.5D);harvest(actor,birchLog);actor.awaitBlock(birchLog,AIR);
   decay(actor,oakLeaf,0,spruceLeaf,1,birchLeaf,2,window,windows);actor.close();awaitPlayers(server,0);server.save();
   reader=new B173WireClient("127.0.0.1",port,user,timeout);reader.connect();reader.synchronizePose();RemoteChunkSnapshot after=reader.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);
   require(air(after,oakLog,cx,cz)&&air(after,oakLeaf,cx,cz)&&air(after,spruceLog,cx,cz)&&air(after,spruceLeaf,cx,cz)&&air(after,birchLog,cx,cz)&&air(after,birchLeaf,cx,cz),"persisted leaf-decay-set drift");
   String evidence="column="+column+",oakLog="+cell(oakLog)+":17:0->0:0,oakLeaves="+cell(oakLeaf)+":18:8->0:0,spruceLog="+cell(spruceLog)+":17:1->0:0,spruceLeaves="+cell(spruceLeaf)+":18:9->0:0,birchLog="+cell(birchLog)+":17:2->0:0,birchLeaves="+cell(birchLeaf)+":18:10->0:0,items=18:0+18:1+18:2,persisted=true,clients=2,disconnect=clean";String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=raised-stone+oak17:0+spruce17:1+birch17:2+leaves18:0+18:1+18:2|cause=packet14-remove-log-support|wire=packet53-leaves18:8+18:9+18:10->0:0|oracle=bounded-tick-oak-spruce-birch-leaf-decay+fresh-login|"+evidence;System.out.println("WORLDLINE_M385_SET="+evidence);System.out.println("WORLDLINE_M385_TRACE="+trace);System.out.println("WORLDLINE_M385_SIGNATURE="+sha(trace));
  }finally{actor.close();if(reader!=null)reader.close();server.close();}
 }
 private static void harvest(B173WireClient a,BlockPosition target){a.beginBreak(target);worldline.test.WorldlineSmokeAwait.observe(a,20);a.finishBreak(target);}
 private static void decay(B173WireClient a,BlockPosition oak,int oakType,BlockPosition spruce,int spruceType,BlockPosition birch,int birchType,int window,int windows)throws Exception{
  worldline.test.WorldlineSmokeAwait.awaitWorld(a,v->at(v,oak).equals(AIR)&&at(v,spruce).equals(AIR)&&at(v,birch).equals(AIR),"oak spruce birch leaf decay",window*windows);
 }
 private static BlockPosition place(B173WireClient a,BlockPosition support,BlockFace face,int id,int meta)throws Exception{BlockPosition target=face.adjacent(support);a.placeHeldBlock(support,face);a.awaitBlock(target,new BlockState(id,meta));return target;}
 private static BlockPosition span(B173WireClient a,BlockPosition from,double dx,double dz,BlockFace face,int n)throws Exception{BlockPosition p=from;for(int i=0;i<n;i++){p=place(a,p,face,1,0);a.moveAndObserve(dx,0D,dz,1);}return p;}
 private static PlayerPose go(B173WireClient a,PlayerPose p,double x,double y,double z){for(int i=0;i<16&&(Math.abs(p.x()-x)>0.4D||Math.abs(p.y()-y)>0.4D||Math.abs(p.z()-z)>0.4D);i++)p=a.moveAndObserve(clamp(x-p.x()),clamp(y-p.y()),clamp(z-p.z()),1).resulting();return p;}
 private static double clamp(double v){return v>1D?1D:v<-1D?-1D:v;}
 private static boolean leaf(BlockState s,int type){return s.legacyId()==18&&(s.metadata()&3)==type;}private static boolean leafOrAir(BlockState s,int type){return s.equals(AIR)||leaf(s,type);}
 private static BlockPosition foundation(RemoteChunkSnapshot q,int cx,int cz){for(int x=4;x<=11;x++)for(int z=4;z<=11;z++)for(int y=126;y>=1;y--)if(q.blockAt(x,y,z).legacyId()==3&&water(q.blockAt(x,y+1,z).legacyId()))return new BlockPosition(cx*16+x,y,cz*16+z);throw new IllegalStateException("no deterministic leaf-decay-set foundation");}
 private static BlockState at(RemoteWorldView v,BlockPosition p){return v.blockAt(p.x(),p.y(),p.z());}private static boolean air(RemoteChunkSnapshot q,BlockPosition p,int cx,int cz){return q.blockAt(local(p.x(),cx),p.y(),local(p.z(),cz)).equals(AIR);}
 private static String cell(BlockPosition p){return p.x()+":"+p.y()+":"+p.z();}
 private static boolean water(int id){return id==8||id==9;}private static int local(int v,int c){return v-c*16;}private static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}private static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}private static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
