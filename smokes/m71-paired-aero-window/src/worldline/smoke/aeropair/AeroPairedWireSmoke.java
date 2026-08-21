package worldline.smoke.aeropair;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.MovementOutcome;
import worldline.api.PeerSwingSession;
import worldline.api.PlayerPose;
import worldline.api.RemoteArmorSlot;
import worldline.api.RemoteCombatStrike;
import worldline.api.RemoteHeldItem;
import worldline.api.RemoteIncomingHit;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteSwingRequest;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173WireClient;

/** Holds one fresh matched control/event fixture around the common Packet3 anchor. */
public final class AeroPairedWireSmoke {
    private AeroPairedWireSmoke() {}
    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 8) throw new IllegalArgumentException(
                "usage: AeroPairedWireSmoke server.jar workspace port seed attacker victim arm trigger");
        Path jar=Paths.get(arguments[0]),workspace=Paths.get(arguments[1]);int port=Integer.parseInt(arguments[2]);
        long seed=Long.parseLong(arguments[3]);String attackerName=arguments[4],victimName=arguments[5],arm=arguments[6];
        String trigger=arguments[7];require("control".equals(arm)||"event".equals(arm),"invalid arm");
        Duration timeout=Duration.ofSeconds(180);B173DedicatedServer server=new B173DedicatedServer(
                jar,workspace,port,seed,timeout,3,true);PeerSwingSession victim=client(port,victimName,timeout);
        PeerSwingSession attacker=client(port,attackerName,timeout);BufferedReader control=new BufferedReader(
                new InputStreamReader(System.in,StandardCharsets.UTF_8));
        try {server.boot();server.operator(victimName);server.operator(attackerName);victim.connect();victim.synchronizePose();
            require(victim.awaitInventory().occupiedSlots()==0,"victim inventory drifted");victim.look(0F,90F);
            for(RemoteArmorSlot slot:RemoteArmorSlot.values()){acquire(victim,victimName,slot.leatherItemId());
                int source=find(victim.inventory(),new RemoteItemStack(slot.leatherItemId(),1,0));
                require(source>=36,"leather source absent: "+slot);victim.equipLeatherArmor(source,slot);}
            for(int step=0;step<4;step++)victim.moveAndObserve(2.5D,5D,0D,3);
            attacker.connect();attacker.synchronizePose();require(attacker.awaitInventory().occupiedSlots()==0,"attacker inventory drifted");
            attacker.look(0F,90F);acquire(attacker,attackerName,276);int sword=find(attacker.inventory(),new RemoteItemStack(276,1,0));
            require(sword>=36,"sword absent");attacker.selectHeldSlot(sword-36);victim.awaitPeerHeldItem(new RemoteHeldItem(attackerName,276,0));
            for(RemoteArmorSlot slot:RemoteArmorSlot.values())attacker.awaitPeerArmor(
                    new worldline.api.RemoteArmorPiece(victimName,slot,slot.leatherItemId(),0));
            PlayerPose victimAir=raise(victim),aligned=align(attacker,raise(attacker),victimAir);
            require(distance(victimAir,aligned)<6D,"combat alignment drifted");victim.sustainTicks(80);attacker.sustainTicks(2);
            System.out.println("WORLDLINE_M71_WIRE_ARMED=arm="+arm+";attacker="+attacker.state().entityId()
                    +";victim="+victim.state().entityId());System.out.flush();await(control,"GO",victim,attacker);
            attacker.sendChat(trigger);System.out.println("WORLDLINE_M71_WIRE_TRIGGER=arm="+arm);System.out.flush();
            if("event".equals(arm)){RemoteSwingRequest swing=attacker.swingHeldItem();RemoteCombatStrike strike=attacker.attackPlayer(victimName);
                victim.sustainTicks(2);RemoteIncomingHit hit=victim.awaitIncomingHit(18);attacker.sustainTicks(2);
                require(swing.entityId()==attacker.state().entityId()&&strike.targetEntityId()==victim.state().entityId()
                        &&hit.healthBefore()==20&&hit.healthAfter()==18&&attacker.inventory().slot(sword).item().damage()==1,
                        "event evidence drifted");System.out.println("WORLDLINE_M71_WIRE_EVENT=health=20->18;sword=0->1");
            }else{require(attacker.inventory().slot(sword).item().damage()==0,"control weapon drifted");
                System.out.println("WORLDLINE_M71_WIRE_CONTROL=sword=0;combat-request=absent");}System.out.flush();
            await(control,"RELEASE",victim,attacker);attacker.close();victim.close();awaitPlayers(server,0);server.save();
            System.out.println("WORLDLINE_M71_WIRE_COMPLETE=arm="+arm+";shutdown=clean");
        }finally{attacker.close();victim.close();server.close();}
    }
    private static PeerSwingSession client(int port,String name,Duration timeout){return new B173WireClient("127.0.0.1",port,name,timeout);}
    private static void acquire(PeerSwingSession client,String username,int item){int occupied=client.inventory().occupiedSlots()+1;
        for(int step=0;step<10;step++)client.moveAndObserve(0D,5D,0D,3);client.sendChat("/give "+username+" "+item+" 1");
        client.sustainTicks(40);for(int step=0;step<25&&client.inventory().occupiedSlots()<occupied;step++)
            client.moveAndObserve(0D,-5D,0D,3);client.sustainTicks(10);}
    private static int find(RemoteInventoryView view,RemoteItemStack expected){for(int slot=9;slot<=44;slot++)
        if(!view.slot(slot).empty()&&view.slot(slot).item().equals(expected))return slot;return -1;}
    private static PlayerPose raise(PeerSwingSession client){MovementOutcome result=null;
        for(int step=0;step<4;step++)result=client.moveAndObserve(0D,5D,0D,3);return result.resulting();}
    private static PlayerPose align(PeerSwingSession client,PlayerPose start,PlayerPose target){PlayerPose current=start;
        for(int step=0;step<16&&distance(current,target)>3D;step++){double dx=target.x()+2D-current.x(),dy=target.y()-current.y(),
                dz=target.z()-current.z(),scale=Math.max(1D,Math.max(Math.abs(dx),Math.max(Math.abs(dy),Math.abs(dz)))/4D);
            current=client.moveAndObserve(dx/scale,dy/scale,dz/scale,3).resulting();}return current;}
    private static double distance(PlayerPose a,PlayerPose b){double x=a.x()-b.x(),y=a.y()-b.y(),z=a.z()-b.z();return Math.sqrt(x*x+y*y+z*z);}
    private static void await(BufferedReader control,String expected,PeerSwingSession victim,PeerSwingSession attacker)throws Exception{
        long end=System.currentTimeMillis()+180000L;while(System.currentTimeMillis()<end){if(control.ready()){
                require(expected.equals(control.readLine()),expected+" drifted");return;}victim.sustainTicks(2);attacker.sustainTicks(2);}
        throw new IllegalStateException(expected+" absent");}
    private static void awaitPlayers(B173DedicatedServer server,int count)throws Exception{long end=System.currentTimeMillis()+5000L;
        while(System.currentTimeMillis()<end){if(server.players().size()==count)return;Thread.sleep(100L);}throw new IllegalStateException("player count drifted");}
    private static void require(boolean condition,String message){if(!condition)throw new IllegalStateException(message);}
}
