package worldline.m74.client;

import aero.modellib.Aero_BECellRenderer;
import net.minecraft.client.Minecraft;
import net.modificationstation.stationapi.api.network.packet.MessagePacket;
import worldline.m74.*;
import worldline.m94.*;

/** Binds default-TTL eviction between depletion and reverse recovery. */
public final class WorldlineRecoveryEvent {
    private static final int AFTER=Integer.getInteger("worldline.recovery.after",300),GAP=Integer.getInteger("worldline.recovery.restoreAfter",30);
    static final int[] requests=fill(),events=fill(),indices={1,2,3,5,6,7,7,6,5,3,2,1},operations={1,1,1,1,1,1,2,2,2,2,2,2},members={15,14,13,12,11,10,11,12,13,14,15,16};
    static int expiry=-1,expiredBefore=-1,expiredAfter=-1;private static final boolean[]air=new boolean[8],restored=new boolean[8];private static int step;private WorldlineRecoveryEvent(){}private static int[]fill(){int[]v=new int[12];java.util.Arrays.fill(v,-1);return v;}
    public static void head(Minecraft client){if(!WorldlinePagedBridge.running()||WorldlinePagedBridge.sealed())return;int x=WorldlineCensusSync.x(),y=WorldlineCensusSync.y(),z=WorldlineCensusSync.z(),root=WorldlineCensusProbe.nonce();WorldlineRecoveryState.apply(client.world);if(step==0&&expiredBefore<0)expiredBefore=Aero_BECellRenderer.expiredCachedPages();if(events[5]>=0&&expiry<0&&Aero_BECellRenderer.cachedPageCount()==3&&Aero_BECellRenderer.expiredCachedPages()==expiredBefore+1&&Aero_BECellRenderer.evictedCachedPages()==0){expiry=WorldlinePagedBridge.count()-1;expiredAfter=Aero_BECellRenderer.expiredCachedPages();}
        if(step<12&&readyToSend()){if(AFTER!=300||GAP!=30||!"8".equals(System.getProperty("aero.becell.rebuildsPerFrame"))||System.getProperty("aero.becell.pageTtlFrames")!=null||System.getProperty("aero.perf.memory")!=null)throw new IllegalStateException("M94 runtime drift");int ordinal=step+1,index=indices[step],operation=operations[step],dy=index&3,dz=index>>2;MessagePacket packet=new MessagePacket(WorldlineRecoveryMod.CHANGE);packet.ints=new int[]{x,y+dy,z+dz,root,ordinal,operation,index};client.getNetworkHandler().sendPacket(packet);requests[step]=WorldlinePagedBridge.count();step++;}
        if(step>0){int at=step-1,index=indices[at],operation=operations[at],tx=x,ty=y+(index&3),tz=z+(index>>2);if(operation==1)air[index]|=client.world.getBlockId(tx,ty,tz)==0;else{WorldlineRecoveryState.apply(client.world);restored[index]=client.world.getBlockId(tx,ty,tz)==WorldlineCensusMod.block.id&&client.world.getBlockEntity(tx,ty,tz)instanceof WorldlineCensusBlockEntity be&&be.nonce()==root*100+index+1;}}}
    private static boolean readyToSend(){if(step==0)return WorldlinePagedBridge.count()>=AFTER;if(step==6)return expiry>=0&&WorldlinePagedBridge.count()>=expiry+1+GAP;return events[step-1]>=0&&WorldlinePagedBridge.count()>=events[step-1]+GAP;}
    public static void tail(){if(step==0)return;int at=step-1;if(events[at]>=0)return;int ordinal=at+1,index=indices[at],operation=operations[at],x=WorldlineCensusSync.x(),y=WorldlineCensusSync.y()+(index&3),z=WorldlineCensusSync.z()+(index>>2),root=WorldlineCensusProbe.nonce(),queued=Aero_BECellRenderer.queuedLastFrame();boolean accepted=operation==1?WorldlineRecoveryState.removed(ordinal,x,y,z,root)&&air[index]&&queued==members[at]:WorldlineRecoveryState.restored(ordinal,x,y,z,root)&&restored[index]&&queued==members[at];if(accepted)events[at]=WorldlinePagedBridge.count();}
    static boolean valid(){for(int i=0;i<12;i++)if(requests[i]<0||events[i]<requests[i]||i>0&&i!=6&&requests[i]<events[i-1]+GAP)return false;if(expiry<events[5]||requests[6]<expiry+1+GAP||expiredBefore!=0||expiredAfter!=1)return false;for(int i:new int[]{1,2,3,5,6,7})if(!air[i]||!restored[i])return false;return true;}
    static String diagnostic(){return "requests="+join(requests)+" events="+join(events)+" expiry="+expiry+" expired="+expiredBefore+"->"+expiredAfter+" depleted="+validCells(air)+" restored="+validCells(restored);}
    private static boolean validCells(boolean[]v){for(int i:new int[]{1,2,3,5,6,7})if(!v[i])return false;return true;}private static String join(int[]v){StringBuilder s=new StringBuilder();for(int i=0;i<v.length;i++){if(i>0)s.append('/');s.append(v[i]);}return s.toString();}
}
