package worldline.b173server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkObservation;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.MovementDisposition;
import worldline.api.MovementOutcome;

/** Original bounded codec for the protocol-14 initial play-position exchange. */
final class B173PlayChannel {
    private final DataInputStream input;
    private final DataOutputStream output;
    private final B173PlayInbound inbound;
    private final B173HeldItemChannel held; private final B173PersonalWindowChannel personal; private final B173ContainerWindowChannel container; private final B173WorkbenchChannel workbench; private final B173CombatChannel combat;
    private PlayerPose pose; private double stanceHeight;

    B173PlayChannel(DataInputStream input, DataOutputStream output, int timeoutMillis,
            int localEntityId, String localUsername, int dimension) throws IOException {
        this.input = input; this.output = output;
        this.inbound = new B173PlayInbound(input, output, timeoutMillis, localEntityId, localUsername, dimension); this.held = new B173HeldItemChannel(output, inbound); this.personal = new B173PersonalWindowChannel(output, inbound); this.container = new B173ContainerWindowChannel(output, inbound); this.workbench = new B173WorkbenchChannel(output, inbound); this.combat = new B173CombatChannel(output, inbound, localEntityId, localUsername);
    }
    B173PlayChannel(DataInputStream input, DataOutputStream output, int timeoutMillis) throws IOException {
        this(input, output, timeoutMillis, 0, "Worldline", 0); }

    PlayerPose synchronize() throws IOException {
        require(pose == null, "play channel was already synchronized");
        StringBuilder packets = new StringBuilder();
        for (int count = 0; count < 8192; count++) {
            int packet = input.readUnsignedByte();
            if (packets.length() > 0) packets.append(',');
            packets.append(packet);
            if (packet == 13) {
                double x = input.readDouble();
                double clientY = input.readDouble();
                double feetY = input.readDouble();
                double z = input.readDouble();
                float yaw = input.readFloat(), pitch = input.readFloat();
                input.readBoolean();
                require(clientY > feetY && clientY - feetY < 2.0D,
                        "server position stance drift");
                stanceHeight = clientY - feetY;
                pose = new PlayerPose(x, feetY, z, yaw, pitch);
                acknowledge(x, feetY, clientY, z, yaw, pitch);
                return pose;
            }
            try { inbound.skip(packet); }
            catch (IOException error) { throw new IOException(
                    "play prelude failed after packets " + packets, error); }
        }
        throw new IOException("initial play position packet absent");
    }

    void look(float yaw, float pitch) throws IOException {
        require(pose != null, "play channel is not synchronized");
        new PlayerPose(pose.x(), pose.y(), pose.z(), yaw, pitch);
        output.writeByte(12);
        output.writeFloat(yaw); output.writeFloat(pitch); output.writeBoolean(false);
        output.flush();
        pose = new PlayerPose(pose.x(), pose.y(), pose.z(), yaw, pitch);
    }

    PlayerPose moveBy(double deltaX, double deltaY, double deltaZ) throws IOException {
        require(pose != null, "play channel is not synchronized");
        inbound.enableImplicitChunks();
        PlayerPose target = new PlayerPose(pose.x() + deltaX, pose.y() + deltaY,
                pose.z() + deltaZ, pose.yaw(), pose.pitch());
        output.writeByte(13);
        output.writeDouble(target.x()); output.writeDouble(target.y());
        output.writeDouble(target.y() + stanceHeight); output.writeDouble(target.z());
        output.writeFloat(target.yaw()); output.writeFloat(target.pitch());
        output.writeBoolean(true); output.flush();
        pose = target; return target;
    }

    void sendChat(String message) throws IOException {
        require(pose != null, "play channel is not synchronized");
        if (message == null || message.trim().isEmpty() || message.length() > 100)
            throw new IllegalArgumentException("invalid chat message");
        output.writeByte(3); B173InboundPacket.string(output, message); output.flush();
    }

    String awaitChat() throws IOException {
        require(pose != null, "play channel is not synchronized");
        return inbound.awaitChat();
    }

    RemoteChunkObservation awaitChunk() throws IOException {
        require(pose != null, "play channel is not synchronized");
        return inbound.awaitChunk().observation();
    }

    RemoteChunkSnapshot awaitChunkSnapshot() throws IOException {
        require(pose != null, "play channel is not synchronized");
        return inbound.awaitChunk();
    }

    RemoteWorldView awaitRemoteWorld(int minimumChunks) throws IOException {
        require(pose != null, "play channel is not synchronized");
        return inbound.awaitWorld(minimumChunks);
    }

    RemoteWorldView awaitRemoteChunk(int chunkX, int chunkZ) throws IOException {
        require(pose != null, "play channel is not synchronized"); return inbound.awaitChunk(chunkX, chunkZ);
    }

    void beginBreak(BlockPosition position) throws IOException { dig(position, 0); }
    void finishBreak(BlockPosition position) throws IOException { dig(position, 2); }

    RemoteWorldView awaitBlock(BlockPosition position, BlockState expected) throws IOException {
        require(pose != null, "play channel is not synchronized");
        return inbound.awaitBlock(position, expected);
    }

    RemoteWorldView sustainTicks(int ticks) throws IOException, InterruptedException {
        sustain(ticks); return inbound.snapshot(); }

    B173PlayInbound inbound() { require(pose != null, "play channel is not synchronized"); return inbound; } int dimension() { return inbound.dimension(); } int awaitDimension(int expected) throws IOException { return inbound.awaitDimension(expected); } int health(){return inbound.health();} int awaitHealth(int expected)throws IOException{return inbound.awaitHealth(expected);} worldline.api.RemoteRespawn respawn()throws IOException{require(pose!=null&&!inbound.windowActive()&&inbound.cursorObserved()&&inbound.cursor()==null&&inbound.health()<=0,"respawn requires synchronized dead personal state");long epoch=inbound.respawnEpoch();int before=inbound.dimension();B173RespawnPacket.write(output,before);output.flush();int after=inbound.awaitRespawn(epoch,0),health=inbound.awaitHealth(20);return new worldline.api.RemoteRespawn(before,after,0,health);} worldline.api.RemoteExplosion awaitExplosion()throws IOException{return inbound.awaitExplosion();}

    void selectHeldSlot(int slot) throws IOException { require(pose != null, "play channel is not synchronized");
        held.select(slot); }
    void dropHeldItem() throws IOException { require(pose != null, "play channel is not synchronized"); held.drop(); }
    void placeHeldBlock(worldline.api.BlockPosition support, worldline.api.BlockFace face) throws IOException { require(pose != null, "play channel is not synchronized"); held.place(support, face); } void useHeldItemOnBlock(BlockPosition support, worldline.api.BlockFace face) throws IOException { require(pose != null && !inbound.windowActive() && inbound.cursorObserved() && inbound.cursor() == null, "held-item use requires synchronized personal window and empty cursor"); held.useHeldItem(support, face); } void activateBlock(BlockPosition position, worldline.api.BlockFace face) throws IOException { require(pose != null && !inbound.windowActive() && inbound.cursorObserved() && inbound.cursor() == null, "activation requires synchronized personal window and empty cursor"); held.use(position, face); }
    worldline.api.RemoteContainerWindow openChest(BlockPosition position, worldline.api.BlockFace face) throws IOException { require(pose != null, "play channel is not synchronized"); require(!inbound.windowActive() && inbound.cursorObserved() && inbound.cursor() == null, "chest open requires no active window and an observed empty cursor"); held.use(position, face); inbound.beginChest(); return inbound.awaitChest(); } worldline.api.RemoteContainerWindow openFurnace(BlockPosition position, worldline.api.BlockFace face) throws IOException { require(pose != null, "play channel is not synchronized"); require(!inbound.windowActive() && inbound.cursorObserved() && inbound.cursor() == null, "furnace open requires no active window and an observed empty cursor"); held.use(position, face); inbound.beginFurnace(); return inbound.awaitChest(); } worldline.api.RemoteContainerWindow openWorkbench(BlockPosition position, worldline.api.BlockFace face) throws IOException { require(pose != null, "play channel is not synchronized"); require(!inbound.windowActive() && inbound.cursorObserved() && inbound.cursor() == null, "workbench open requires no active window and an observed empty cursor"); held.use(position, face); inbound.beginWorkbench(); return inbound.awaitChest(); } worldline.api.RemoteWorkbenchPreparation prepareWorkbenchSlabs(int personalSlot) throws IOException { return workbench.prepareSlabs(personalSlot); } worldline.api.RemoteWorkbenchOutput takeWorkbenchSlabs(int personalSlot) throws IOException { return workbench.takeSlabs(personalSlot); } worldline.api.RemoteChestTransfer storeInOpenChest(int personalSlot, int chestSlot) throws IOException { return container.store(personalSlot, chestSlot); } worldline.api.RemoteChestRetrieval retrieveFromOpenChest(int chestSlot, int personalSlot) throws IOException { return container.retrieve(chestSlot, personalSlot); } worldline.api.RemoteFurnaceLoad loadFurnace(int inputSlot, int fuelSlot) throws IOException { return container.loadFurnace(inputSlot, fuelSlot); } worldline.api.RemoteFurnaceSmelt awaitFurnaceSmelt() throws IOException { return inbound.awaitFurnaceSmelt(); } worldline.api.RemoteFurnaceExtraction takeFurnaceOutput(int personalSlot) throws IOException { return container.takeFurnaceOutput(personalSlot); } worldline.api.RemoteWindowClosure closeWindow() throws IOException { worldline.api.RemoteContainerWindow window = inbound.activeWindow(); int slot = personal.personalProofSlot(); int id = held.closeWindow(); B173PersonalStep proof = personal.provePersonalWindow(slot); inbound.closeWindow(id); return new worldline.api.RemoteWindowClosure(window, proof.action, proof.slot, proof.before, proof.after); } worldline.api.RemotePersonalTransaction clickPersonalSlot(int slot) throws IOException { require(pose != null, "play channel is not synchronized"); return personal.click(slot); } worldline.api.RemotePersonalCraft craftPersonal2x2(int slot) throws IOException { require(pose != null, "play channel is not synchronized"); return personal.craft2x2(slot); } worldline.api.RemotePersonalTransaction rejectedTakeProbe(int slot) throws IOException { require(pose != null, "play channel is not synchronized"); return personal.rejectedTakeProbe(slot); } worldline.api.RemoteArmorEquip equipLeatherArmor(int slot, worldline.api.RemoteArmorSlot armor) throws IOException { require(pose != null, "play channel is not synchronized"); return personal.equipLeather(slot, armor); } worldline.api.RemoteCombatStrike attackPlayer(String target) throws IOException { require(pose != null && !inbound.windowActive() && inbound.cursorObserved() && inbound.cursor() == null, "combat requires synchronized play with the personal window and empty cursor"); require(held.selectedEquals(new worldline.api.RemoteItemStack(276, 1, 0)), "combat requires selected undamaged diamond sword"); return combat.attack(target); } worldline.api.RemoteIncomingHit awaitIncomingHit(int health) throws IOException { return inbound.awaitIncomingHit(health); } worldline.api.RemoteSwingRequest swingHeldItem() throws IOException { require(pose != null && !inbound.windowActive() && inbound.cursorObserved() && inbound.cursor() == null && held.selectedEquals(new worldline.api.RemoteItemStack(276, 1, 0)), "swing requires synchronized selected sword"); return combat.swing(); }

    MovementOutcome moveAndObserve(double dx, double dy, double dz, int ticks)
            throws IOException, InterruptedException {
        PlayerPose attempted = moveBy(dx, dy, dz); boolean corrected = sustain(ticks);
        return new MovementOutcome(attempted, pose, corrected
                ? MovementDisposition.CORRECTED : MovementDisposition.UNCHALLENGED);
    }

    private boolean sustain(int ticks) throws IOException, InterruptedException {
        require(pose != null, "play channel is not synchronized");
        if (ticks < 1 || ticks > 1200) throw new IllegalArgumentException("invalid heartbeat tick count");
        boolean corrected = false;
        for (int tick = 1; tick <= ticks; tick++) {
            if (tick % 20 == 0) acknowledge(pose.x(), pose.y(), pose.y() + stanceHeight,
                    pose.z(), pose.yaw(), pose.pitch());
            else { output.writeByte(10); output.writeBoolean(false); output.flush(); }
            Thread.sleep(50L); inbound.pumpAvailable(); corrected |= applyCorrection();
        }
        return corrected;
    }

    private boolean applyCorrection() {
        B173PlayInbound.Correction value = inbound.takeCorrection();
        if (value == null) return false;
        pose = value.pose; stanceHeight = value.stance; return true;
    }

    private void acknowledge(double x, double feetY, double clientY, double z,
            float yaw, float pitch) throws IOException {
        output.writeByte(13);
        output.writeDouble(x); output.writeDouble(feetY); output.writeDouble(clientY);
        output.writeDouble(z); output.writeFloat(yaw); output.writeFloat(pitch);
        output.writeBoolean(false); output.flush();
    }

    private void dig(BlockPosition position, int status) throws IOException {
        require(pose != null, "play channel is not synchronized");
        if (position == null || position.y() < 0 || position.y() >= 128)
            throw new IllegalArgumentException("invalid dig position");
        output.writeByte(14); output.writeByte(status); output.writeInt(position.x());
        output.writeByte(position.y()); output.writeInt(position.z()); output.writeByte(1); output.flush();
    }

    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
}
