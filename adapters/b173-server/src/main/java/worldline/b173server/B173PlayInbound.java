package worldline.b173server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.PlayerPose;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteHeldItem;
import worldline.api.RemoteItemCollection;

/** Single bounded inbound pump that preserves Packet50/51 lifecycle state. */
final class B173PlayInbound {
    private final DataInputStream input;
    private final DataOutputStream output;
    private final B173RemoteWorldCache cache = new B173RemoteWorldCache();
    private final B173ItemInbound items;
    private final long timeoutNanos;
    private Correction correction; private int dimension; private long respawnEpoch; private worldline.api.RemoteExplosion explosion; private final B173MobTracker mobs=new B173MobTracker(); private final B173ObjectTracker objects=new B173ObjectTracker(); private final B173VelocityTracker velocities=new B173VelocityTracker(); private final B173BedTracker beds=new B173BedTracker(); private final B173NoteEvent notes=new B173NoteEvent(); private final B173SignTracker signs=new B173SignTracker(); private final B173PaintingTracker paintings=new B173PaintingTracker(); private final B173PlayWaits waits=new B173PlayWaits(this);

    B173PlayInbound(DataInputStream input, DataOutputStream output, int timeoutMillis,
            int localEntityId, String localUsername, int dimension) throws IOException { this.input = input;
        this.output = output; this.timeoutNanos = timeoutMillis * 1_000_000L;
        this.items = new B173ItemInbound(localEntityId, localUsername, output, mobs); if (dimension != 0 && dimension != -1) throw new IOException("invalid initial dimension"); this.dimension = dimension; }

    void skip(int packet) throws IOException {
        if (packet == 0) { synchronized (output) {
            output.writeByte(10); output.writeBoolean(false); output.flush(); } return; }
        if (packet == 9) { int next = input.readByte(); if (next != 0 && next != -1) throw new IOException("invalid respawn dimension"); if (next != dimension) cache.reset(); dimension = next; respawnEpoch++; return; } if (items.accept(packet, input)) return;
        if (packet == 3) { B173InboundPacket.string(input, 119); return; }
        if (packet == 13) { position(); return; }
        if (packet == 50) { cache.preChunk(input); return; }
        if (packet == 51) { cache.accept(B173ChunkCodec.read(input)); return; }
        if (packet == 52) { cache.multiBlock(input); return; }
        if (packet == 53) { cache.singleBlock(input); return; } if(packet==23){objects.spawn(input);return;} if(packet==39){objects.attach(input);return;} if(packet==24){mobs.spawn(input);return;} if(packet==25){paintings.spawn(input);return;} if(packet==28){velocities.accept(input);return;} if(packet>=30&&packet<=34){mobs.update(packet,input);return;} if(packet==40){mobs.watcher(input);return;} if (packet == 60) { if (explosion != null) throw new IOException("unconsumed explosion observation"); explosion = cache.explosion(input); return; } if(packet==17||packet==70){beds.accept(packet,input);return;} if(packet==54||packet==61){notes.accept(packet,input);return;} if(packet==130){signs.accept(input);return;}
        if (packet == 255) throw disconnect();
        B173InboundPacket.skip(input, packet);
    }

    String awaitChat() throws IOException {
        for (int count = 0; count < 4096; count++) {
            int packet = input.readUnsignedByte();
            if (packet == 3) return B173InboundPacket.string(input, 119);
            skip(packet);
        }
        throw new IOException("chat packet absent from bounded inbound window");
    }

    RemoteChunkSnapshot awaitChunk() throws IOException {
        RemoteChunkSnapshot ready = cache.firstDecoded();
        if (ready != null) return ready;
        for (int count = 0; count < 4096; count++) {
            int packet = input.readUnsignedByte();
            if (packet == 51) {
                RemoteChunkSnapshot chunk = B173ChunkCodec.read(input);
                if (cache.accept(chunk)) return chunk;
                continue;
            }
            skip(packet);
        }
        throw new IOException("chunk packet absent from bounded inbound window");
    }

    RemoteWorldView awaitWorld(int minimumChunks) throws IOException {
        if (minimumChunks < 1 || minimumChunks > RemoteWorldView.MAX_CHUNKS)
            throw new IllegalArgumentException("invalid minimum remote chunk count");
        if (cache.decoded() >= minimumChunks) return cache.snapshot();
        for (int count = 0; count < 8192; count++) {
            int packet;
            try { packet = input.readUnsignedByte(); }
            catch (IOException error) { throw new IOException("remote world stream ended with decoded="
                    + cache.decoded() + ",tracked=" + cache.tracked(), error); }
            if (packet == 51) cache.accept(B173ChunkCodec.read(input));
            else skip(packet);
            if (cache.decoded() >= minimumChunks) return cache.snapshot();
        }
        throw new IOException("remote world minimum absent from bounded inbound window");
    }

    RemoteWorldView awaitChunk(int chunkX, int chunkZ) throws IOException {
        if (cache.snapshot().containsChunk(chunkX, chunkZ)) return cache.snapshot();
        for (int count = 0; count < 16384; count++) {
            skip(input.readUnsignedByte());
            if (cache.snapshot().containsChunk(chunkX, chunkZ)) return cache.snapshot();
        }
        throw new IOException("requested remote chunk absent from bounded inbound window");
    }

    RemoteWorldView awaitBlock(BlockPosition position, BlockState expected) throws IOException {
        if (position == null || expected == null) throw new IllegalArgumentException("null block wait");
        if (cache.matches(position, expected)) return cache.snapshot();
        Thread pulse = pulse();
        long deadline = System.nanoTime() + timeoutNanos;
        try {
            for (int count = 0; count < 8192 && System.nanoTime() < deadline; count++) {
                skip(input.readUnsignedByte());
                if (cache.matches(position, expected)) return cache.snapshot();
            }
            throw new IOException("expected block state absent before deadline");
        } finally { pulse.interrupt(); }
    }

    void pumpAvailable() throws IOException {
        for (int count = 0; count < 4096 && input.available() > 0; count++)
            skip(input.readUnsignedByte());
    }

    RemoteWorldView snapshot() { return cache.snapshot(); } int dimension() { return dimension; } int awaitDimension(int expected) throws IOException { if (expected != 0 && expected != -1) throw new IllegalArgumentException("invalid expected dimension"); for (int count = 0; count < 8192; count++) { if (dimension == expected) return dimension; skip(input.readUnsignedByte()); } throw new IOException("dimension absent from bounded inbound window"); } long respawnEpoch(){return respawnEpoch;} int awaitRespawn(long before,int expected)throws IOException{for(int count=0;count<8192;count++){if(respawnEpoch>before){if(dimension!=expected)throw new IOException("respawn dimension drift");return dimension;}skip(input.readUnsignedByte());}throw new IOException("respawn packet absent");} int health(){return items.health();} int awaitHealth(int expected)throws IOException{return items.awaitHealth(expected,this::pumpOne);} worldline.api.RemoteExplosion awaitExplosion()throws IOException{for(int count=0;count<8192;count++){if(explosion!=null){worldline.api.RemoteExplosion value=explosion;explosion=null;return value;}pumpOne();}throw new IOException("explosion observation absent");} worldline.api.RemoteMobSpawn awaitMobSpawn(int type)throws IOException{return waits.spawn(type);} worldline.api.RemoteMobSpawn awaitMobSpawnAny(int[] types)throws IOException{return waits.spawnAny(types);} worldline.api.RemoteMobMovement awaitMobMovement(int entity)throws IOException{return waits.movement(entity);} worldline.api.RemoteMobDeath awaitMobDeath(int entity)throws IOException{return waits.death(entity);} worldline.api.RemoteMobMovement awaitObservedMobMovement()throws IOException{return waits.observedMovement();} worldline.api.RemoteMobDeath awaitObservedMobDeath()throws IOException{return waits.observedDeath();} B173EntityVelocity awaitVelocity(int entity)throws IOException{return waits.velocity(entity);} worldline.api.RemoteDroppedItem peekDroppedItem(worldline.api.RemoteItemStack expected){return items.peekDropped(expected);} worldline.api.RemoteObjectSpawn awaitObjectSpawn(int type)throws IOException{return waits.object(type);} worldline.api.RemoteObjectSpawn awaitThrownObject(int type,int thrower)throws IOException{return waits.objectFrom(type,thrower);} worldline.api.RemoteBedUse awaitBedUse()throws IOException{return waits.bed();} worldline.api.RemoteNoteEvent awaitNoteEvent()throws IOException{return waits.note();} worldline.api.RemoteSignText awaitSignText()throws IOException{return waits.sign();} worldline.api.RemotePaintingSpawn awaitPainting()throws IOException{return waits.painting();} int awaitCreeperFuse(int entity)throws IOException{return waits.fuse(entity).intValue();} boolean explosionQueued(){return explosion!=null;}

    RemoteInventoryView awaitInventory() throws IOException { return items.awaitInventory(this::pumpOne); }
    RemoteInventoryView inventory() { return items.inventory(); }
    worldline.api.RemoteContainerWindow awaitChest() throws IOException {
        return items.awaitChest(this::pumpOne); }
    worldline.api.RemoteFurnaceSmelt awaitFurnaceSmelt() throws IOException { return items.awaitFurnaceSmelt(this::pumpOne); }
    void beginWindow(worldline.api.RemoteWindowKind kind) { items.beginWindow(kind); } int activeWindowId() { return items.activeWindowId(); } worldline.api.RemoteContainerWindow activeWindow() { return items.activeWindow(); } long activeWindowEpoch() { return items.activeWindowEpoch(); } boolean windowActive() { return items.windowActive(); } void closeWindow(int id) throws IOException { items.closeWindow(id); } int combatEntityId(String name) { return items.combatEntityId(name); } void beginCombat(int target) { items.beginCombat(target); } worldline.api.RemoteCombatStrike awaitCombatStrike() throws IOException { return items.awaitCombatStrike(this::pumpOne); } worldline.api.RemoteIncomingHit awaitIncomingHit(int health) throws IOException { return items.awaitIncomingHit(health, this::pumpOne); } worldline.api.RemotePeerSwing awaitPeerSwing(String username) throws IOException { return items.awaitPeerSwing(username, this::pumpOne); }
    boolean cursorObserved() { return items.cursorObserved(); }
    worldline.api.RemoteItemStack cursor() { return items.cursor(); }
    void beginPersonalTransaction(int action, int slot, worldline.api.RemoteItemStack predicted,
            RemoteInventoryView before, RemoteInventoryView after,
            worldline.api.RemoteItemStack cursorBefore, worldline.api.RemoteItemStack cursorAfter) {
        items.beginPersonalTransaction(action, slot, predicted, before, after, cursorBefore, cursorAfter); }
    worldline.api.RemotePersonalTransaction awaitPersonalTransaction() throws IOException {
        return items.awaitPersonalTransaction(this::pumpOne); } B173PersonalStep awaitPersonalStep() throws IOException { return items.awaitPersonalStep(this::pumpOne); }
    void beginContainerTransaction(B173ContainerStep step) { items.beginContainerTransaction(step); } B173ContainerStep awaitContainerTransaction() throws IOException { return items.awaitContainerTransaction(this::pumpOne); }
    RemoteHeldItem awaitPeerHeldItem(RemoteHeldItem expected) throws IOException {
        return items.awaitPeerHeldItem(expected, this::pumpOne); }
    worldline.api.RemoteArmorPiece awaitPeerArmor(worldline.api.RemoteArmorPiece expected) throws IOException { return items.awaitPeerArmor(expected, this::pumpOne); }
    worldline.api.RemoteDroppedItem awaitDroppedItem(worldline.api.RemoteItemStack expected) throws IOException {
        return items.awaitDroppedItem(expected, this::pumpOne); }
    RemoteItemCollection awaitItemCollection(worldline.api.RemoteDroppedItem expected, String username)
            throws IOException { return items.awaitCollection(expected, username, this::pumpOne); }

    void enableImplicitChunks() { cache.enableImplicitLoads(); }

    Correction takeCorrection() { Correction result = correction; correction = null; return result; }

    private void position() throws IOException {
        double x = input.readDouble(), clientY = input.readDouble(), feetY = input.readDouble();
        double z = input.readDouble(); float yaw = input.readFloat(), pitch = input.readFloat();
        input.readBoolean();
        if (clientY <= feetY || clientY - feetY >= 2D) throw new IOException("server correction stance drift");
        synchronized (output) {
            output.writeByte(13); output.writeDouble(x); output.writeDouble(feetY);
            output.writeDouble(clientY); output.writeDouble(z); output.writeFloat(yaw);
            output.writeFloat(pitch); output.writeBoolean(false); output.flush();
        }
        correction = new Correction(new PlayerPose(x, feetY, z, yaw, pitch), clientY - feetY);
    }

    Thread pulse() { return B173Pulse.start(output); }
    B173MobTracker mobs() { return mobs; }
    B173ObjectTracker objects() { return objects; } B173VelocityTracker velocities() { return velocities; } B173BedTracker beds() { return beds; } B173NoteEvent notes() { return notes; } B173SignTracker signs() { return signs; } B173PaintingTracker paintings() { return paintings; }
    long timeoutNanos() { return timeoutNanos; }
    B173RemoteWorldCache cache() { return cache; }
    void pumpOne() throws IOException { skip(input.readUnsignedByte()); }

    private IOException disconnect() throws IOException { return new IOException(
            "server disconnected: " + B173InboundPacket.string(input, 256)); }

    static final class Correction { final PlayerPose pose; final double stance;
        Correction(PlayerPose pose, double stance) { this.pose = pose; this.stance = stance; } }
}
