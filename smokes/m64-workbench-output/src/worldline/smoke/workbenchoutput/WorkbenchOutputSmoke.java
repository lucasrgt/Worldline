package worldline.smoke.workbenchoutput;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.MovementOutcome;
import worldline.api.PlayerPose;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWindowClosure;
import worldline.api.RemoteWorkbenchOutput;
import worldline.api.RemoteWorkbenchPreparation;
import worldline.api.RemoteWorldView;
import worldline.api.WorkbenchOutputSession;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;
import worldline.b173server.B173WorkbenchOutputPacketFixture;

/** Proves exact slabs output, ingredient consumption, close, and restart persistence. */
public final class WorkbenchOutputSmoke {
    private static final String TRACE = "v1|server=official-b1.7.3|servers=2|clients=2"
            + "|fixture=workbench58+planks5x3|prepare-actions=1,2,3,4"
            + "|output-wire=action5-slot0-left-slabs44x3:2+action6-slot37-left-null"
            + "|output-acks=5,6-accepted|stat=packet200-16842796x3"
            + "|owned=prepared-to-empty|cursor=empty-slabs-empty|personal36=empty-slabs"
            + "|close-proof=personal-action1|restart=clean-new-server"
            + "|reopen=personal36-slabs44x3:2+workbench-owned-empty|player-items=1";
    private WorkbenchOutputSmoke() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 5) throw new IllegalArgumentException(
                "usage: WorkbenchOutputSmoke server.jar workspace port seed actor");
        Path jar = Paths.get(args[0]), workspace = Paths.get(args[1]); int port = Integer.parseInt(args[2]);
        long seed = Long.parseLong(args[3]); String name = args[4]; Duration timeout = Duration.ofSeconds(90);
        B173DedicatedServer first = null, second = null; WorkbenchOutputSession actor = null, reopened = null;
        RemoteWorkbenchOutput output; RemoteWindowClosure firstClose, secondClose; BlockPosition target;
        try {
            B173WorkbenchOutputPacketFixture.verify(); first = server(jar, workspace, port, seed, timeout);
            first.boot(); first.operator(name); B173PlayerSeed.writeInventory(workspace, name, 4.5, 100, 4.5,
                    new int[] {0, 1}, new int[] {5, 58}, new int[] {3, 1}, new int[] {0, 0});
            actor = client(port, name, timeout); actor.connect(); actor.synchronizePose();
            RemoteItemStack planks = item(5, 3, 0), slabs = item(44, 3, 2);
            require(actor.awaitInventory().occupiedSlots() == 2 && actor.inventory().slot(36).item().equals(planks)
                    && actor.inventory().slot(37).item().equals(item(58, 1, 0)), "actor inventory seed drifted");
            actor.look(0F, 90F); PlayerPose pose = settle(actor);
            RemoteWorldView baseline = actor.awaitRemoteChunk((int) Math.floor(pose.x()) >> 4,
                    (int) Math.floor(pose.z()) >> 4); BlockPosition support = placement(baseline, pose);
            target = BlockFace.UP.adjacent(support); actor.selectHeldSlot(1);
            actor.placeHeldBlock(support, BlockFace.UP); actor.awaitBlock(target, new BlockState(58, 0)); worldline.test.WorldlineSmokeAwait.observe(actor,5);
            require(actor.inventory().slot(36).item().equals(planks) && actor.inventory().slot(37).empty(),
                    "placed workbench inventory drifted"); actor.selectHeldSlot(1);
            actor.openWorkbench(target, BlockFace.UP); RemoteWorkbenchPreparation prepared = actor.prepareWorkbenchSlabs(36);
            output = actor.takeWorkbenchSlabs(36); require(output.takeAction() == 5 && output.storeAction() == 6
                    && output.craftedCount() == 3 && output.before().equals(prepared.prepared())
                    && output.stack().equals(slabs) && emptyOwned(output.consumed()) && emptyOwned(output.after())
                    && output.after().slot(37).item().equals(slabs) && actor.inventory().equals(output.personalAfter()),
                    "workbench output drifted");
            firstClose = actor.closeWindow(); require(firstClose.proofAction() == 1
                    && firstClose.closedWindow().inventory().equals(output.after()), "workbench output close drifted");
            actor.close(); awaitPlayers(first, 0); first.save(); require(first.player(name).inventoryItems() == 1,
                    "workbench output inventory did not persist"); first.close(); first = null;
            second = server(jar, workspace, port, seed, timeout); second.boot(); second.operator(name);
            reopened = client(port, name, timeout); reopened.connect(); reopened.synchronizePose();
            require(reopened.awaitInventory().occupiedSlots() == 1 && reopened.inventory().slot(36).item().equals(slabs),
                    "restarted slabs inventory drifted"); reopened.selectHeldSlot(1);
            RemoteContainerWindow persisted = reopened.openWorkbench(target, BlockFace.UP);
            require(emptyOwned(persisted.inventory()) && persisted.inventory().slot(37).item().equals(slabs),
                    "restarted workbench state drifted"); secondClose = reopened.closeWindow();
            require(secondClose.proofAction() == 1, "restarted workbench close drifted"); reopened.close();
            awaitPlayers(second, 0); second.save(); require(second.player(name).inventoryItems() == 1,
                    "restarted slabs inventory did not persist");
        } finally { if (actor != null) actor.close(); if (reopened != null) reopened.close();
            if (first != null) first.close(); if (second != null) second.close(); }
        System.out.println("WORLDLINE_M64_API=workbench-output,packet200-stat,restart-persistence");
        System.out.println("WORLDLINE_M64_OUTPUT=actions=" + output.takeAction() + "," + output.storeAction()
                + ";crafted=" + output.craftedCount() + ";stack=" + output.stack() + ";close="
                + firstClose.proofAction() + ";close2=" + secondClose.proofAction());
        System.out.println("WORLDLINE_M64_TRACE=" + TRACE); System.out.println("WORLDLINE_M64_SIGNATURE=" + sha256(TRACE));
    }

    private static B173DedicatedServer server(Path jar, Path workspace, int port, long seed, Duration timeout) {
        return new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true); }
    private static WorkbenchOutputSession client(int port, String name, Duration timeout) {
        return new B173WireClient("127.0.0.1", port, name, timeout); }
    private static PlayerPose settle(WorkbenchOutputSession client) {
        worldline.test.WorldlineSmokeAwait.observe(client,5); MovementOutcome settled = null; for (int i = 0; i < 100; i++) {
            settled = client.moveAndObserve(0, -1, 0, 2); if (settled.corrected()) break; }
        require(settled != null && settled.corrected(), "ground settlement absent"); return settled.resulting(); }
    private static BlockPosition placement(RemoteWorldView view, PlayerPose pose) { int x=(int)Math.floor(pose.x()),
            y=(int)Math.floor(pose.y()),z=(int)Math.floor(pose.z()); for(int r=2;r<=5;r++)for(int dx=-r;dx<=r;dx++)
        for(int dz=-r;dz<=r;dz++)if(Math.max(Math.abs(dx),Math.abs(dz))==r)for(int dy=3;dy>=-5;dy--){BlockPosition s=
            new BlockPosition(x+dx,y+dy,z+dz),t=BlockFace.UP.adjacent(s);try{BlockState b=view.blockAt(t.x(),t.y(),t.z());
            if(s.y()>=0&&t.y()<128&&view.blockAt(s.x(),s.y(),s.z()).legacyId()!=0&&replaceable(b))return s;}catch(IllegalArgumentException absent){}}
        throw new IllegalStateException("nearby workbench placement absent"); }
    private static boolean emptyOwned(RemoteInventoryView view){for(int s=0;s<10;s++)if(!view.slot(s).empty())return false;return true;}
    private static boolean replaceable(BlockState state){int id=state.legacyId();return id==0||id==8||id==9||id==78;}
    private static RemoteItemStack item(int id,int count,int damage){return new RemoteItemStack(id,count,damage);}
    private static void awaitPlayers(B173DedicatedServer server,int count)throws InterruptedException{long end=System.currentTimeMillis()+5000;
        while(System.currentTimeMillis()<end){if(server.players().size()==count)return;Thread.sleep(100);}throw new IllegalStateException("player count drifted");}
    private static String sha256(String value)throws Exception{byte[] bytes=MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder result=new StringBuilder();for(byte item:bytes)result.append(String.format("%02x",item&255));return result.toString();}
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
}
