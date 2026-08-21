package worldline.smoke.tntprimesetb173;

import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.security.MessageDigest;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Places TNT 46, primes it with flint-and-steel 259, observes Packet23 type 50, and freezes the explosion crater. */
public final class TntPrimeSetSmoke{
 private TntPrimeSetSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=9)throw new IllegalArgumentException("usage: TntPrimeSetSmoke server.jar workspace port seed username chunkX chunkZ fixtureTicks fuseTicks");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]);int fixtureTicks=Integer.parseInt(a[7]),fuseTicks=Integer.parseInt(a[8]);
  require(seed==17320110707L&&user.equals("TntPrime381")&&user.length()<=16,"tnt-prime-set identity drift");
  Duration timeout=Duration.ofSeconds(90);B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout),reader=null;
  BlockPosition top,tnt;int column;RemoteObjectSpawn primed;RemoteExplosion explosion;
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,new int[]{0,1,2},new int[]{1,46,259},new int[]{32,1,1},new int[]{0,0,0});actor.connect();actor.synchronizePose();require(actor.awaitInventory().occupiedSlots()==3,"tnt-prime-set inventory drift");
   RemoteChunkSnapshot initial=actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);top=foundation(initial,cx,cz);column=0;actor.selectHeldSlot(0);
   while(water(initial.blockAt(local(top.x(),cx),top.y()+1,local(top.z(),cz)).legacyId())){top=place(actor,top,BlockFace.UP,1);actor.moveAndObserve(0D,1D,0D,1);require(++column<=15,"water column exceeded tnt-prime-set fixture");}
   for(int lift=0;lift<6;lift++){top=place(actor,top,BlockFace.UP,1);actor.moveAndObserve(0D,1D,0D,1);column++;}
   actor.selectHeldSlot(1);tnt=place(actor,top,BlockFace.UP,46);RemoteWorldView before=actor.sustainTicks(fixtureTicks);
   require(before.blockAt(top.x(),top.y(),top.z()).equals(new BlockState(1,0))&&before.blockAt(tnt.x(),tnt.y(),tnt.z()).equals(new BlockState(46,0)),"tnt 46:0 baseline drift");
   actor.selectHeldSlot(2);actor.useHeldItemOnBlock(tnt,BlockFace.UP);
   primed=actor.awaitObjectSpawn(50);require(primed.type()==50&&primed.entityId()!=actor.state().entityId()&&primed.throwerId()==0,"Packet23 type 50 primed TNT drift");
   actor.awaitBlock(tnt,new BlockState(0,0));actor.moveAndObserve(10D,0D,0D,4);explosion=actor.awaitExplosion();
   require(explosion.strength()==4F&&Math.abs(explosion.x()-(tnt.x()+0.5D))<2D&&Math.abs(explosion.y()-(tnt.y()+0.5D))<4D&&Math.abs(explosion.z()-(tnt.z()+0.5D))<2D,"Packet60 center/strength drift");
   RemoteWorldView after=actor.sustainTicks(1);require(explosion.destroyed().contains(top)&&after.blockAt(top.x(),top.y(),top.z()).equals(new BlockState(0,0))&&after.blockAt(tnt.x(),tnt.y(),tnt.z()).equals(new BlockState(0,0)),"tnt-prime-set crater drift");
   actor.close();awaitPlayers(server,0);server.save();reader=new B173WireClient("127.0.0.1",port,user,timeout);reader.connect();reader.synchronizePose();
   RemoteWorldView persisted=reader.awaitRemoteChunk(cx,cz);require(persisted.blockAt(top.x(),top.y(),top.z()).equals(new BlockState(0,0))&&persisted.blockAt(tnt.x(),tnt.y(),tnt.z()).equals(new BlockState(0,0)),"fresh crater persistence drift");
   String evidence="column="+column+",support="+top.x()+":"+top.y()+":"+top.z()+":1:0->0:0,tnt="+tnt.x()+":"+tnt.y()+":"+tnt.z()+":46:0->0:0,flint=259,packet23=50,strength=4,crater=true,persisted=true,clients=2,disconnect=clean";
   String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=raised-stone+tnt46+flint259|cause=packet15-item46+packet15-item259-prime|fuse="+fuseTicks+"ticks|wire=packet23-type50+packet60-center+strength+relative-destroyed-cells|oracle=live-prime-object+crater-support-air+fresh-login|"+evidence;
   System.out.println("WORLDLINE_M381_SET="+evidence);System.out.println("WORLDLINE_M381_TRACE="+trace);System.out.println("WORLDLINE_M381_SIGNATURE="+sha(trace));
  }finally{actor.close();if(reader!=null)reader.close();server.close();}
 }
 private static BlockPosition place(B173WireClient a,BlockPosition support,BlockFace face,int id)throws Exception{BlockPosition target=face.adjacent(support);a.placeHeldBlock(support,face);a.awaitBlock(target,new BlockState(id,0));return target;}
 private static BlockPosition foundation(RemoteChunkSnapshot q,int cx,int cz){for(int x=4;x<=11;x++)for(int z=4;z<=11;z++)for(int y=126;y>=1;y--)if(q.blockAt(x,y,z).legacyId()==3&&water(q.blockAt(x,y+1,z).legacyId()))return new BlockPosition(cx*16+x,y,cz*16+z);throw new IllegalStateException("no deterministic tnt-prime-set foundation");}
 private static boolean water(int id){return id==8||id==9;}private static int local(int v,int c){return v-c*16;}
 private static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}
 private static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}
 private static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
