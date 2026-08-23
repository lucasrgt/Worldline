package worldline.smoke.remainingdoororientsetb173;

import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.security.MessageDigest;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Places remaining wooden-door 64 look-yaw hinge/face halves 0/8, 1/9, 2/10, 3/11 as one SET. */
public final class RemainingDoorOrientSetSmoke{
 private RemainingDoorOrientSetSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=7)throw new IllegalArgumentException("usage: RemainingDoorOrientSetSmoke server.jar workspace port seed username chunkX chunkZ");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]);
  require(seed==17320110707L&&user.equals("DoorOrnt428")&&user.length()<=16,"remaining-door-orient-set identity drift");
  Duration timeout=Duration.ofSeconds(90);B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout),reader=null;
  BlockPosition top,east,south,pad1,pad2,pad3,face0,face1,face2,face3;int column;
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,new int[]{0,1},new int[]{1,324},new int[]{48,4},new int[]{0,0});actor.connect();actor.synchronizePose();require(actor.awaitInventory().occupiedSlots()==2,"remaining-door-orient-set inventory drift");RemoteChunkSnapshot initial=actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);top=foundation(initial,cx,cz);column=0;actor.selectHeldSlot(0);
   while(water(initial.blockAt(local(top.x(),cx),top.y()+1,local(top.z(),cz)).legacyId())){top=place(actor,top,BlockFace.UP,1);actor.moveAndObserve(0D,1D,0D,1);require(++column<=15,"water column exceeded remaining-door-orient-set fixture");}for(int lift=0;lift<8;lift++){top=place(actor,top,BlockFace.UP,1);actor.moveAndObserve(0D,1D,0D,1);column++;}
   east=place(actor,top,BlockFace.EAST,1);pad1=place(actor,east,BlockFace.EAST,1);south=place(actor,top,BlockFace.SOUTH,1);pad2=place(actor,south,BlockFace.SOUTH,1);east=place(actor,pad2,BlockFace.EAST,1);pad3=place(actor,east,BlockFace.EAST,1);
   actor.selectHeldSlot(1);face0=door(actor,top,-90F,0);face1=door(actor,pad1,0F,1);face2=door(actor,pad2,90F,2);face3=door(actor,pad3,180F,3);
   RemoteWorldView live=worldline.test.WorldlineSmokeAwait.observe(actor,5);require(halves(live,face0,0)&&halves(live,face1,1)&&halves(live,face2,2)&&halves(live,face3,3),"live remaining-door-orient-set drift");
   actor.close();awaitPlayers(server,0);server.save();reader=new B173WireClient("127.0.0.1",port,user,timeout);reader.connect();reader.synchronizePose();RemoteChunkSnapshot after=reader.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);
   require(persisted(after,face0,cx,cz,0)&&persisted(after,face1,cx,cz,1)&&persisted(after,face2,cx,cz,2)&&persisted(after,face3,cx,cz,3),"persisted remaining-door-orient-set drift");
   String evidence="column="+column+",support="+top.x()+":"+top.y()+":"+top.z()+":1:0,face0="+token(face0,0)+",face1="+token(face1,1)+",face2="+token(face2,2)+",face3="+token(face3,3)+",look=-90+0+90+180,persisted=64:0/8+64:1/9+64:2/10+64:3/11,clients=2,disconnect=clean";
   String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=raised-stone+woodendoor64:0/8+64:1/9+64:2/10+64:3/11|cause=packet15-item324-look-90+0+90+180|wire=packet53-door64:0/8+64:1/9+64:2/10+64:3/11|oracle=remaining-door-hinge-face+fresh-login|"+evidence;
   System.out.println("WORLDLINE_M428_SET="+evidence);System.out.println("WORLDLINE_M428_TRACE="+trace);System.out.println("WORLDLINE_M428_SIGNATURE="+sha(trace));
  }finally{actor.close();if(reader!=null)reader.close();server.close();}
 }
 private static BlockPosition door(B173WireClient a,BlockPosition support,float yaw,int low)throws Exception{BlockPosition lower=BlockFace.UP.adjacent(support),upper=BlockFace.UP.adjacent(lower);a.look(yaw,0F);worldline.test.WorldlineSmokeAwait.observe(a,2);a.useHeldItemOnBlock(support,BlockFace.UP);awaitDoor(a,lower,upper,low,low+8);return lower;}
 private static void awaitDoor(B173WireClient a,BlockPosition lower,BlockPosition upper,int lowMeta,int highMeta)throws Exception{RemoteWorldView v=a.awaitBlock(lower,new BlockState(64,lowMeta));if(!v.blockAt(upper.x(),upper.y(),upper.z()).equals(new BlockState(64,highMeta)))v=a.awaitBlock(upper,new BlockState(64,highMeta));BlockState low=v.blockAt(lower.x(),lower.y(),lower.z()),high=v.blockAt(upper.x(),upper.y(),upper.z());require(low.equals(new BlockState(64,lowMeta))&&high.equals(new BlockState(64,highMeta))&&(high.metadata()&8)==8,"wooden door cells "+low+" / "+high);}
 private static boolean halves(RemoteWorldView v,BlockPosition lower,int low){return v.blockAt(lower.x(),lower.y(),lower.z()).equals(new BlockState(64,low))&&v.blockAt(lower.x(),lower.y()+1,lower.z()).equals(new BlockState(64,low+8));}
 private static boolean persisted(RemoteChunkSnapshot q,BlockPosition lower,int cx,int cz,int low){return q.blockAt(local(lower.x(),cx),lower.y(),local(lower.z(),cz)).equals(new BlockState(64,low))&&q.blockAt(local(lower.x(),cx),lower.y()+1,local(lower.z(),cz)).equals(new BlockState(64,low+8));}
 private static String token(BlockPosition lower,int low){return lower.x()+":"+lower.y()+":"+lower.z()+":64:"+low+"/"+(low+8);}
 private static BlockPosition place(B173WireClient a,BlockPosition support,BlockFace face,int id)throws Exception{BlockPosition target=face.adjacent(support);a.placeHeldBlock(support,face);a.awaitBlock(target,new BlockState(id,0));return target;}
 private static BlockPosition foundation(RemoteChunkSnapshot q,int cx,int cz){for(int x=4;x<=11;x++)for(int z=4;z<=11;z++)for(int y=126;y>=1;y--)if(q.blockAt(x,y,z).legacyId()==3&&water(q.blockAt(x,y+1,z).legacyId()))return new BlockPosition(cx*16+x,y,cz*16+z);throw new IllegalStateException("no deterministic remaining-door-orient-set foundation");}
 private static boolean water(int id){return id==8||id==9;}private static int local(int v,int c){return v-c*16;}private static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}private static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}private static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
