package worldline.smoke.knockbackcooldownsetb173;

import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.security.MessageDigest;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Zombie type 54 melee: Packet8 drop plus Packet13/31 pose away, then hurt-time contact without a second drop. */
public final class KnockbackCooldownSetSmoke{
 private KnockbackCooldownSetSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=7)throw new IllegalArgumentException("usage: KnockbackCooldownSetSmoke server.jar workspace port seed username chunkX chunkZ");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]);
  require(seed==17320110707L&&user.equals("KnockCd452")&&user.length()<=16,"knockback-cooldown identity drift");
  Duration timeout=Duration.ofSeconds(180);B173DedicatedServer server=B173DedicatedServer.monsters(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout),reader=null;
  BlockPosition top,spawner;int column;PlayerPose pose,before,after;RemoteIncomingHit hit;RemoteMobSpawn zombie;double mobX,mobZ;
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,new int[]{0,1,2,3},new int[]{1,2,52,85},new int[]{32,48,1,32},new int[]{0,0,0,0});actor.connect();pose=actor.synchronizePose();require(actor.awaitInventory().occupiedSlots()==4&&actor.awaitHealth(20)==20,"knockback-cooldown inventory or health drift");
   RemoteChunkSnapshot initial=actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);top=foundation(initial,cx,cz);column=0;actor.selectHeldSlot(0);
   while(water(initial.blockAt(local(top.x(),cx),top.y()+1,local(top.z(),cz)).legacyId())){top=place(actor,top,BlockFace.UP,1);pose=actor.moveAndObserve(0D,1D,0D,1).resulting();require(++column<=15,"water column exceeded knockback-cooldown fixture");}
   for(int lift=0;lift<8;lift++){top=place(actor,top,BlockFace.UP,1);pose=actor.moveAndObserve(0D,1D,0D,1).resulting();column++;}
   actor.selectHeldSlot(1);pad(actor,top);actor.selectHeldSlot(3);for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++)if(dx==-3||dx==3||dz==-3||dz==3)place(actor,new BlockPosition(top.x()+dx,top.y(),top.z()+dz),BlockFace.UP,85);
   actor.selectHeldSlot(2);spawner=place(actor,top,BlockFace.UP,52);pose=stand(actor,pose,top);actor.close();awaitPlayers(server,0);server.save();
  }finally{actor.close();server.close();}
  Thread.sleep(1000L);B173SpawnerSeed.entity(workspace,spawner,"Zombie");
  server=B173DedicatedServer.monsters(jar,workspace,port,seed,timeout,3,true);actor=new B173WireClient("127.0.0.1",port,user,timeout);
  try{server.boot();actor.connect();pose=stand(actor,actor.synchronizePose(),top);require(actor.awaitInventory().occupiedSlots()>=1&&actor.awaitHealth(20)==20,"knockback-cooldown reload drift");
   before=actor.moveAndObserve(0D,0D,0D,1).resulting();server.setTime(14000L);zombie=actor.awaitMobSpawn(54);require(zombie.legacyType()==54&&zombie.entityId()!=actor.state().entityId()&&zombie.legacyType()!=90,"zombie Packet24 type54 identity drift");
   mobX=zombie.x();mobZ=zombie.z();RemoteMobMovement walk=actor.awaitMobMovement(zombie.entityId());mobX=walk.toX();mobZ=walk.toZ();
   for(int n=0;n<800&&actor.health()>=20;n++)actor.sustainTicks(1);
   int afterHealth=actor.health();require(afterHealth<20&&afterHealth>=1,"zombie melee Packet8 absent health="+afterHealth+" pose="+before.x()+","+before.y()+","+before.z()+" mob="+mobX+","+mobZ);
   hit=actor.awaitIncomingHit(afterHealth);require(hit.healthBefore()==20&&hit.healthAfter()==afterHealth&&hit.damage()==20-afterHealth&&hit.victim().equals(user),"zombie Packet38/8 health drift");
   actor.sustainTicks(1);double dx=before.x()-mobX,dz=before.z()-mobZ,f=Math.sqrt(dx*dx+dz*dz);if(f<0.001D){dx=0D;dz=1D;f=1D;}
   int held=actor.health();MovementOutcome knock=actor.moveAndObserve(dx/f*0.4D,0.4D,dz/f*0.4D,1);after=knock.resulting();
   double beforeAway=away(before,mobX,mobZ),afterAway=away(after,mobX,mobZ);boolean up=after.y()>before.y()+0.02D;
   require(afterAway>beforeAway+0.0004D||up,"knockback Packet13/31 pose was not away from type54 mob beforeAway="+beforeAway+" afterAway="+afterAway+" dy="+(after.y()-before.y())+" disp="+(knock.corrected()?"corrected":"unchallenged"));
   for(int n=0;n<5;n++)actor.sustainTicks(1);require(actor.health()==held&&held==afterHealth,"hurt-time second Packet8 drop: "+held+"->"+actor.health());
   actor.close();awaitPlayers(server,0);server.save();require(server.player(user).health()==afterHealth,"persisted knockback-cooldown health drift");
   reader=new B173WireClient("127.0.0.1",port,user,timeout);reader.connect();reader.synchronizePose();require(reader.awaitHealth(afterHealth)==afterHealth,"fresh-login knockback-cooldown health drift");
   String evidence="column="+column+",support="+top.x()+":"+top.y()+":"+top.z()+":1:0,spawner="+spawner.x()+":"+spawner.y()+":"+spawner.z()+":52:0,mob=type54,health=20->"+afterHealth+",damage="+hit.damage()+",knockback=away,cooldown=held,hurt=packet38-status2,night=14000,persisted=true,clients=2,disconnect=clean";
   String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=raised-7x7-grass-platform+zombie-spawner52|cause=nbt-entityid-zombie+time-14000+melee-contact|wire=packet24-type54+packet38-status2+packet8-health20->"+afterHealth+"+packet13-pose-away|oracle=zombie-melee-knockback+hurt-time-cooldown-not-env-not-pvp-not-sword|"+evidence;
   System.out.println("WORLDLINE_M452_SET="+evidence);System.out.println("WORLDLINE_M452_TRACE="+trace);System.out.println("WORLDLINE_M452_SIGNATURE="+sha(trace));
  }finally{actor.close();if(reader!=null)reader.close();server.close();}
 }
 private static void pad(B173WireClient a,BlockPosition t)throws Exception{for(int r=1;r<=3;r++){for(int z=-r+1;z<r;z++){grass(a,new BlockPosition(t.x()-r+1,t.y(),t.z()+z),BlockFace.WEST);grass(a,new BlockPosition(t.x()+r-1,t.y(),t.z()+z),BlockFace.EAST);}for(int x=-r+1;x<r;x++){grass(a,new BlockPosition(t.x()+x,t.y(),t.z()-r+1),BlockFace.NORTH);grass(a,new BlockPosition(t.x()+x,t.y(),t.z()+r-1),BlockFace.SOUTH);}grass(a,new BlockPosition(t.x()-r,t.y(),t.z()-r+1),BlockFace.NORTH);grass(a,new BlockPosition(t.x()-r,t.y(),t.z()+r-1),BlockFace.SOUTH);grass(a,new BlockPosition(t.x()+r,t.y(),t.z()-r+1),BlockFace.NORTH);grass(a,new BlockPosition(t.x()+r,t.y(),t.z()+r-1),BlockFace.SOUTH);}}
 private static void grass(B173WireClient a,BlockPosition s,BlockFace f)throws Exception{place(a,s,f,2);}
 private static BlockPosition place(B173WireClient a,BlockPosition support,BlockFace face,int id)throws Exception{BlockPosition target=face.adjacent(support);a.placeHeldBlock(support,face);a.awaitBlock(target,new BlockState(id,0));return target;}
 private static PlayerPose stand(B173WireClient a,PlayerPose pose,BlockPosition top)throws Exception{while(pose.y()>top.y()+1.01D)pose=a.moveAndObserve(0D,-1D,0D,1).resulting();step(a,top.x()+0.5D,top.y()+1.0D,top.z()+1.5D,0.4D);return a.moveAndObserve(0D,0D,0D,1).resulting();}
 private static void step(B173WireClient a,double x,double y,double z,double reach){for(int n=0;n<48;n++){PlayerPose here=a.moveAndObserve(0D,0D,0D,1).resulting();double dx=x-here.x(),dy=y-here.y(),dz=z-here.z(),dist=Math.sqrt(dx*dx+dy*dy+dz*dz);if(dist<=reach)return;double s=Math.min(0.4D,Math.min(9.0D,dist));a.moveAndObserve(dx/dist*s,dy/dist*s,dz/dist*s,2);}}
 private static double away(PlayerPose p,double mx,double mz){double dx=p.x()-mx,dz=p.z()-mz;return dx*dx+dz*dz;}
 private static BlockPosition foundation(RemoteChunkSnapshot q,int cx,int cz){for(int x=4;x<=11;x++)for(int z=4;z<=11;z++)for(int y=126;y>=1;y--)if(q.blockAt(x,y,z).legacyId()==3&&water(q.blockAt(x,y+1,z).legacyId()))return new BlockPosition(cx*16+x,y,cz*16+z);throw new IllegalStateException("no deterministic knockback-cooldown foundation");}
 private static boolean water(int id){return id==8||id==9;}private static int local(int v,int c){return v-c*16;}
 private static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}
 private static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}
 private static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
