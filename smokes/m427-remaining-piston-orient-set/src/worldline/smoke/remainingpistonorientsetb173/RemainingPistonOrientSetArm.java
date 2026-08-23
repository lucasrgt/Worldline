package worldline.smoke.remainingpistonorientsetb173;

import java.nio.charset.StandardCharsets;import java.security.MessageDigest;
import worldline.api.*;import worldline.b173server.*;

/** Raised-stone pads and look-derived remaining piston facings. */
public final class RemainingPistonOrientSetArm{
 static BlockPosition raise(B173WireClient a,RemoteChunkSnapshot initial,int cx,int cz,int[] column)throws Exception{
  BlockPosition top=foundation(initial,cx,cz);column[0]=0;a.selectHeldSlot(0);
  while(water(at(initial,BlockFace.UP.adjacent(top),cx,cz).legacyId())){top=place(a,top,BlockFace.UP,1);a.moveAndObserve(0D,1D,0D,1);require(++column[0]<=15,"water column exceeded remaining-piston-orient fixture");}
  for(int lift=0;lift<8;lift++){top=place(a,top,BlockFace.UP,1);a.moveAndObserve(0D,1D,0D,1);column[0]++;}
  return top;
 }
 static BlockPosition stack(B173WireClient a,BlockPosition support,int n)throws Exception{a.selectHeldSlot(0);BlockPosition p=support;for(int i=0;i<n;i++)p=place(a,p,BlockFace.UP,1);return p;}
 static BlockPosition piston(B173WireClient a,BlockPosition support,int slot,int id,int meta,float yaw)throws Exception{
  BlockPosition t=BlockFace.UP.adjacent(support);a.selectHeldSlot(slot);a.look(yaw,0F);a.placeHeldBlock(support,BlockFace.UP);
  BlockState got=worldline.test.WorldlineSmokeAwait.observe(a,5).blockAt(t.x(),t.y(),t.z());require(got.equals(new BlockState(id,meta)),"live piston "+id+":"+meta+" facing drift: "+got+" at "+cell(t));return t;
 }
 static BlockPosition place(B173WireClient a,BlockPosition support,BlockFace face,int id)throws Exception{BlockPosition target=face.adjacent(support);a.placeHeldBlock(support,face);a.awaitBlock(target,new BlockState(id,0));return target;}
 static BlockPosition foundation(RemoteChunkSnapshot c,int cx,int cz){for(int x=4;x<=11;x++)for(int z=4;z<=11;z++)for(int y=126;y>=1;y--)if(c.blockAt(x,y,z).legacyId()==3&&water(c.blockAt(x,y+1,z).legacyId()))return new BlockPosition(cx*16+x,y,cz*16+z);throw new IllegalStateException("no deterministic remaining-piston-orient foundation");}
 static BlockState at(RemoteChunkSnapshot c,BlockPosition p,int cx,int cz){return c.blockAt(p.x()-cx*16,p.y(),p.z()-cz*16);}
 static boolean water(int id){return id==8||id==9;}
 static void persist(RemoteChunkSnapshot after,int cx,int cz,BlockPosition p,int id,int meta,String label){require(at(after,p,cx,cz).equals(new BlockState(id,meta)),label+" persist drift: "+at(after,p,cx,cz));}
 static void awaitPlayers(B173DedicatedServer s,int n)throws Exception{long e=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<e){if(s.players().size()==n)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}
 static String cell(BlockPosition p){return p.x()+":"+p.y()+":"+p.z();}
 static String token(BlockPosition p,int id,int meta){return cell(p)+":"+id+":"+meta;}
 static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}
 static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
