package worldline.b173server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import worldline.api.MultiplayerConnection;
import worldline.api.MultiplayerState;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkObservation;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.MovementOutcome;
import worldline.api.ObjectObservationSession;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteHeldItem;

/** Minimal original protocol-14 client for headless multiplayer qualification. */
public final class B173WireClient implements ObjectObservationSession {
    public static final int PROTOCOL = 14;
    private final String host, username; private final int port, timeoutMillis;
    private MultiplayerConnection connection = MultiplayerConnection.NEW;
    private int entityId = MultiplayerState.UNKNOWN_ENTITY, dimension;
    private Socket socket;
    private B173PlayChannel play;

    public B173WireClient(String host, int port, String username, Duration timeout) {
        if (host == null || host.isEmpty()) throw new IllegalArgumentException("empty host");
        if (port < 1 || port > 65535) throw new IllegalArgumentException("invalid port");
        if (username == null || username.isEmpty() || username.length() > 16)
            throw new IllegalArgumentException("invalid username");
        this.host = host; this.port = port; this.username = username;
        this.timeoutMillis = Math.toIntExact(timeout.toMillis());
    }

    @Override
    public void connect() {
        require(connection == MultiplayerConnection.NEW, "session was already used");
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), timeoutMillis);
            socket.setSoTimeout(timeoutMillis);
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            output.writeByte(2); B173InboundPacket.string(output, username); output.flush();
            require(input.readUnsignedByte() == 2, "handshake response packet drift");
            require(B173InboundPacket.string(input, 32).equals("-"), "server did not use offline handshake");
            output.writeByte(1); output.writeInt(PROTOCOL); B173InboundPacket.string(output, username);
            output.writeLong(0L); output.writeByte(0); output.flush();
            int packet = input.readUnsignedByte();
            if (packet == 255) throw new IllegalStateException("login rejected: " + B173InboundPacket.string(input, 256));
            require(packet == 1, "login response packet drift: " + packet);
            entityId = input.readInt();
            B173InboundPacket.string(input, 16); input.readLong(); dimension = input.readByte(); require(dimension == 0 || dimension == -1, "server returned invalid dimension");
            require(entityId >= 0, "server returned invalid entity id");
            play = new B173PlayChannel(input, output, timeoutMillis, entityId, username, dimension);
            connection = MultiplayerConnection.CONNECTED;
        } catch (IOException error) { closeSocket(); throw new IllegalStateException("multiplayer login failed", error); }
    }

    @Override public MultiplayerState state() { return new MultiplayerState(connection, username, PROTOCOL, entityId); } @Override public int dimension() { return channel().dimension(); } @Override public int awaitDimension(int expected) { try { return channel().awaitDimension(expected); } catch (IOException error) { throw new IllegalStateException("dimension transition absent", error); } } @Override public int health(){return channel().health();} @Override public int awaitHealth(int expected){try{return channel().awaitHealth(expected);}catch(IOException error){throw new IllegalStateException("health observation absent",error);}} @Override public worldline.api.RemoteRespawn respawn(){try{return channel().respawn();}catch(IOException error){throw new IllegalStateException("respawn failed",error);}} @Override public worldline.api.RemoteExplosion awaitExplosion(){try{return channel().awaitExplosion();}catch(IOException error){throw new IllegalStateException("explosion observation absent",error);}}

    @Override public PlayerPose synchronizePose() {
        require(connection == MultiplayerConnection.CONNECTED, "session is not connected");
        try { return play.synchronize(); }
        catch (IOException error) { throw new IllegalStateException("play synchronization failed", error); }
    }

    @Override public void look(float yaw, float pitch) {
        require(connection == MultiplayerConnection.CONNECTED, "session is not connected");
        try { play.look(yaw, pitch); }
        catch (IOException error) { throw new IllegalStateException("play look failed", error); }
    }

    @Override
    public PlayerPose moveBy(double deltaX, double deltaY, double deltaZ) {
        require(connection == MultiplayerConnection.CONNECTED, "session is not connected");
        try { return play.moveBy(deltaX, deltaY, deltaZ); }
        catch (IOException error) { throw new IllegalStateException("play movement failed", error); }
    }

    @Override
    public void sendChat(String message) {
        require(connection == MultiplayerConnection.CONNECTED, "session is not connected");
        try { play.sendChat(message); }
        catch (IOException error) { throw new IllegalStateException("chat send failed", error); }
    }

    @Override
    public String awaitChat() {
        require(connection == MultiplayerConnection.CONNECTED, "session is not connected");
        try { return play.awaitChat(); }
        catch (IOException error) { throw new IllegalStateException("chat receive failed", error); }
    }

    @Override
    public RemoteChunkObservation awaitChunk() {
        require(connection == MultiplayerConnection.CONNECTED, "session is not connected");
        try { return play.awaitChunk(); }
        catch (IOException error) { throw new IllegalStateException("chunk receive failed", error); }
    }

    @Override
    public RemoteChunkSnapshot awaitChunkSnapshot() {
        require(connection == MultiplayerConnection.CONNECTED, "session is not connected");
        try { return play.awaitChunkSnapshot(); } catch (IOException error) { throw new IllegalStateException("chunk decode failed", error); }
    }

    @Override
    public RemoteWorldView awaitRemoteWorld(int minimumChunks) {
        require(connection == MultiplayerConnection.CONNECTED, "session is not connected");
        try { return play.awaitRemoteWorld(minimumChunks); } catch (IOException error) { throw new IllegalStateException("remote world receive failed", error); }
    }

    @Override public RemoteWorldView awaitRemoteChunk(int chunkX, int chunkZ) {
        require(connection == MultiplayerConnection.CONNECTED, "session is not connected");
        try { return play.awaitRemoteChunk(chunkX, chunkZ); } catch (IOException error) { throw new IllegalStateException("remote chunk receive failed", error); }
    }

    @Override public void beginBreak(BlockPosition position) {
        require(connection == MultiplayerConnection.CONNECTED, "session is not connected");
        try { play.beginBreak(position); } catch (IOException error) { throw new IllegalStateException("begin break failed", error); }
    }

    @Override public void finishBreak(BlockPosition position) {
        require(connection == MultiplayerConnection.CONNECTED, "session is not connected");
        try { play.finishBreak(position); } catch (IOException error) { throw new IllegalStateException("finish break failed", error); }
    }

    @Override public RemoteWorldView awaitBlock(BlockPosition position, BlockState expected) {
        require(connection == MultiplayerConnection.CONNECTED, "session is not connected");
        try { return play.awaitBlock(position, expected); } catch (IOException error) {
            throw new IllegalStateException("expected block receive failed", error); }
    }

    @Override public RemoteWorldView sustainTicks(int ticks) {
        require(connection == MultiplayerConnection.CONNECTED, "session is not connected");
        try { return play.sustainTicks(ticks); }
        catch (IOException error) { throw new IllegalStateException("play heartbeat failed", error); }
        catch (InterruptedException error) { Thread.currentThread().interrupt();
            throw new IllegalStateException("play heartbeat interrupted", error); }
    }

    @Override public MovementOutcome moveAndObserve(double dx, double dy, double dz, int ticks) {
        require(connection == MultiplayerConnection.CONNECTED, "session is not connected");
        try { return play.moveAndObserve(dx, dy, dz, ticks); }
        catch (IOException error) { throw new IllegalStateException("movement observation failed", error); }
        catch (InterruptedException error) { Thread.currentThread().interrupt();
            throw new IllegalStateException("movement observation interrupted", error); }
    }

    @Override public RemoteInventoryView awaitInventory() { return B173ItemAccess.awaitInventory(channel()); } @Override public RemoteInventoryView inventory() { return B173ItemAccess.inventory(channel()); }
    @Override public void selectHeldSlot(int slot) { B173ItemAccess.selectHeldSlot(channel(), slot); } @Override public void dropHeldItem() { B173ItemAccess.dropHeldItem(channel()); }
    @Override public void placeHeldBlock(BlockPosition support, worldline.api.BlockFace face) { B173ItemAccess.placeHeldBlock(channel(), support, face); } @Override public void useHeldItemOnBlock(BlockPosition support, worldline.api.BlockFace face) { B173ItemAccess.useHeldItemOnBlock(channel(), support, face); } @Override public void activateBlock(BlockPosition position, worldline.api.BlockFace face) { B173ItemAccess.activateBlock(channel(), position, face); }
    @Override public worldline.api.RemoteContainerWindow openChest(BlockPosition position, worldline.api.BlockFace face) { return B173ItemAccess.openChest(channel(), position, face); } @Override public worldline.api.RemoteChestTransfer storeInOpenChest(int personalSlot, int chestSlot) { return B173ItemAccess.storeInOpenChest(channel(), personalSlot, chestSlot); } @Override public worldline.api.RemoteChestRetrieval retrieveFromOpenChest(int chestSlot, int personalSlot) { return B173ItemAccess.retrieveFromOpenChest(channel(), chestSlot, personalSlot); } @Override public worldline.api.RemoteContainerWindow openFurnace(BlockPosition position, worldline.api.BlockFace face) { return B173ItemAccess.openFurnace(channel(), position, face); } @Override public worldline.api.RemoteFurnaceLoad loadFurnace(int inputSlot, int fuelSlot) { return B173ItemAccess.loadFurnace(channel(), inputSlot, fuelSlot); } @Override public worldline.api.RemoteFurnaceSmelt awaitFurnaceSmelt() { return B173ItemAccess.awaitFurnaceSmelt(channel()); } @Override public worldline.api.RemoteFurnaceExtraction takeFurnaceOutput(int personalSlot) { return B173ItemAccess.takeFurnaceOutput(channel(), personalSlot); } @Override public worldline.api.RemoteContainerWindow openWorkbench(BlockPosition position, worldline.api.BlockFace face) { return B173ItemAccess.openWorkbench(channel(), position, face); } @Override public worldline.api.RemoteWorkbenchPreparation prepareWorkbenchSlabs(int personalSlot) { return B173ItemAccess.prepareWorkbenchSlabs(channel(), personalSlot); } @Override public worldline.api.RemoteWorkbenchOutput takeWorkbenchSlabs(int personalSlot) { return B173ItemAccess.takeWorkbenchSlabs(channel(), personalSlot); } @Override public worldline.api.RemoteWindowClosure closeWindow() { return B173ItemAccess.closeWindow(channel()); } @Override public worldline.api.RemotePersonalTransaction clickPersonalSlot(int slot) { return B173ItemAccess.clickPersonalSlot(channel(), slot); } @Override public worldline.api.RemotePersonalCraft craftPersonal2x2(int slot) { return B173ItemAccess.craftPersonal2x2(channel(), slot); } worldline.api.RemoteRejectedTransaction rejectedTakeProbe(int slot) { return B173ItemAccess.rejectedTakeProbe(channel(), slot); }
    @Override public RemoteHeldItem awaitPeerHeldItem(RemoteHeldItem expected) { return B173ItemAccess.awaitPeerHeldItem(channel(), expected); } @Override public worldline.api.RemoteArmorEquip equipLeatherArmor(int personalSlot, worldline.api.RemoteArmorSlot slot) { return B173ItemAccess.equipLeatherArmor(channel(), personalSlot, slot); } @Override public worldline.api.RemoteArmorPiece awaitPeerArmor(worldline.api.RemoteArmorPiece expected) { return B173ItemAccess.awaitPeerArmor(channel(), expected); } @Override public worldline.api.RemoteCombatStrike attackPlayer(String target) { return B173ItemAccess.attackPlayer(channel(), target); } @Override public worldline.api.RemoteIncomingHit awaitIncomingHit(int health) { return B173ItemAccess.awaitIncomingHit(channel(), health); } @Override public worldline.api.RemoteSwingRequest swingHeldItem() { return B173ItemAccess.swingHeldItem(channel()); } @Override public worldline.api.RemotePeerSwing awaitPeerSwing(String username) { return B173ItemAccess.awaitPeerSwing(channel(), username); } @Override public worldline.api.RemoteMobSpawn awaitMobSpawn(int type){return B173ItemAccess.awaitMobSpawn(channel(),type);} @Override public worldline.api.RemoteMobMovement awaitMobMovement(int entity){return B173ItemAccess.awaitMobMovement(channel(),entity);} @Override public void attackMob(int entity){B173ItemAccess.attackMob(channel(),entity);} @Override public worldline.api.RemoteMobDeath awaitMobDeath(int entity){return B173ItemAccess.awaitMobDeath(channel(),entity);} @Override public worldline.api.RemoteMobMovement awaitObservedMobMovement(){return B173ItemAccess.awaitObservedMobMovement(channel());} @Override public void attackObservedMob(){B173ItemAccess.attackObservedMob(channel());} @Override public worldline.api.RemoteMobDeath awaitObservedMobDeath(){return B173ItemAccess.awaitObservedMobDeath(channel());} @Override public worldline.api.RemoteDroppedItem peekDroppedItem(worldline.api.RemoteItemStack expected){return B173ItemAccess.peekDroppedItem(channel(), expected);}
    @Override public worldline.api.RemoteDroppedItem awaitDroppedItem(worldline.api.RemoteItemStack expected) { return B173ItemAccess.awaitDroppedItem(channel(), expected); } @Override public void useSelectedItemInAir() { B173ItemAccess.useSelectedItemInAir(channel()); } @Override public worldline.api.RemoteObjectSpawn awaitObjectSpawn(int type) { return B173ItemAccess.awaitObjectSpawn(channel(), type); }
    @Override public worldline.api.RemoteItemCollection awaitItemCollection(worldline.api.RemoteDroppedItem expected, String username) { return B173ItemAccess.awaitItemCollection(channel(), expected, username); }

    @Override public void close() { closeSocket(); if (connection != MultiplayerConnection.NEW) connection = MultiplayerConnection.DISCONNECTED; }

    private void closeSocket() { if (socket == null) return;
        try { socket.close(); } catch (IOException error) {
            throw new IllegalStateException("could not close multiplayer socket", error); }
        finally { socket = null; play = null; }
    }

    B173PlayChannel channel() { require(connection == MultiplayerConnection.CONNECTED, "session is not connected"); return play; }

    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
}
