package worldline.smoke.causalwaterb173;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.MovementOutcome;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Opens one generated-water floor cell and observes the official settled flow. */
public final class CausalWaterFlowSmoke {
    private CausalWaterFlowSmoke() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 8) throw new IllegalArgumentException(
                "usage: CausalWaterFlowSmoke server.jar workspace port seed username chunkX chunkZ settleTicks");
        Path jar=Paths.get(arguments[0]),workspace=Paths.get(arguments[1]);int port=Integer.parseInt(arguments[2]);
        long seed=Long.parseLong(arguments[3]);String username=arguments[4];int chunkX=Integer.parseInt(arguments[5]);
        int chunkZ=Integer.parseInt(arguments[6]),settleTicks=Integer.parseInt(arguments[7]);Duration timeout=Duration.ofSeconds(90);
        B173DedicatedServer server=new B173DedicatedServer(jar,workspace,port,seed,timeout,3,true);
        B173WireClient actor=new B173WireClient("127.0.0.1",port,username,timeout),reader=null;
        RemoteChunkSnapshot before,after;BlockPosition source,target;BlockState opened,settled;
        try{server.boot();B173PlayerSeed.write(workspace,username,4.5D,58D,4.5D);actor.connect();
            PlayerPose pose=actor.synchronizePose();before=actor.awaitRemoteChunk(chunkX,chunkZ).chunkAt(chunkX,chunkZ);
            source=source(before,chunkX,chunkZ);target=new BlockPosition(source.x(),source.y()-1,source.z());
            pose=actor.moveAndObserve(target.x()+0.5D-pose.x(),0D,target.z()+0.5D-pose.z(),3).resulting();
            while(pose.y()>target.y()+4D){MovementOutcome move=actor.moveAndObserve(0D,-1D,0D,1);pose=move.resulting();
                require(!move.corrected()||pose.y()<=target.y()+5D,"descent corrected above dig range");}
            Thread.sleep(1000L);actor.beginBreak(target);Thread.sleep(3000L);actor.finishBreak(target);
            opened=actor.awaitBlock(target,new BlockState(0,0)).blockAt(target.x(),target.y(),target.z());
            RemoteWorldView live=worldline.test.WorldlineSmokeAwait.observe(actor,settleTicks);settled=live.blockAt(target.x(),target.y(),target.z());
            require(water(settled.legacyId()),"generated water did not enter opened cell: "+settled);
            actor.close();awaitPlayers(server,0);server.save();reader=new B173WireClient("127.0.0.1",port,username,timeout);
            reader.connect();reader.synchronizePose();after=reader.awaitRemoteChunk(chunkX,chunkZ).chunkAt(chunkX,chunkZ);
            require(after.blockAt(local(target.x(),chunkX),target.y(),local(target.z(),chunkZ)).equals(settled),
                    "fresh Packet51 did not preserve settled water");
        }finally{actor.close();if(reader!=null)reader.close();server.close();}
        StateDelta delta=delta(before,after);int lx=local(target.x(),chunkX),lz=local(target.z(),chunkZ);
        BlockState prior=before.blockAt(lx,target.y(),lz),sourceState=before.blockAt(lx,source.y(),lz);
        require(sourceState.legacyId()==9&&prior.legacyId()==3&&opened.legacyId()==0&&water(settled.legacyId())
                &&delta.changed>0,"causal water fixture drift");
        String evidence="source="+source.x()+":"+source.y()+":"+source.z()+":"+sourceState.legacyId()+":"+sourceState.metadata()
                +",opened="+target.x()+":"+target.y()+":"+target.z()+":"+prior.legacyId()+":"+prior.metadata()
                +"->0:0->"+settled.legacyId()+":"+settled.metadata()+",states="+delta;
        String trace="v1|server=official-b1.7.3|seed="+seed+"|chunk="+chunkX+","+chunkZ
                +"|cause=packet14-break-below-generated-water|confirmation=packet53-air|settle="+settleTicks
                +"ticks|observation=live-packet53+fresh-login-packet51|"+evidence+"|disconnect=clean";
        System.out.println("WORLDLINE_M114_WATER="+evidence);System.out.println("WORLDLINE_M114_TRACE="+trace);
        System.out.println("WORLDLINE_M114_SIGNATURE="+sha256(trace));
    }

    private static BlockPosition source(RemoteChunkSnapshot chunk,int chunkX,int chunkZ){
        for(int x=4;x<=11;x++)for(int z=4;z<=11;z++)for(int y=126;y>=2;y--){int id=chunk.blockAt(x,y,z).legacyId();
            int below=chunk.blockAt(x,y-1,z).legacyId(),floor=chunk.blockAt(x,y-2,z).legacyId();
            if(id==9&&below==3&&floor==3)return new BlockPosition(chunkX*16+x,y,chunkZ*16+z);}
        throw new IllegalStateException("no deterministic generated-water floor");}
    private static StateDelta delta(RemoteChunkSnapshot before,RemoteChunkSnapshot after)throws Exception{
        MessageDigest digest=MessageDigest.getInstance("SHA-256");ByteBuffer row=ByteBuffer.allocate(10);int changed=0,water=0,air=0;
        for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=0;y<128;y++){BlockState a=before.blockAt(x,y,z),b=after.blockAt(x,y,z);
            if(!a.equals(b)){changed++;if(water(b.legacyId())&&!water(a.legacyId()))water++;if(b.legacyId()==0)air++;
                row.clear();row.putShort((short)x).putShort((short)y).putShort((short)z).put((byte)a.legacyId())
                        .put((byte)a.metadata()).put((byte)b.legacyId()).put((byte)b.metadata());digest.update(row.array());}}
        return new StateDelta(changed,water,air,hex(digest.digest()));}
    private static boolean water(int id){return id==8||id==9;}private static int local(int value,int chunk){return value-chunk*16;}
    private static void awaitPlayers(B173DedicatedServer server,int count)throws Exception{long end=System.currentTimeMillis()+5000;
        while(System.currentTimeMillis()<end){if(server.players().size()==count)return;Thread.sleep(100);}throw new IllegalStateException("player count drift");}
    private static String sha256(String value)throws Exception{return hex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}
    private static String hex(byte[]value){StringBuilder result=new StringBuilder();for(byte item:value)result.append(String.format("%02x",item&255));return result.toString();}
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    private static final class StateDelta{final int changed,water,air;final String hash;StateDelta(int c,int w,int a,String h){changed=c;water=w;air=a;hash=h;}
        @Override public String toString(){return changed+":"+water+":"+air+":"+hash;}}
}
