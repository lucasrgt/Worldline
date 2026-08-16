package worldline.api;

public final class DomainApiTest {
    private DomainApiTest() {}

    public static void main(String[] arguments) {
        valueEqualityIsExact();
        invalidValuesFailClosed();
        snapshotsAreBoundedAndImmutable();
        uiSpecRoundTripsBuilderAndInventory();
        System.out.println("DomainApiTest passed");
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
