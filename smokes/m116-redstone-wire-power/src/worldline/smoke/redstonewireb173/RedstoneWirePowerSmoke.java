package worldline.smoke.redstonewireb173;

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

/** Powers one official redstone wire from one adjacent side lever. */
public final class RedstoneWirePowerSmoke {
    private RedstoneWirePowerSmoke() {}
    public static void main(String[]arguments)throws Exception{
        if(arguments.length!=9)throw new IllegalArgumentException("usage: RedstoneWirePowerSmoke server.jar workspace port seed username chunkX chunkZ fixtureTicks signalTicks");
        Path jar=Paths.get(arguments[0]),workspace=Paths.get(arguments[1]);int port=Integer.parseInt(arguments[2]);long seed=Long.parseLong(arguments[3]);
        String username=arguments[4];int chunkX=Integer.parseInt(arguments[5]),chunkZ=Integer.parseInt(arguments[6]);
        int fixtureTicks=Integer.parseInt(arguments[7]),signalTicks=Integer.parseInt(arguments[8]);Duration timeout=Duration.ofSeconds(90);
        B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);
        B173WireClient actor=new B173WireClient("127.0.0.1",port,username,timeout),reader=null;
        RemoteChunkSnapshot before,after;BlockPosition foundation,top,lever,wire;BlockState leverOff,leverOn,wireOff,wireOn;int column;
        try{server.boot();B173PlayerSeed.writeInventory(workspace,username,4.5D,60D,4.5D,new int[]{0,1,2},
                    new int[]{1,69,331},new int[]{16,1,1},new int[]{0,0,0});actor.connect();actor.synchronizePose();actor.look(-90F,0F);
            RemoteInventoryView inventory=actor.awaitInventory();require(inventory.occupiedSlots()==3,"wire fixture inventory drift");
            RemoteChunkSnapshot initial=actor.awaitRemoteChunk(chunkX,chunkZ).chunkAt(chunkX,chunkZ);
            foundation=foundation(initial,chunkX,chunkZ);top=foundation;column=0;actor.selectHeldSlot(0);
            while(water(initial.blockAt(local(top.x(),chunkX),top.y()+1,local(top.z(),chunkZ)).legacyId())){
                actor.placeHeldBlock(top,BlockFace.UP);top=BlockFace.UP.adjacent(top);actor.awaitBlock(top,new BlockState(1,0));
                actor.moveAndObserve(0D,1D,0D,1);column++;require(column<=15,"water column exceeded fixture stack");}
            actor.placeHeldBlock(top,BlockFace.UP);top=BlockFace.UP.adjacent(top);actor.awaitBlock(top,new BlockState(1,0));
            actor.moveAndObserve(0D,1D,0D,1);column++;wire=BlockFace.UP.adjacent(top);lever=BlockFace.EAST.adjacent(top);
            require(initial.blockAt(local(wire.x(),chunkX),wire.y(),local(wire.z(),chunkZ)).legacyId()==0
                    &&initial.blockAt(local(lever.x(),chunkX),lever.y(),local(lever.z(),chunkZ)).legacyId()==0,"signal targets were not initial air");
            actor.selectHeldSlot(2);actor.useHeldItemOnBlock(top,BlockFace.UP);RemoteWorldView dust=worldline.test.WorldlineSmokeAwait.observe(actor,5);
            wireOff=dust.blockAt(wire.x(),wire.y(),wire.z());require(wireOff.equals(new BlockState(55,0)),"unpowered wire drift: "+wireOff);
            actor.selectHeldSlot(1);actor.placeHeldBlock(top,BlockFace.EAST);RemoteWorldView placed=worldline.test.WorldlineSmokeAwait.observe(actor,5);
            leverOff=placed.blockAt(lever.x(),lever.y(),lever.z());require(leverOff.legacyId()==69&&leverOff.metadata()<8,"side lever drift: "+leverOff);
            actor.selectHeldSlot(3);before=worldline.test.WorldlineSmokeAwait.observe(actor,fixtureTicks).chunkAt(chunkX,chunkZ);
            leverOff=before.blockAt(local(lever.x(),chunkX),lever.y(),local(lever.z(),chunkZ));
            wireOff=before.blockAt(local(wire.x(),chunkX),wire.y(),local(wire.z(),chunkZ));require(wireOff.equals(new BlockState(55,0)),"wire powered before treatment");
            actor.activateBlock(lever,BlockFace.UP);RemoteWorldView live=worldline.test.WorldlineSmokeAwait.observe(actor,signalTicks);
            leverOn=live.blockAt(lever.x(),lever.y(),lever.z());wireOn=live.blockAt(wire.x(),wire.y(),wire.z());
            require(leverOn.legacyId()==69&&leverOn.metadata()!=leverOff.metadata()&&wireOn.legacyId()==55
                    &&wireOn.metadata()>0,"lever signal did not power wire: "+leverOn+" / "+wireOn);
            actor.close();awaitPlayers(server,0);server.save();reader=new B173WireClient("127.0.0.1",port,username,timeout);
            reader.connect();reader.synchronizePose();after=reader.awaitRemoteChunk(chunkX,chunkZ).chunkAt(chunkX,chunkZ);
            require(after.blockAt(local(lever.x(),chunkX),lever.y(),local(lever.z(),chunkZ)).equals(leverOn)
                    &&after.blockAt(local(wire.x(),chunkX),wire.y(),local(wire.z(),chunkZ)).equals(wireOn),"fresh powered state drift");
        }finally{actor.close();if(reader!=null)reader.close();server.close();}
        StateDelta delta=delta(before,after);require(delta.changed==2,"signal changed unrelated states: "+delta);
        String evidence="column="+column+",lever="+lever.x()+":"+lever.y()+":"+lever.z()+":"+leverOff.metadata()+"->"+leverOn.metadata()
                +",wire="+wire.x()+":"+wire.y()+":"+wire.z()+":"+wireOff.metadata()+"->"+wireOn.metadata()+",states="+delta;
        String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=stone-column+lever69+dust331-wire55|settle="+fixtureTicks
                +"+"+signalTicks+"ticks|cause=packet15-lever-activate|effect=packet53-wire-power|observation=fresh-login-packet51|"+evidence+"|disconnect=clean";
        System.out.println("WORLDLINE_M116_SIGNAL="+evidence);System.out.println("WORLDLINE_M116_TRACE="+trace);
        System.out.println("WORLDLINE_M116_SIGNATURE="+sha256(trace));
    }
    private static BlockPosition foundation(RemoteChunkSnapshot chunk,int chunkX,int chunkZ){for(int x=4;x<=11;x++)for(int z=4;z<=11;z++)for(int y=126;y>=1;y--)
        if(chunk.blockAt(x,y,z).legacyId()==3&&water(chunk.blockAt(x,y+1,z).legacyId()))return new BlockPosition(chunkX*16+x,y,chunkZ*16+z);throw new IllegalStateException("no deterministic redstone foundation");}
    private static StateDelta delta(RemoteChunkSnapshot before,RemoteChunkSnapshot after)throws Exception{MessageDigest digest=MessageDigest.getInstance("SHA-256");ByteBuffer row=ByteBuffer.allocate(10);int changed=0;
        for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=0;y<128;y++){BlockState a=before.blockAt(x,y,z),b=after.blockAt(x,y,z);if(!a.equals(b)){changed++;row.clear();row.putShort((short)x).putShort((short)y).putShort((short)z).put((byte)a.legacyId()).put((byte)a.metadata()).put((byte)b.legacyId()).put((byte)b.metadata());digest.update(row.array());}}return new StateDelta(changed,hex(digest.digest()));}
    private static boolean water(int id){return id==8||id==9;}private static int local(int value,int chunk){return value-chunk*16;}
    private static void awaitPlayers(B173DedicatedServer server,int count)throws Exception{long end=System.currentTimeMillis()+5000;while(System.currentTimeMillis()<end){if(server.players().size()==count)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}
    private static String sha256(String value)throws Exception{return hex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}private static String hex(byte[]value){StringBuilder result=new StringBuilder();for(byte item:value)result.append(String.format("%02x",item&255));return result.toString();}
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}private static final class StateDelta{final int changed;final String hash;StateDelta(int c,String h){changed=c;hash=h;}@Override public String toString(){return changed+":"+hash;}}
}
