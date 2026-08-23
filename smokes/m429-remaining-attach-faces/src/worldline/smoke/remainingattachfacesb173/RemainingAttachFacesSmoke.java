package worldline.smoke.remainingattachfacesb173;

import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.security.MessageDigest;import java.time.Duration;import worldline.api.*;import worldline.b173server.*;

/** Places remaining wall-attach facings of ladder 65, trapdoor 96, and wall sign 68 together as a SET. */
public final class RemainingAttachFacesSmoke{
 private RemainingAttachFacesSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=7)throw new IllegalArgumentException("usage: RemainingAttachFacesSmoke server.jar workspace port seed username chunkX chunkZ");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]);int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]);
  require(seed==17320110707L&&user.equals("AttachFace429")&&user.length()<=16,"remaining-attach-faces identity drift");
  Duration timeout=Duration.ofSeconds(90);B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout),reader=null;
  BlockPosition top,low,mid,high,lw,ls,ln,tw,ts,tn,sw,ss,sn;int column;
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,new int[]{0,1,2,3},new int[]{1,65,96,323},new int[]{48,8,8,8},new int[]{0,0,0,0});actor.connect();actor.synchronizePose();require(actor.awaitInventory().occupiedSlots()==4,"remaining-attach-faces inventory drift");
   RemoteChunkSnapshot initial=actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);top=foundation(initial,cx,cz);column=0;actor.selectHeldSlot(0);
   while(water(initial.blockAt(local(top.x(),cx),top.y()+1,local(top.z(),cz)).legacyId())){top=attach(actor,top,BlockFace.UP,1,0);actor.moveAndObserve(0D,1D,0D,1);require(++column<=15,"water column exceeded remaining-attach-faces fixture");}for(int lift=0;lift<10;lift++){top=attach(actor,top,BlockFace.UP,1,0);actor.moveAndObserve(0D,1D,0D,1);column++;}
   high=top;mid=new BlockPosition(top.x(),top.y()-1,top.z());low=new BlockPosition(top.x(),top.y()-2,top.z());
   actor.selectHeldSlot(1);lw=attach(actor,low,BlockFace.WEST,65,4);ls=attach(actor,low,BlockFace.SOUTH,65,3);ln=attach(actor,low,BlockFace.NORTH,65,2);
   actor.selectHeldSlot(2);tw=attach(actor,mid,BlockFace.WEST,96,2);ts=attach(actor,mid,BlockFace.SOUTH,96,1);tn=attach(actor,mid,BlockFace.NORTH,96,0);
   actor.selectHeldSlot(3);sw=wall(actor,high,BlockFace.WEST,4,90F);ss=wall(actor,high,BlockFace.SOUTH,3,0F);sn=wall(actor,high,BlockFace.NORTH,2,180F);
   RemoteWorldView live=worldline.test.WorldlineSmokeAwait.observe(actor,5);require(ok(live,lw,65,4)&&ok(live,ls,65,3)&&ok(live,ln,65,2)&&ok(live,tw,96,2)&&ok(live,ts,96,1)&&ok(live,tn,96,0)&&ok(live,sw,68,4)&&ok(live,ss,68,3)&&ok(live,sn,68,2)&&live.blockAt(BlockFace.EAST.adjacent(low).x(),low.y(),BlockFace.EAST.adjacent(low).z()).legacyId()==0&&live.blockAt(sw.x(),sw.y(),sw.z()).legacyId()!=63,"live remaining-attach-faces drift");
   actor.close();awaitPlayers(server,0);server.save();reader=new B173WireClient("127.0.0.1",port,user,timeout);reader.connect();reader.synchronizePose();RemoteChunkSnapshot after=reader.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);
   require(same(after,low,cx,cz,1,0)&&same(after,mid,cx,cz,1,0)&&same(after,high,cx,cz,1,0)&&same(after,lw,cx,cz,65,4)&&same(after,ls,cx,cz,65,3)&&same(after,ln,cx,cz,65,2)&&same(after,tw,cx,cz,96,2)&&same(after,ts,cx,cz,96,1)&&same(after,tn,cx,cz,96,0)&&same(after,sw,cx,cz,68,4)&&same(after,ss,cx,cz,68,3)&&same(after,sn,cx,cz,68,2),"persisted remaining-attach-faces drift");
   String evidence="column="+column+",low="+token(low,1,0)+",mid="+token(mid,1,0)+",high="+token(high,1,0)+",ladder="+token(lw,65,4)+"+"+token(ls,65,3)+"+"+token(ln,65,2)+",trapdoor="+token(tw,96,2)+"+"+token(ts,96,1)+"+"+token(tn,96,0)+",wallsign="+token(sw,68,4)+"+"+token(ss,68,3)+"+"+token(sn,68,2)+",persisted=true,clients=2,disconnect=clean";
   String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=raised-stone-column3+ladder65:4+65:3+65:2+trapdoor96:2+96:1+96:0+item323-wall68:4+68:3+68:2|cause=packet15-item65-west+south+north+packet15-item96-west+south+north+packet15-item323-west+south+north|wire=packet53-ladder65:4+65:3+65:2+trapdoor96:2+96:1+96:0+sign68:4+68:3+68:2|oracle=remaining-wall-attach-faces+fresh-login|"+evidence;
   System.out.println("WORLDLINE_M429_SET="+evidence);System.out.println("WORLDLINE_M429_TRACE="+trace);System.out.println("WORLDLINE_M429_SIGNATURE="+sha(trace));
  }finally{actor.close();if(reader!=null)reader.close();server.close();}
 }
 private static BlockPosition attach(B173WireClient a,BlockPosition support,BlockFace face,int id,int meta)throws Exception{BlockPosition target=face.adjacent(support);a.placeHeldBlock(support,face);a.awaitBlock(target,new BlockState(id,meta));return target;}
 private static BlockPosition wall(B173WireClient a,BlockPosition support,BlockFace face,int meta,float yaw)throws Exception{BlockPosition target=face.adjacent(support);a.look(yaw,0F);a.useHeldItemOnBlock(support,face);BlockState placed=wait(a,target,68);require(placed.equals(new BlockState(68,meta))&&placed.legacyId()!=63,"wall sign facing drift 68:"+meta+": "+placed);return target;}
 private static BlockState wait(B173WireClient a,BlockPosition cell,int id)throws Exception{return worldline.test.WorldlineSmokeAwait.awaitBlockMatching(a,cell,s->s.legacyId()==id,"block "+id,40);}
 private static BlockPosition foundation(RemoteChunkSnapshot q,int cx,int cz){for(int x=4;x<=11;x++)for(int z=4;z<=11;z++)for(int y=126;y>=1;y--)if(q.blockAt(x,y,z).legacyId()==3&&water(q.blockAt(x,y+1,z).legacyId()))return new BlockPosition(cx*16+x,y,cz*16+z);throw new IllegalStateException("no deterministic remaining-attach-faces foundation");}
 private static boolean ok(RemoteWorldView v,BlockPosition p,int id,int meta){return v.blockAt(p.x(),p.y(),p.z()).equals(new BlockState(id,meta));}
 private static boolean same(RemoteChunkSnapshot q,BlockPosition p,int cx,int cz,int id,int meta){return q.blockAt(local(p.x(),cx),p.y(),local(p.z(),cz)).equals(new BlockState(id,meta));}
 private static String token(BlockPosition p,int id,int meta){return p.x()+":"+p.y()+":"+p.z()+":"+id+":"+meta;}
 private static boolean water(int id){return id==8||id==9;}private static int local(int v,int c){return v-c*16;}private static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}private static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}private static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
