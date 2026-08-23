package worldline.api;

public final class DomainApiTest {
    private DomainApiTest() {}

    public static void main(String[] arguments) {
        valueEqualityIsExact();
        invalidValuesFailClosed();
        snapshotsAreBoundedAndImmutable();
        uiSpecRoundTripsBuilderAndInventory();
        itemCensusIsExactAndFailClosed();
        itemStackRecipePreservesMetadata();
        WorldlineContractTest.run();
        semanticMappingIsExactAndFailClosed();
        expandedSurfaceDefaultsFailClosed();
        serverStateIsExactAndFailClosed();
        multiplayerStateIsExactAndFailClosed();
        serverPlayerStateIsExactAndFailClosed();
        remoteChunkSnapshotIsImmutableAndAddressable();
        remoteWorldViewIsSortedAndAddressable();
        movementOutcomeIsExactAndFailClosed();
        movementRouteIsImmutableAndRecovers();
        movementRoutePolicyStopsWithoutRetry();
        movementAlternativeIsExplicitAndFailClosed();
        movementRouteEventIsIndexedAndFailClosed();
        movementRouteExecutionIsExactAndFailClosed();
        correlatedRouteExecutionPreservesIdentity();
        RemoteWindowLayoutTest.run();
        RemoteWorkbenchPreparationTest.run();
        RemoteWorkbenchOutputTest.run();
        RemoteArmorEquipmentTest.run();
        RemoteCombatTest.run();
        RemoteRespawnTest.run(); RemoteExplosionTest.run(); RemoteMobSpawnTest.run(); RemoteMobMovementTest.run(); RemoteMobDeathTest.run(); RemoteObjectSpawnTest.run(); RemoteBedUseTest.run(); RemoteNoteEventTest.run(); RemoteSignTextTest.run(); RemotePaintingSpawnTest.run();
        RemoteChestRetrievalTest.run();
        RemoteObjectMovementTest.run();
        RemoteRainStartTest.run();
        RemoteDispenserLoadTest.run();
        WorldlineEvidenceTest.run();
        peerSwingValuesAreExactAndFailClosed();
        System.out.println("DomainApiTest passed");
    }

    private static void peerSwingValuesAreExactAndFailClosed() {
        equal(new RemoteSwingRequest("SwingActor69", 7), new RemoteSwingRequest("SwingActor69", 7), "swing request");
        equal(new RemotePeerSwing("SwingActor69", 7), new RemotePeerSwing("SwingActor69", 7), "peer swing");
        if (new RemotePeerSwing("SwingActor69", 7).animation() != 1) throw new AssertionError("swing animation drifted");
        failure(() -> new RemoteSwingRequest("bad name", 7)); failure(() -> new RemotePeerSwing("peer", -1));
    }

    private static void correlatedRouteExecutionPreservesIdentity() {
        Object correlation = new Object(); PlayerPose pose = new PlayerPose(0D, 64D, 0D, 0F, 0F);
        MovementOutcome outcome = new MovementOutcome(pose, pose, MovementDisposition.UNCHALLENGED);
        MovementRouteResult result = new MovementRouteResult(java.util.Collections.singletonList(outcome));
        MovementRouteEvent event = new MovementRouteEvent(0, 0, MovementAttemptKind.PRIMARY, outcome);
        MovementRouteExecution execution = new MovementRouteExecution(
                result, MovementRouteTermination.EXHAUSTED, event);
        CorrelatedMovementRouteEvent correlated = new CorrelatedMovementRouteEvent(correlation, event);
        CorrelatedMovementRouteExecution value = new CorrelatedMovementRouteExecution(
                correlation, execution, correlated);
        if (value.correlation() != correlation || value.execution() != execution
                || value.terminalEvent() != correlated)
            throw new AssertionError("correlated route identity drifted");
        failure(() -> new CorrelatedMovementRouteExecution(
                new Object(), execution, correlated));
    }

    private static void movementRouteExecutionIsExactAndFailClosed() {
        PlayerPose pose = new PlayerPose(0D, 64D, 0D, 0F, 0F);
        MovementOutcome outcome = new MovementOutcome(pose, pose, MovementDisposition.UNCHALLENGED);
        MovementRouteResult result = new MovementRouteResult(java.util.Collections.singletonList(outcome));
        MovementRouteEvent event = new MovementRouteEvent(0, 0, MovementAttemptKind.PRIMARY, outcome);
        MovementRouteExecution execution = new MovementRouteExecution(
                result, MovementRouteTermination.CONTROLLER_STOP, event);
        if (execution.result() != result || execution.terminalEvent() != event || !execution.stopped()
                || execution.termination() != MovementRouteTermination.CONTROLLER_STOP)
            throw new AssertionError("movement route execution accessors drifted");
        MovementOutcome equalButDistinct = new MovementOutcome(pose, pose, MovementDisposition.UNCHALLENGED);
        failure(() -> new MovementRouteExecution(result, MovementRouteTermination.EXHAUSTED,
                new MovementRouteEvent(0, 0, MovementAttemptKind.PRIMARY, equalButDistinct)));
    }

    private static void movementRouteEventIsIndexedAndFailClosed() {
        PlayerPose pose = new PlayerPose(0D, 64D, 0D, 0F, 0F);
        MovementOutcome outcome = new MovementOutcome(pose, pose, MovementDisposition.UNCHALLENGED);
        MovementRouteEvent event = new MovementRouteEvent(2, 3, MovementAttemptKind.FALLBACK, outcome);
        if (event.alternativeIndex() != 2 || event.outcomeIndex() != 3
                || event.kind() != MovementAttemptKind.FALLBACK || event.outcome() != outcome)
            throw new AssertionError("movement route event accessors drifted");
        failure(() -> new MovementRouteEvent(-1, 0, MovementAttemptKind.PRIMARY, outcome));
        failure(() -> new MovementRouteEvent(0, 64, MovementAttemptKind.PRIMARY, outcome));
        failure(() -> new MovementRouteEvent(0, 0, null, outcome));
    }

    private static void movementAlternativeIsExplicitAndFailClosed() {
        MovementStep primary = new MovementStep(.125D, 0D, 0D, 5);
        MovementStep fallback = new MovementStep(0D, 0D, .125D, 5);
        MovementAlternative alternative = new MovementAlternative(primary, fallback);
        if (alternative.primary() != primary || alternative.fallback() != fallback)
            throw new AssertionError("movement alternative accessors drifted");
        failure(() -> new MovementAlternative(null, fallback));
        failure(() -> new MovementAlternative(primary, null));
    }

    private static void movementRoutePolicyStopsWithoutRetry() {
        java.util.List<MovementDisposition> script = new java.util.ArrayList<>(java.util.Arrays.asList(
                MovementDisposition.UNCHALLENGED, MovementDisposition.CORRECTED,
                MovementDisposition.UNCHALLENGED));
        class Session implements RecoveringMovementMultiplayerSession {
            int calls; PlayerPose pose = new PlayerPose(0D, 64D, 0D, 0F, 0F);
            @Override public MovementOutcome moveAndObserve(double x, double y, double z, int ticks) {
                PlayerPose attempted = new PlayerPose(pose.x() + x, pose.y() + y, pose.z() + z, 0F, 0F);
                MovementDisposition disposition = script.get(calls++); PlayerPose result = disposition
                        == MovementDisposition.CORRECTED ? pose : attempted; pose = result;
                return new MovementOutcome(attempted, result, disposition);
            }
            @Override public RemoteWorldView sustainTicks(int ticks) { throw new UnsupportedOperationException(); }
            @Override public void connect() { } @Override public void close() { }
            @Override public MultiplayerState state() { return new MultiplayerState(MultiplayerConnection.CONNECTED, "Worldline", 14, 1); }
            @Override public PlayerPose synchronizePose() { return pose; }
            @Override public void look(float yaw, float pitch) { }
            @Override public PlayerPose moveBy(double x, double y, double z) { throw new UnsupportedOperationException(); }
            @Override public String awaitChat() { throw new UnsupportedOperationException(); }
            @Override public void sendChat(String message) { throw new UnsupportedOperationException(); }
            @Override public RemoteChunkObservation awaitChunk() { throw new UnsupportedOperationException(); }
            @Override public RemoteChunkSnapshot awaitChunkSnapshot() { throw new UnsupportedOperationException(); }
            @Override public RemoteWorldView awaitRemoteWorld(int minimum) { throw new UnsupportedOperationException(); }
            @Override public RemoteWorldView awaitRemoteChunk(int x, int z) { throw new UnsupportedOperationException(); }
            @Override public void beginBreak(BlockPosition position) { throw new UnsupportedOperationException(); }
            @Override public void finishBreak(BlockPosition position) { throw new UnsupportedOperationException(); }
            @Override public RemoteWorldView awaitBlock(BlockPosition p, BlockState s) { throw new UnsupportedOperationException(); }
        }
        Session session = new Session(); MovementStep step = new MovementStep(.125D, 0D, 0D, 1);
        MovementRouteResult stopped = session.moveRoute(java.util.Arrays.asList(step, step, step),
                RouteCorrectionPolicy.STOP_ON_CORRECTION);
        if (session.calls != 2 || stopped.outcomes().size() != 2 || stopped.corrections() != 1)
            throw new AssertionError("stop-on-correction policy retried or continued");
    }

    private static void movementRouteIsImmutableAndRecovers() {
        if (!ResolvedMovementMultiplayerSession.class.isAssignableFrom(
                RecoveringMovementMultiplayerSession.class)) throw new AssertionError("recovering session hierarchy drifted");
        MovementStep step = new MovementStep(.125D, 0D, 0D, 5);
        if (step.deltaX() != .125D || step.deltaY() != 0D || step.deltaZ() != 0D || step.ticks() != 5)
            throw new AssertionError("movement step accessors drifted");
        PlayerPose first = new PlayerPose(1D, 64D, 2D, 0F, 0F);
        PlayerPose second = new PlayerPose(1.125D, 64D, 2D, 0F, 0F);
        java.util.List<MovementOutcome> source = new java.util.ArrayList<>();
        source.add(new MovementOutcome(first, first, MovementDisposition.UNCHALLENGED));
        source.add(new MovementOutcome(second, first, MovementDisposition.CORRECTED));
        MovementRouteResult result = new MovementRouteResult(source); source.clear();
        if (result.outcomes().size() != 2 || result.corrections() != 1 || result.finalPose() != first)
            throw new AssertionError("movement route result drifted");
        failure(() -> new MovementStep(0D, 0D, 0D, 5));
        failure(() -> new MovementStep(.125D, 0D, 0D, 0));
        failure(() -> new MovementRouteResult(java.util.Collections.emptyList()));
        try { result.outcomes().clear(); throw new AssertionError("mutable movement outcomes"); }
        catch (UnsupportedOperationException expected) { }
    }

    private static void movementOutcomeIsExactAndFailClosed() {
        if (!SustainedRemoteWorldMultiplayerSession.class.isAssignableFrom(
                ResolvedMovementMultiplayerSession.class)) throw new AssertionError("resolved session hierarchy drifted");
        PlayerPose attempted = new PlayerPose(1.125D, 64D, 2D, 0F, 0F);
        PlayerPose original = new PlayerPose(1D, 64D, 2D, 0F, 0F);
        MovementOutcome accepted = new MovementOutcome(
                attempted, attempted, MovementDisposition.UNCHALLENGED);
        MovementOutcome corrected = new MovementOutcome(
                attempted, original, MovementDisposition.CORRECTED);
        equal(accepted, new MovementOutcome(attempted, attempted,
                MovementDisposition.UNCHALLENGED), "unchallenged movement outcome");
        if (accepted.corrected() || !corrected.corrected()
                || corrected.resulting() != original || corrected.attempted() != attempted)
            throw new AssertionError("movement outcome accessors drifted");
        failure(() -> new MovementOutcome(attempted, original, MovementDisposition.UNCHALLENGED));
    }

    private static void serverStateIsExactAndFailClosed() {
        ServerState state = new ServerState(ServerLifecycle.RUNNING, 25565, false, 6001L, 1);
        equal(state, new ServerState(ServerLifecycle.RUNNING, 25565, false, 6001L, 1),
                "server state");
        if (state.lifecycle() != ServerLifecycle.RUNNING || state.port() != 25565
                || state.onlineMode() || state.worldTime() != 6001L || state.completedSaves() != 1) {
            throw new AssertionError("server state accessors drifted");
        }
        failure(() -> new ServerState(ServerLifecycle.NEW, 0, false, -1L, 0));
        failure(() -> new ServerState(ServerLifecycle.RUNNING, 25565, false, -2L, 0));
        failure(() -> new ServerState(ServerLifecycle.STOPPED, 25565, false, 0L, -1));
    }

    private static void multiplayerStateIsExactAndFailClosed() {
        if (!IncrementalRemoteWorldMultiplayerSession.class.isAssignableFrom(
                SustainedRemoteWorldMultiplayerSession.class)) throw new AssertionError("sustained session hierarchy drifted");
        MultiplayerState state = new MultiplayerState(
                MultiplayerConnection.CONNECTED, "Worldline", 14, 7);
        equal(state, new MultiplayerState(MultiplayerConnection.CONNECTED, "Worldline", 14, 7),
                "multiplayer state");
        if (state.connection() != MultiplayerConnection.CONNECTED
                || !state.username().equals("Worldline") || state.protocolVersion() != 14
                || state.entityId() != 7) throw new AssertionError("multiplayer state accessors drifted");
        failure(() -> new MultiplayerState(MultiplayerConnection.NEW, "", 14, -1));
        failure(() -> new MultiplayerState(MultiplayerConnection.NEW, "Worldline", -1, -1));
        failure(() -> new MultiplayerState(MultiplayerConnection.CONNECTED, "Worldline", 14, -2));
    }

    private static void serverPlayerStateIsExactAndFailClosed() {
        ServerPlayerState state = new ServerPlayerState("Worldline", 0, 1.5D, 64.0D, -2.5D, 20, 0);
        equal(state, new ServerPlayerState("Worldline", 0, 1.5D, 64.0D, -2.5D, 20, 0),
                "server player state");
        if (!state.username().equals("Worldline") || state.dimension() != 0 || state.x() != 1.5D
                || state.y() != 64.0D || state.z() != -2.5D || state.health() != 20
                || state.yaw() != 0.0F || state.pitch() != 0.0F
                || state.inventoryItems() != 0) throw new AssertionError("player state accessors drifted");
        ServerPlayerState rotated = new ServerPlayerState(
                "Worldline", 0, 1.5D, 64.0D, -2.5D, 135.0F, -22.5F, 20, 0);
        if (rotated.yaw() != 135.0F || rotated.pitch() != -22.5F)
            throw new AssertionError("player rotation accessors drifted");
        equal(new PlayerPose(1.5D, 64.0D, -2.5D, 135.0F, -22.5F),
                new PlayerPose(1.5D, 64.0D, -2.5D, 135.0F, -22.5F), "player pose");
        RemoteChunkObservation chunk = new RemoteChunkObservation(-16, 0, 32, 16, 128, 16, 4096);
        equal(chunk, new RemoteChunkObservation(-16, 0, 32, 16, 128, 16, 4096),
                "remote chunk observation");
        if (chunk.width() != 16 || chunk.height() != 128 || chunk.depth() != 16
                || chunk.payloadBytes() != 4096) throw new AssertionError("chunk accessors drifted");
        failure(() -> new ServerPlayerState("../x", 0, 0, 0, 0, 20, 0));
        failure(() -> new ServerPlayerState("Worldline", 0, Double.NaN, 0, 0, 20, 0));
        failure(() -> new ServerPlayerState("Worldline", 0, 0, 0, 0, -1, 0));
        failure(() -> new PlayerPose(0, 0, 0, 0, 91));
        failure(() -> new RemoteChunkObservation(0, 0, 0, 0, 128, 16, 1));
        failure(() -> new RemoteChunkObservation(0, 0, 0, 16, 128, 16, 0));
    }

    private static void remoteChunkSnapshotIsImmutableAndAddressable() {
        RemoteChunkObservation region = new RemoteChunkObservation(32, 4, -16, 2, 2, 2, 32);
        byte[] ids = new byte[] {0, 1, 2, 3, 4, 5, 6, 7};
        byte[] metadata = new byte[] {0x10, 0x32, 0x54, 0x76};
        byte[] blockLight = new byte[] {0x21, 0x43, 0x65, (byte) 0x87};
        byte[] skyLight = new byte[] {(byte) 0xfe, (byte) 0xdc, (byte) 0xba, (byte) 0x98};
        RemoteChunkSnapshot snapshot = new RemoteChunkSnapshot(
                region, ids, metadata, blockLight, skyLight);
        ids[7] = 0; metadata[3] = 0; blockLight[3] = 0; skyLight[3] = 0;
        equal(snapshot.blockAt(1, 1, 1), new BlockState(7, 7), "remote block state");
        RemoteChunkSnapshot changed = snapshot.withBlock(1, 1, 1, new BlockState(20, 3));
        if (snapshot.blockCount() != 8 || snapshot.nonAirBlocks() != 7
                || snapshot.blockLightAt(1, 1, 1) != 8
                || snapshot.skyLightAt(1, 1, 1) != 9
                || !snapshot.blockAt(1, 1, 1).equals(new BlockState(7, 7))
                || !changed.blockAt(1, 1, 1).equals(new BlockState(20, 3)))
            throw new AssertionError("remote chunk snapshot accessors drifted");
        failure(() -> snapshot.blockAt(2, 0, 0));
        failure(() -> new RemoteChunkSnapshot(region, new byte[7], new byte[4],
                new byte[4], new byte[4]));
    }

    private static void remoteWorldViewIsSortedAndAddressable() {
        byte[] ids = new byte[32768]; ids[0] = 7;
        RemoteChunkSnapshot west = new RemoteChunkSnapshot(
                new RemoteChunkObservation(-16, 0, -16, 16, 128, 16, 1024), ids,
                new byte[16384], new byte[16384], new byte[16384]);
        RemoteChunkSnapshot east = new RemoteChunkSnapshot(
                new RemoteChunkObservation(0, 0, -16, 16, 128, 16, 1024), new byte[32768],
                new byte[16384], new byte[16384], new byte[16384]);
        RemoteWorldView view = new RemoteWorldView(java.util.Arrays.asList(east, west));
        if (view.loadedChunks() != 2 || !view.containsChunk(-1, -1)
                || view.chunks().get(0) != west || view.chunkAt(0, -1) != east
                || !view.blockAt(-16, 0, -16).equals(new BlockState(7, 0)))
            throw new AssertionError("remote world indexing drifted");
        failure(() -> view.chunkAt(1, 1));
        failure(() -> new RemoteWorldView(java.util.Arrays.asList(west, west)));
    }

    private static void valueEqualityIsExact() {
        equal(new BlockPosition(1, 2, 3), new BlockPosition(1, 2, 3), "block position");
        equal(new BlockState(20, 7), new BlockState(20, 7), "block state");
        equal(new GamePosition(1.25D, 2.5D, -3.75D),
                new GamePosition(1.25D, 2.5D, -3.75D), "game position");
        equal(new GameUiNode(GameUiNode.SLOT, "0", 0, -1, 0),
                new GameUiNode(GameUiNode.SLOT, "0", 0, -1, 0), "ui node");
    }

    private static void invalidValuesFailClosed() {
        failure(() -> new BlockState(-1, 0));
        failure(() -> new BlockState(1, 16));
        failure(() -> new GamePosition(Double.NaN, 0.0D, 0.0D));
        failure(() -> new GamePosition(0.0D, Double.POSITIVE_INFINITY, 0.0D));
        failure(() -> new GameUiNode("", "inventory", -1, -1, 0));
        failure(() -> new GameUiNode(GameUiNode.SLOT, "0", 0, -2, 0));
    }

    private static void snapshotsAreBoundedAndImmutable() {
        byte[] source = new byte[] {1, 2, 3};
        RuntimeSnapshot snapshot = RuntimeSnapshot.of(source);
        source[0] = 9;
        byte[] copy = snapshot.bytes();
        copy[1] = 9;
        equal(RuntimeSnapshot.of(new byte[] {1, 2, 3}), snapshot, "runtime snapshot");
        if (snapshot.size() != 3 || snapshot.bytes()[0] != 1 || snapshot.bytes()[1] != 2
                || !snapshot.sha256().equals(
                        "039058c6f2c0cb492c533b0a4d14ef77cc0f78abccced5287d84a1a2011cfb81")) {
            throw new AssertionError("runtime snapshot immutability failed");
        }
        failure(() -> RuntimeSnapshot.of(new byte[0]));
        failure(() -> RuntimeSnapshot.of(new byte[RuntimeSnapshot.MAX_BYTES + 1]));
    }

    private static void uiSpecRoundTripsBuilderAndInventory() {
        GameUiSpec inventory = GameUiSpec.inventory();
        if (!GameUiNode.INVENTORY.equals(inventory.screen()) || inventory.nodes().size() != 46
                || !inventory.matchesStructure(inventory.nodes())
                || inventory.node(GameUiNode.SLOT, "0").index() != 0) {
            throw new AssertionError("vanilla inventory spec failed");
        }
        GameUiSpec workbench = GameUiSpec.workbench();
        if (!GameUiNode.WORKBENCH.equals(workbench.screen()) || workbench.nodes().size() != 47
                || !workbench.matchesStructure(workbench.nodes())
                || workbench.node(GameUiNode.SLOT, "45").index() != 45) {
            throw new AssertionError("vanilla workbench spec failed");
        }
        java.util.List<GameUiSpec.Part> parts = java.util.Arrays.asList(
                new GameUiSpec.Part("slot", null, "input"),
                new GameUiSpec.Part("progress_arrow", null, null),
                new GameUiSpec.Part("slot", null, "output"),
                new GameUiSpec.Part("energy_bar", null, null),
                new GameUiSpec.Part("separator", null, null));
        GameUiSpec crusher = GameUiSpec.fromBuilder("crusher", parts);
        if (crusher.nodes().size() != 41 || crusher.node(GameUiNode.SLOT, "input").index() != 0
                || crusher.node(GameUiNode.SLOT, "output").index() != 1
                || crusher.node(GameUiNode.PROGRESS, "craft").index() != -1
                || crusher.node(GameUiNode.ENERGY, "energy").index() != -1
                || crusher.node(GameUiNode.SLOT, "player.0").index() != 2
                || !crusher.matchesStructure(crusher.nodes())) {
            throw new AssertionError("builder spec mapping failed: " + crusher.nodes());
        }
        failure(() -> GameUiSpec.fromBuilder("x", java.util.Collections.singletonList(
                new GameUiSpec.Part("unknown", null, null))));
        GameUiSpec declared = Ui.screen("crusher",
                Ui.row("process", Ui.slot("input"), Ui.progress("craft"), Ui.slot("output")),
                Ui.energy("energy"),
                Ui.playerInventory());
        equal(crusher, declared, "ui language");
        if (Ui.screen("bare", Ui.slot("input")).nodes().size() != 2) {
            throw new AssertionError("layout flatten or player opt-in failed");
        }
    }

    private static void itemCensusIsExactAndFailClosed() {
        ItemCensus iron = ItemCensus.of(265, 10);
        equal(iron.plus(50, 0), iron, "zero plus is identity");
        equal(ItemCensus.fromNodes(java.util.Arrays.asList(
                new GameUiNode(GameUiNode.SLOT, "0", 0, 265, 4),
                new GameUiNode(GameUiNode.SLOT, "1", 1, 265, 6))), iron, "node census");
        equal(iron.plus(ItemCensus.of(50, 2)), iron.plus(50, 2), "census merge");
        equal(iron.decrease(ItemCensus.of(265, 7)), ItemCensus.of(265, 3), "census decrease");
        equal(new ItemRecipe(ItemCensus.of(17, 1), ItemCensus.of(5, 4)),
                new ItemRecipe(ItemCensus.of(17, 1), ItemCensus.of(5, 4)), "item recipe");
        equal(EntityCensus.of("minecraft:zombie", 2),
                EntityCensus.of("minecraft:zombie", 1).plus("minecraft:zombie", 1), "entity census");
        equal(CauseDrop.death("minecraft:zombie", ItemCensus.of(288, 2)),
                CauseDrop.death("minecraft:zombie", ItemCensus.of(288, 2)), "cause drop");
        equal(new SpawnRule("block:2", "minecraft:pig", 4),
                new SpawnRule("block:2", "minecraft:pig", 4), "spawn rule");
        equal(EntityCensus.of("minecraft:pig", 1).plus(EntityCensus.of("minecraft:cow", 1)),
                EntityCensus.of("minecraft:pig", 1).plus("minecraft:cow", 1), "census merge");
        if (iron.total() != 10 || iron.count(265) != 10 || iron.exceeds(iron)
                || !iron.plus(265, 1).exceeds(iron) || ItemCensus.empty().exceeds(iron)
                || !iron.contains(ItemCensus.of(265, 10)) || iron.contains(ItemCensus.of(265, 11))) {
            throw new AssertionError("item census totals failed");
        }
        failure(() -> ItemCensus.of(-1, 1));
        failure(() -> ItemCensus.of(1, -1));
        failure(() -> new ItemRecipe(ItemCensus.empty(), ItemCensus.of(1, 1)));
        failure(() -> new InvariantViolation("", "detail"));
        failure(() -> new SpawnRule("", "minecraft:pig", 1));
        failure(() -> new SpawnRule("block:2", "minecraft:pig", 0));
    }

    private static void itemStackRecipePreservesMetadata() {
        RemoteItemStack white = new RemoteItemStack(35, 1, 0);
        RemoteItemStack red = new RemoteItemStack(351, 1, 1);
        RemoteItemStack redWool = new RemoteItemStack(35, 1, 14);
        ItemStackRecipe recipe = new ItemStackRecipe(
                java.util.Arrays.asList(red, white, red),
                java.util.Collections.singletonList(redWool));
        equal(recipe, new ItemStackRecipe(
                java.util.Arrays.asList(white, new RemoteItemStack(351, 2, 1)),
                java.util.Collections.singletonList(redWool)), "metadata recipe");
        if (!recipe.inputs().get(0).equals(white)
                || !recipe.inputs().get(1).equals(new RemoteItemStack(351, 2, 1))
                || !recipe.outputs().get(0).equals(redWool)) {
            throw new AssertionError("metadata recipe canonicalization failed");
        }
        unsupported(() -> recipe.inputs().add(white));
        failure(() -> new ItemStackRecipe(java.util.Collections.emptyList(), recipe.outputs()));
        failure(() -> new ItemStackRecipe(java.util.Arrays.asList(
                new RemoteItemStack(1, 127, 0), new RemoteItemStack(1, 1, 0)), recipe.outputs()));
    }

    private static void semanticMappingIsExactAndFailClosed() {
        SemanticMapping tick = SemanticMapping.of("tick", "CLIENT_TICK_ROOT",
                "net/minecraft/client/Minecraft", "method", "runTick", "()V",
                "INPUT,CLOCK", "WORLD,PLAYER,GUI", "CLOCK", "controlled-client-tick", 9998);
        equal(tick, SemanticMapping.of("tick", "CLIENT_TICK_ROOT",
                "net/minecraft/client/Minecraft", "method", "runTick", "()V",
                "INPUT,CLOCK", "WORLD,PLAYER,GUI", "CLOCK", "controlled-client-tick", 9998),
                "semantic mapping");
        SemanticMapping named = SemanticMapping.of("tick", "CLIENT_TICK_ROOT",
                "net/minecraft/client/Minecraft", "method", "runTick", "()V",
                "INPUT,CLOCK", "WORLD,PLAYER,GUI", "CLOCK", "controlled-client-tick", "k", 9998);
        if (!tick.known() || tick.confidence() != 9998 || tick.reads().size() != 2
                || !tick.canonical().contains("CLIENT_TICK_ROOT") || !tick.official().isEmpty()
                || !"k".equals(named.official())) {
            throw new AssertionError("semantic mapping fields failed");
        }
        failure(() -> SemanticMapping.of("", "CLIENT_TICK_ROOT",
                "net/minecraft/client/Minecraft", "method", "runTick", "()V",
                "", "", "", "lab-cycle", 9998));
        failure(() -> SemanticMapping.of("tick", "CLIENT_TICK_ROOT",
                "net/minecraft/client/Minecraft", "method", "runTick", "()V",
                "", "", "", "lab-cycle", 0));
    }

    private static void expandedSurfaceDefaultsFailClosed() {
        GameWorld world = new GameWorld() {
            @Override public long time() { return 0L; }
            @Override public BlockState block(BlockPosition position) { return new BlockState(1, 0); }
            @Override public boolean setBlock(BlockPosition position, BlockState state) { return true; }
            @Override public java.util.List<GameEntity> entities() { return java.util.Collections.emptyList(); }
            @Override public ItemCensus items() { return ItemCensus.empty(); }
            @Override public ItemCensus blocks() { return ItemCensus.empty(); }
        };
        GamePlayer player = new GamePlayer() {
            @Override public int id() { return 0; }
            @Override public String type() { return "minecraft:player"; }
            @Override public GamePosition position() { return new GamePosition(0, 0, 0); }
            @Override public boolean alive() { return true; }
            @Override public void teleport(GamePosition position) { }
            @Override public String username() { return "Worldline"; }
            @Override public int health() { return 20; }
            @Override public int selectedHotbarSlot() { return 0; }
            @Override public void selectHotbarSlot(int slot) { }
            @Override public ItemCensus items() { return ItemCensus.empty(); }
        };
        unsupported(() -> world.spawn("minecraft:pig", new GamePosition(0, 64, 0)));
        unsupported(() -> world.remove(player));
        unsupported(() -> world.itemsAt(new BlockPosition(8, 64, 8)));
        unsupported(() -> player.give(265, 1));
    }

    private static void unsupported(Runnable action) {
        try { action.run(); throw new AssertionError("expected unsupported failure"); }
        catch (UnsupportedOperationException expected) { }
    }
    private static void failure(Runnable action) {
        try { action.run(); throw new AssertionError("expected invalid value failure"); }
        catch (IllegalArgumentException expected) { }
    }

    private static void equal(Object expected, Object actual, String label) {
        if (!expected.equals(actual) || expected.hashCode() != actual.hashCode()) {
            throw new AssertionError(label + " equality contract failed");
        }
    }
}
