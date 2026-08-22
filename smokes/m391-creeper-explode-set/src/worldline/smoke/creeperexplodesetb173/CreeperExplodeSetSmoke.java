package worldline.smoke.creeperexplodesetb173;

import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.security.MessageDigest;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Spawns one official creeper (Packet24 type 50) and detonates Packet60 strength 3 into nearby wool and dirt. */
public final class CreeperExplodeSetSmoke{
 private CreeperExplodeSetSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=7)throw new IllegalArgumentException("usage: CreeperExplodeSetSmoke server.jar workspace port seed username chunkX chunkZ");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]);
  require(seed==17320110707L&&user.equals("Creeper391")&&user.length()<=16,"creeper-explode-set identity drift");
  Duration timeout=Duration.ofSeconds(180);B173DedicatedServer server=B173DedicatedServer.monsters(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout),reader=null;
  BlockPosition top,dirt,wool,spawner;int column;RemoteExplosion explosion;PlayerPose pose;
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,new int[]{0,1,2,3},new int[]{1,3,35,52},new int[]{32,48,48,1},new int[]{0,0,0,0});actor.connect();pose=actor.synchronizePose();require(actor.awaitInventory().occupiedSlots()==4,"creeper-explode-set inventory drift");
   RemoteChunkSnapshot initial=actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);top=foundation(initial,cx,cz);column=0;actor.selectHeldSlot(0);
   while(water(initial.blockAt(local(top.x(),cx),top.y()+1,local(top.z(),cz)).legacyId())){top=place(actor,top,BlockFace.UP,1);pose=actor.moveAndObserve(0D,1D,0D,1).resulting();require(++column<=15,"water column exceeded creeper-explode-set fixture");}for(int lift=0;lift<8;lift++){top=place(actor,top,BlockFace.UP,1);pose=actor.moveAndObserve(0D,1D,0D,1).resulting();column++;}
   pad(actor,top);dirt=new BlockPosition(top.x()-1,top.y(),top.z());wool=new BlockPosition(top.x()+1,top.y(),top.z());
   require(actor.sustainTicks(2).blockAt(dirt.x(),dirt.y(),dirt.z()).equals(new BlockState(3,0))&&actor.sustainTicks(1).blockAt(wool.x(),wool.y(),wool.z()).equals(new BlockState(35,0)),"wool/dirt pad drift");
   actor.selectHeldSlot(3);spawner=place(actor,new BlockPosition(top.x(),top.y(),top.z()-1),BlockFace.UP,52);pose=stand(actor,pose,top);actor.close();awaitPlayers(server,0);server.save();
  }finally{actor.close();server.close();}
  Thread.sleep(1000L);B173SpawnerSeed.entity(workspace,spawner,"Creeper");server=B173DedicatedServer.monsters(jar,workspace,port,seed,timeout,3,true);actor=new B173WireClient("127.0.0.1",port,user,timeout);
  try{server.boot();actor.connect();pose=stand(actor,actor.synchronizePose(),top);require(actor.awaitInventory().occupiedSlots()>=1&&Math.abs(pose.y()-(top.y()+1.0D))<2D,"creeper-explode-set reload pose drift");server.setTime(14000L);
   RemoteMobSpawn creeper=actor.awaitMobSpawn(50);require(creeper.legacyType()==50&&creeper.entityId()!=actor.state().entityId()&&creeper.legacyType()!=90,"creeper Packet24 type50 identity drift");
   actor.sustainTicks(40);explosion=actor.awaitExplosion();require(explosion.strength()==3F&&explosion.destroyed().size()>1&&Math.abs(explosion.x()-(top.x()+0.5D))<6D&&Math.abs(explosion.z()-(top.z()+0.5D))<6D,"Packet60 creeper center/strength drift: "+explosion.x()+":"+explosion.y()+":"+explosion.z()+":"+explosion.strength());
   RemoteWorldView after=actor.sustainTicks(2);require(hits(explosion,top,true)>0&&hits(explosion,top,false)>0&&crater(after,top,true)+crater(after,top,false)>0,"creeper Packet60 wool/dirt destruction drift");
   actor.close();awaitPlayers(server,0);server.save();B173PlayerSeed.write(workspace,user,top.x()+0.5D,top.y()+1.0D,top.z()+0.5D);
   reader=new B173WireClient("127.0.0.1",port,user,timeout);reader.connect();reader.synchronizePose();RemoteChunkSnapshot persisted=reader.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);
   int west=crater(persisted,top,cx,cz,true),east=crater(persisted,top,cx,cz,false);require(west+east>0,"persisted creeper crater drift: "+west+":"+east);
   String evidence="column="+column+",support="+top.x()+":"+top.y()+":"+top.z()+":1:0,pad="+dirt.x()+":"+dirt.y()+":"+dirt.z()+"+"+wool.x()+":"+wool.y()+":"+wool.z()+",destroyed-materials=dirt3+wool35,spawner="+spawner.x()+":"+spawner.y()+":"+spawner.z()+":52:0,mob=type50,packet60=strength3,destroyed=multiple+wool+dirt,night=14000,persisted=crater,clients=3,disconnect=clean";
   String trace="v2|server=official-b1.7.3|seed="+seed+"|fixture=raised-7x7-dirt+wool-pad+creeper-spawner52|cause=nbt-entityid-creeper+time-14000+proximity-fuse|wire=packet24-type50+packet60-strength3|oracle=creeper-explode-wool+dirt-set-not-tnt4-not-bed5|"+evidence;
   System.out.println("WORLDLINE_M391_SET="+evidence);System.out.println("WORLDLINE_M391_TRACE="+trace);System.out.println("WORLDLINE_M391_SIGNATURE="+sha(trace));
  }finally{actor.close();if(reader!=null)reader.close();server.close();}
 }
 private static void pad(B173WireClient a,BlockPosition t)throws Exception{for(int r=1;r<=3;r++){for(int z=-r+1;z<r;z++){cell(a,new BlockPosition(t.x()-r+1,t.y(),t.z()+z),BlockFace.WEST,1,3);cell(a,new BlockPosition(t.x()+r-1,t.y(),t.z()+z),BlockFace.EAST,2,35);}for(int x=-r+1;x<r;x++){cell(a,new BlockPosition(t.x()+x,t.y(),t.z()-r+1),BlockFace.NORTH,x<=0?1:2,x<=0?3:35);cell(a,new BlockPosition(t.x()+x,t.y(),t.z()+r-1),BlockFace.SOUTH,x<=0?1:2,x<=0?3:35);}cell(a,new BlockPosition(t.x()-r,t.y(),t.z()-r+1),BlockFace.NORTH,1,3);cell(a,new BlockPosition(t.x()-r,t.y(),t.z()+r-1),BlockFace.SOUTH,1,3);cell(a,new BlockPosition(t.x()+r,t.y(),t.z()-r+1),BlockFace.NORTH,2,35);cell(a,new BlockPosition(t.x()+r,t.y(),t.z()+r-1),BlockFace.SOUTH,2,35);}}
 private static void cell(B173WireClient a,BlockPosition s,BlockFace f,int slot,int id)throws Exception{a.selectHeldSlot(slot);place(a,s,f,id);}
 private static BlockPosition place(B173WireClient a,BlockPosition support,BlockFace face,int id)throws Exception{BlockPosition target=face.adjacent(support);a.placeHeldBlock(support,face);a.awaitBlock(target,new BlockState(id,0));return target;}
 private static PlayerPose stand(B173WireClient a,PlayerPose pose,BlockPosition top)throws Exception{while(pose.y()>top.y()+1.01D)pose=a.moveAndObserve(0D,-1D,0D,1).resulting();require(Math.abs(pose.x()-(top.x()+0.5D))<3D&&Math.abs(pose.z()-(top.z()+0.5D))<3D,"actor missed creeper pad");return pose;}
 private static int hits(RemoteExplosion e,BlockPosition t,boolean west){int n=0;for(BlockPosition p:e.destroyed()){int dx=p.x()-t.x(),dz=p.z()-t.z();if(Math.abs(dx)>3||p.y()!=t.y()||Math.abs(dz)>3||(dx==0&&dz==0))continue;if(west?dx<=0:dx>0)n++;}return n;}
 private static int crater(RemoteWorldView v,BlockPosition t,boolean west){int n=0;for(int dx=-3;dx<=3;dx++)for(int dy=0;dy<=1;dy++)for(int dz=-3;dz<=3;dz++){if((west&&dx>0)||(!west&&dx<=0)||(dx==0&&dz==0&&dy==0))continue;if(v.blockAt(t.x()+dx,t.y()+dy,t.z()+dz).legacyId()==0)n++;}return n;}
 private static int crater(RemoteChunkSnapshot q,BlockPosition t,int cx,int cz,boolean west){int n=0;for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++){if((west&&dx>0)||(!west&&dx<=0)||(dx==0&&dz==0))continue;if(q.blockAt(local(t.x()+dx,cx),t.y(),local(t.z()+dz,cz)).legacyId()==0)n++;}return n;}
 private static BlockPosition foundation(RemoteChunkSnapshot q,int cx,int cz){for(int x=4;x<=11;x++)for(int z=4;z<=11;z++)for(int y=126;y>=1;y--)if(q.blockAt(x,y,z).legacyId()==3&&water(q.blockAt(x,y+1,z).legacyId()))return new BlockPosition(cx*16+x,y,cz*16+z);throw new IllegalStateException("no deterministic creeper-explode-set foundation");}
 private static boolean water(int id){return id==8||id==9;}private static int local(int v,int c){return v-c*16;}private static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}private static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}private static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
