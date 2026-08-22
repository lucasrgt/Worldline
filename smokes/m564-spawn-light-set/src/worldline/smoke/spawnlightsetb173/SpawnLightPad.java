package worldline.smoke.spawnlightsetb173;

import java.io.IOException;import java.nio.file.*;import java.time.Duration;import java.util.*;import java.util.stream.Collectors;import java.util.stream.Stream;import worldline.api.*;import worldline.b173server.*;

/** Raises the isolated 7x7 grass pad and two default spawners, then retargets Creeper plus Zombie. */
final class SpawnLightPad{
 final BlockPosition first,second;final int column;
 private SpawnLightPad(BlockPosition first,BlockPosition second,int column){this.first=first;this.second=second;this.column=column;}
 static SpawnLightPad build(Path jar,Path workspace,int port,long seed,String user,int cx,int cz,Duration timeout)throws Exception{
  B173DedicatedServer server=B173DedicatedServer.monsters(jar,workspace,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout);BlockPosition top,first,second;int column;
  try{server.boot();B173PlayerSeed.writeInventory(workspace,user,4.5D,60D,4.5D,new int[]{0,1,2,3,4},new int[]{1,2,52,52,50},new int[]{32,48,1,1,64},new int[]{0,0,0,0,0});actor.connect();actor.synchronizePose();require(actor.awaitInventory().occupiedSlots()==5,"spawn-light-set inventory drift");RemoteChunkSnapshot initial=actor.awaitRemoteChunk(cx,cz).chunkAt(cx,cz);top=foundation(initial,cx,cz);column=0;actor.selectHeldSlot(0);
   while(water(initial.blockAt(local(top.x(),cx),top.y()+1,local(top.z(),cz)).legacyId())){top=place(actor,top,BlockFace.UP,1);actor.moveAndObserve(0D,1D,0D,1);require(++column<=15,"water column exceeded spawn-light-set fixture");}for(int lift=0;lift<8;lift++){top=place(actor,top,BlockFace.UP,1);actor.moveAndObserve(0D,1D,0D,1);column++;}
   actor.selectHeldSlot(1);for(int r=1;r<=3;r++){for(int z=-r+1;z<r;z++){grass(actor,new BlockPosition(top.x()-r+1,top.y(),top.z()+z),BlockFace.WEST);grass(actor,new BlockPosition(top.x()+r-1,top.y(),top.z()+z),BlockFace.EAST);}for(int x=-r+1;x<r;x++){grass(actor,new BlockPosition(top.x()+x,top.y(),top.z()-r+1),BlockFace.NORTH);grass(actor,new BlockPosition(top.x()+x,top.y(),top.z()+r-1),BlockFace.SOUTH);}grass(actor,new BlockPosition(top.x()-r,top.y(),top.z()-r+1),BlockFace.NORTH);grass(actor,new BlockPosition(top.x()-r,top.y(),top.z()+r-1),BlockFace.SOUTH);grass(actor,new BlockPosition(top.x()+r,top.y(),top.z()-r+1),BlockFace.NORTH);grass(actor,new BlockPosition(top.x()+r,top.y(),top.z()+r-1),BlockFace.SOUTH);}
   actor.selectHeldSlot(2);first=place(actor,top,BlockFace.UP,52);actor.selectHeldSlot(3);second=place(actor,first,BlockFace.EAST,52);actor.sustainTicks(5);actor.close();awaitPlayers(server,0);server.save();
  }finally{actor.close();server.close();}
  Thread.sleep(1000L);B173SpawnerSeed.entity(workspace,first,"Creeper");B173SpawnerSeed.entity(workspace,second,"Zombie");return new SpawnLightPad(first,second,column);
 }
 static void copyWorld(Path from,Path to)throws IOException{
  Path src=from.toAbsolutePath().normalize().resolve("world"),dst=to.toAbsolutePath().normalize().resolve("world");require(src.startsWith(from.toAbsolutePath().normalize())&&Files.isDirectory(src),"spawn-light-set world absent");if(Files.exists(dst)){require(dst.startsWith(to.toAbsolutePath().normalize())&&!dst.equals(to.toAbsolutePath().normalize()),"unsafe lit copy");try(Stream<Path>p=Files.walk(dst)){for(Path f:p.sorted(Comparator.reverseOrder()).collect(Collectors.toList()))Files.delete(f);}}
  try(Stream<Path>p=Files.walk(src)){for(Path f:p.collect(Collectors.toList())){if("session.lock".equals(f.getFileName().toString()))continue;Path t=dst.resolve(src.relativize(f).toString());if(Files.isDirectory(f))Files.createDirectories(t);else{Files.createDirectories(t.getParent());Files.copy(f,t,StandardCopyOption.REPLACE_EXISTING);}}}
 }
 static int lightPad(B173WireClient a,BlockPosition first,BlockPosition second){a.selectHeldSlot(4);int n=0;for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++){if((dx==0&&dz==0)||(dx==1&&dz==0))continue;torch(a,new BlockPosition(first.x()+dx,first.y()-1,first.z()+dz));n++;}torch(a,first);torch(a,second);a.sustainTicks(5);return n+2;}
 static BlockPosition torch(B173WireClient a,BlockPosition support){BlockPosition target=BlockFace.UP.adjacent(support);a.placeHeldBlock(support,BlockFace.UP);a.awaitBlock(target,new BlockState(50,5));return target;}
 static BlockPosition place(B173WireClient a,BlockPosition support,BlockFace face,int id){BlockPosition target=face.adjacent(support);a.placeHeldBlock(support,face);a.awaitBlock(target,new BlockState(id,0));return target;}
 private static void grass(B173WireClient a,BlockPosition support,BlockFace face){place(a,support,face,2);}
 private static BlockPosition foundation(RemoteChunkSnapshot q,int cx,int cz){for(int x=4;x<=11;x++)for(int z=4;z<=11;z++)for(int y=126;y>=1;y--)if(q.blockAt(x,y,z).legacyId()==3&&water(q.blockAt(x,y+1,z).legacyId()))return new BlockPosition(cx*16+x,y,cz*16+z);throw new IllegalStateException("no deterministic spawn-light-set foundation");}
 static String cell(BlockPosition p,int id,int meta){return p.x()+":"+p.y()+":"+p.z()+":"+id+":"+meta;}
 static boolean water(int id){return id==8||id==9;}static int local(int v,int c){return v-c*16;}
 static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}
 static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
