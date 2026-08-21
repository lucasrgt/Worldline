package worldline.m74.client;

import aero.modellib.Aero_BECellRenderer;import net.minecraft.client.Minecraft;import net.modificationstation.stationapi.api.network.packet.MessagePacket;import worldline.m74.*;import worldline.m81.*;

/** Requests two remote removals and binds the first 14-member rebuild record. */
public final class WorldlineWaveEvent {
    private static final int AFTER=Integer.getInteger("worldline.wave.after",300);static boolean requested,acked,firstAir,secondAir;static int requestIndex=-1,eventIndex=-1,firstBlock=-1,secondBlock=-1,eventPages=-1,eventRebuilds=-1,eventDirect=-1,eventCache=-1;private WorldlineWaveEvent(){}
    public static void head(Minecraft client){if(!WorldlinePagedBridge.running()||WorldlinePagedBridge.sealed())return;int x=WorldlineCensusSync.x(),y=WorldlineCensusSync.y(),z=WorldlineCensusSync.z(),root=WorldlineCensusProbe.nonce();
        if(!requested&&WorldlinePagedBridge.count()>=AFTER){if(AFTER!=300||!"8".equals(System.getProperty("aero.becell.rebuildsPerFrame")))throw new IllegalStateException("M81 runtime drift");MessagePacket p=new MessagePacket(WorldlineWaveMod.CHANGE);p.ints=new int[]{x,y,z,root};client.getNetworkHandler().sendPacket(p);requestIndex=WorldlinePagedBridge.count();requested=true;}
        if(requested){acked=WorldlineWaveState.matches(x,y,z,root);firstBlock=client.world.getBlockId(x,y,z);secondBlock=client.world.getBlockId(x,y,z+2);firstAir=firstBlock==0;secondAir=secondBlock==0;}}
    public static void tail(){if(!requested||!acked||eventIndex>=0||Aero_BECellRenderer.queuedLastFrame()!=14)return;eventIndex=WorldlinePagedBridge.count();eventPages=Aero_BECellRenderer.pageCallsThisFrame();eventRebuilds=Aero_BECellRenderer.pageRebuildsThisFrame();eventDirect=Aero_BECellRenderer.directFallbacksThisFrame();eventCache=Aero_BECellRenderer.cachedPageCount();}
    static boolean valid(){return eventIndex>=requestIndex&&firstAir&&secondAir&&eventPages==2&&eventRebuilds==2&&eventDirect==0&&eventCache==2;}
    static String diagnostic(){return "requested="+requested+",acked="+acked+",blocks="+firstBlock+"/"+secondBlock+",request="+requestIndex+",event="+eventIndex+",pages="+eventPages+",rebuilds="+eventRebuilds+",direct="+eventDirect+",cache="+eventCache;}
}
