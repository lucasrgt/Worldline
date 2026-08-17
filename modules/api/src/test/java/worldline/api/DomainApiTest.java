package worldline.api;

public final class DomainApiTest {
    private DomainApiTest() {}

    public static void main(String[] arguments) {
        valueEqualityIsExact();
        invalidValuesFailClosed();
        snapshotsAreBoundedAndImmutable();
        uiSpecRoundTripsBuilderAndInventory();
        itemCensusIsExactAndFailClosed();
        semanticMappingIsExactAndFailClosed();
        serverStateIsExactAndFailClosed();
        multiplayerStateIsExactAndFailClosed();
        serverPlayerStateIsExactAndFailClosed();
        System.out.println("DomainApiTest passed");
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
                || state.inventoryItems() != 0) throw new AssertionError("player state accessors drifted");
        failure(() -> new ServerPlayerState("../x", 0, 0, 0, 0, 20, 0));
        failure(() -> new ServerPlayerState("Worldline", 0, Double.NaN, 0, 0, 20, 0));
        failure(() -> new ServerPlayerState("Worldline", 0, 0, 0, 0, -1, 0));
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
