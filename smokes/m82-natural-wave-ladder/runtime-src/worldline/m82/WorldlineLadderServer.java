package worldline.m82;

import net.minecraft.entity.player.*;import net.modificationstation.stationapi.api.network.packet.MessagePacket;import worldline.m74.*;

/** Validates and applies one 1/2/4-member server-authored removal wave. */
public final class WorldlineLadderServer {
    private static boolean done;private WorldlineLadderServer(){}
    private static int[]indices(int n){if(n==1)return new int[]{0};if(n==2)return new int[]{0,4};if(n==4)return new int[]{0,1,4,5};throw new IllegalStateException("invalid M82 cardinality");}
    public static synchronized void remove(PlayerEntity player,int[]v){int root=Integer.getInteger("worldline.census.nonce",0),expected=Integer.getInteger("worldline.ladder.targets",0);if(!(player instanceof ServerPlayerEntity server)||done||v==null||v.length!=5||v[3]!=root||v[4]!=expected||root<=0)throw new IllegalStateException("invalid M82 request");int x=v[0],y=v[1],z=v[2],n=v[4];int[]ids=indices(n);for(int i:ids){int cy=y+i%4,cz=z+i/4;if(player.world.getBlockId(x,cy,cz)!=WorldlineCensusMod.block.id||!(player.world.getBlockEntity(x,cy,cz)instanceof WorldlineCensusBlockEntity be)||be.nonce()!=root*100+i+1)throw new IllegalStateException("M82 target drift");}for(int i:ids)if(!player.world.setBlock(x,y+i%4,z+i/4,0))throw new IllegalStateException("M82 removal rejected");for(int i:ids)if(player.world.getBlockId(x,y+i%4,z+i/4)!=0)throw new IllegalStateException("M82 removal absent");done=true;MessagePacket ack=new MessagePacket(WorldlineLadderMod.CHANGE);ack.ints=v.clone();server.networkHandler.sendPacket(ack);System.out.println("[WorldlineLadder] removed targets="+n+" x="+x+" y="+y+" z="+z+" nonce="+root);}
}
