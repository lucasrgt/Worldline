package worldline.m74.client;

import aero.modellib.Aero_BECellRenderer;import net.minecraft.client.Minecraft;import net.modificationstation.stationapi.api.network.packet.MessagePacket;import worldline.m74.*;import worldline.m80.*;

/** Requests one remote removal and binds the first 15-member rebuild record. */
public final class WorldlineMembershipEvent {
    private static final int AFTER=Integer.getInteger("worldline.membership.after",300);static boolean requested,acked,blockAir,beAbsent;static int requestIndex=-1,eventIndex=-1,lastBlock=-1,eventPages=-1,eventRebuilds=-1,eventDirect=-1,eventCache=-1;private WorldlineMembershipEvent(){}
    public static void head(Minecraft client){if(!WorldlinePagedBridge.running()||WorldlinePagedBridge.sealed())return;int x=WorldlineCensusSync.x(),y=WorldlineCensusSync.y(),z=WorldlineCensusSync.z(),root=WorldlineCensusProbe.nonce();
        if(!requested&&WorldlinePagedBridge.count()>=AFTER){if(AFTER!=300||!"8".equals(System.getProperty("aero.becell.rebuildsPerFrame")))throw new IllegalStateException("M80 runtime drift");MessagePacket p=new MessagePacket(WorldlineMembershipMod.CHANGE);p.ints=new int[]{x,y,z,root};client.getNetworkHandler().sendPacket(p);requestIndex=WorldlinePagedBridge.count();requested=true;}
        if(requested){acked=WorldlineMembershipState.matches(x,y,z,root);lastBlock=client.world.getBlockId(x,y,z);blockAir=lastBlock==0;beAbsent=client.world.getBlockEntity(x,y,z)==null;}}
    public static void tail(){if(!requested||!acked||eventIndex>=0||Aero_BECellRenderer.queuedLastFrame()!=15)return;eventIndex=WorldlinePagedBridge.count();eventPages=Aero_BECellRenderer.pageCallsThisFrame();eventRebuilds=Aero_BECellRenderer.pageRebuildsThisFrame();eventDirect=Aero_BECellRenderer.directFallbacksThisFrame();eventCache=Aero_BECellRenderer.cachedPageCount();}
    static boolean valid(){return eventIndex>=requestIndex&&blockAir&&eventPages==2&&eventRebuilds==1&&eventDirect==0&&eventCache==2;}
    static String diagnostic(){return "requested="+requested+",acked="+acked+",block="+lastBlock+",beAbsent="+beAbsent+",request="+requestIndex+",event="+eventIndex+",pages="+eventPages+",rebuilds="+eventRebuilds+",direct="+eventDirect+",cache="+eventCache;}
}
