package worldline.smoke.fallingsandb173;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Removes one support block and observes one official sand block settle below. */
public final class FallingSandSmoke {
    private FallingSandSmoke() {}
    public static void main(String[]arguments)throws Exception{
        if(arguments.length!=9)throw new IllegalArgumentException("usage: FallingSandSmoke server.jar workspace port seed username chunkX chunkZ fixtureTicks gravityTicks");
        Path jar=Paths.get(arguments[0]),workspace=Paths.get(arguments[1]);int port=Integer.parseInt(arguments[2]);long seed=Long.parseLong(arguments[3]);
        String username=arguments[4];int chunkX=Integer.parseInt(arguments[5]),chunkZ=Integer.parseInt(arguments[6]);
        int fixtureTicks=Integer.parseInt(arguments[7]),gravityTicks=Integer.parseInt(arguments[8]);Duration timeout=Duration.ofSeconds(90);
        B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);
        B173WireClient actor=new B173WireClient("127.0.0.1",port,username,timeout),reader=null;
        RemoteChunkSnapshot before,after;BlockPosition foundation,support,sand;BlockState opened,settled,cleared;int column;
        try{server.boot();B173PlayerSeed.writeInventory(workspace,username,4.5D,60D,4.5D,new int[]{0,1},
                    new int[]{1,12},new int[]{16,1},new int[]{0,0});actor.connect();actor.synchronizePose();
            RemoteInventoryView inventory=actor.awaitInventory();require(inventory.occupiedSlots()==2,"sand fixture inventory drift");
            RemoteChunkSnapshot initial=actor.awaitRemoteChunk(chunkX,chunkZ).chunkAt(chunkX,chunkZ);
            foundation=foundation(initial,chunkX,chunkZ);support=foundation;column=0;actor.selectHeldSlot(0);
            while(water(initial.blockAt(local(support.x(),chunkX),support.y()+1,local(support.z(),chunkZ)).legacyId())){
                actor.placeHeldBlock(support,BlockFace.UP);support=BlockFace.UP.adjacent(support);actor.awaitBlock(support,new BlockState(1,0));
                actor.moveAndObserve(0D,1D,0D,1);column++;require(column<=15,"water column exceeded fixture stack");}
            actor.placeHeldBlock(support,BlockFace.UP);support=BlockFace.UP.adjacent(support);actor.awaitBlock(support,new BlockState(1,0));
            actor.moveAndObserve(0D,1D,0D,1);column++;sand=BlockFace.UP.adjacent(support);
            require(initial.blockAt(local(sand.x(),chunkX),sand.y(),local(sand.z(),chunkZ)).legacyId()==0,"sand target was not initial air");
            actor.selectHeldSlot(1);actor.placeHeldBlock(support,BlockFace.UP);actor.awaitBlock(sand,new BlockState(12,0));
            actor.selectHeldSlot(2);actor.moveAndObserve(0D,-2D,0D,2);before=worldline.test.WorldlineSmokeAwait.observe(actor,fixtureTicks).chunkAt(chunkX,chunkZ);
            require(before.blockAt(local(support.x(),chunkX),support.y(),local(support.z(),chunkZ)).equals(new BlockState(1,0))
                    &&before.blockAt(local(sand.x(),chunkX),sand.y(),local(sand.z(),chunkZ)).equals(new BlockState(12,0)),"stable sand fixture drift");
            actor.beginBreak(support);Thread.sleep(3000L);actor.finishBreak(support);opened=actor.awaitBlock(support,new BlockState(0,0)).blockAt(support.x(),support.y(),support.z());
            RemoteWorldView live=worldline.test.WorldlineSmokeAwait.observe(actor,gravityTicks);settled=live.blockAt(support.x(),support.y(),support.z());cleared=live.blockAt(sand.x(),sand.y(),sand.z());
            require(opened.equals(new BlockState(0,0))&&settled.equals(new BlockState(12,0))&&cleared.equals(new BlockState(0,0)),"sand did not settle one block: "+opened+" / "+settled+" / "+cleared);
            actor.close();awaitPlayers(server,0);server.save();reader=new B173WireClient("127.0.0.1",port,username,timeout);
            reader.connect();reader.synchronizePose();after=reader.awaitRemoteChunk(chunkX,chunkZ).chunkAt(chunkX,chunkZ);
            require(after.blockAt(local(support.x(),chunkX),support.y(),local(support.z(),chunkZ)).equals(settled)
                    &&after.blockAt(local(sand.x(),chunkX),sand.y(),local(sand.z(),chunkZ)).equals(cleared),"fresh settled sand drift");
        }finally{actor.close();if(reader!=null)reader.close();server.close();}
        StateDelta delta=delta(before,after);require(delta.changed==2,"falling sand changed unrelated states: "+delta);
        String evidence="column="+column+",lower="+support.x()+":"+support.y()+":"+support.z()+":1:0->12:0,upper="
                +sand.x()+":"+sand.y()+":"+sand.z()+":12:0->0:0,states="+delta;
        String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=stone-column+supported-sand12|settle="+fixtureTicks+"+"+gravityTicks
                +"ticks|cause=packet14-remove-support|confirmation=packet53-air|effect=official-falling-sand-settle|observation=live-packet53+fresh-login-packet51|"+evidence+"|disconnect=clean";
        System.out.println("WORLDLINE_M119_GRAVITY="+evidence);System.out.println("WORLDLINE_M119_TRACE="+trace);System.out.println("WORLDLINE_M119_SIGNATURE="+sha256(trace));
    }
    private static BlockPosition foundation(RemoteChunkSnapshot chunk,int chunkX,int chunkZ){for(int x=4;x<=11;x++)for(int z=4;z<=11;z++)for(int y=126;y>=1;y--)
        if(chunk.blockAt(x,y,z).legacyId()==3&&water(chunk.blockAt(x,y+1,z).legacyId()))return new BlockPosition(chunkX*16+x,y,chunkZ*16+z);throw new IllegalStateException("no deterministic sand foundation");}
    private static StateDelta delta(RemoteChunkSnapshot before,RemoteChunkSnapshot after)throws Exception{MessageDigest digest=MessageDigest.getInstance("SHA-256");ByteBuffer row=ByteBuffer.allocate(10);int changed=0;
        for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=0;y<128;y++){BlockState a=before.blockAt(x,y,z),b=after.blockAt(x,y,z);if(!a.equals(b)){changed++;row.clear();row.putShort((short)x).putShort((short)y).putShort((short)z).put((byte)a.legacyId()).put((byte)a.metadata()).put((byte)b.legacyId()).put((byte)b.metadata());digest.update(row.array());}}return new StateDelta(changed,hex(digest.digest()));}
    private static boolean water(int id){return id==8||id==9;}private static int local(int value,int chunk){return value-chunk*16;}
    private static void awaitPlayers(B173DedicatedServer server,int count)throws Exception{long end=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<end){if(server.players().size()==count)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}
    private static String sha256(String value)throws Exception{return hex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}private static String hex(byte[]value){StringBuilder result=new StringBuilder();for(byte item:value)result.append(String.format("%02x",item&255));return result.toString();}
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}private static final class StateDelta{final int changed;final String hash;StateDelta(int c,String h){changed=c;hash=h;}@Override public String toString(){return changed+":"+hash;}}
}
