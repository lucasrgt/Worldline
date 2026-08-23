package worldline.semantics;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class AdapterManifestTest {
    private AdapterManifestTest() {}

    public static void main(String[] arguments) throws Exception {
        SemanticCatalog catalog = SemanticCatalog.standard();
        Path root = Paths.get("adapters");
        java.util.List<AdapterManifest> manifests = AdapterManifest.loadAll(root, catalog);
        require(manifests.size() == 4,
                "expected b173-client, b173-server, StationAPI, and Aero manifests");
        require(AdapterManifest.loadRepository(Paths.get(""), catalog).size() == 4,
                "repository load");
        AdapterManifest b173 = null, server = null, stationapi = null, aero = null;
        for (AdapterManifest manifest : manifests) {
            if ("b173-client".equals(manifest.adapter())) b173 = manifest;
            if ("b173-server".equals(manifest.adapter())) server = manifest;
            if ("stationapi".equals(manifest.adapter())) stationapi = manifest;
            if ("aero-model-lib".equals(manifest.adapter())) aero = manifest;
        }
        require(b173 != null && "driver".equals(b173.kind()) && b173.sites().size() >= 30,
                "b173 client adapter");
        require(b173.render().contains("WORLD_SAVE="), "b173 lists WORLD_SAVE");
        require(b173.render().contains("CONTAINER_SLOT_LIST="), "b173 lists Container.slots");
        require(b173.render().contains("SLOT_STACK="), "b173 lists Slot.getStack");
        require(server != null && "driver".equals(server.kind())
                && server.ownerPrefix().equals("worldline/b173server/"),
                "b173-server prefix");
        require(server.render().contains("PACKET13_PLAYER_LOOK_MOVE="), "server lists Packet13");
        require(server.render().contains("PACKET_STANCE="), "server lists stance");
        require(server.render().contains("PACKET50_PRECHUNK="), "server lists Packet50");
        require(server.render().contains("PACKET51_MAP_CHUNK="), "server lists Packet51");
        require(server.render().contains("PACKET52_MULTI_BLOCK_CHANGE="), "server lists Packet52");
        require(server.render().contains("PACKET53_BLOCK_CHANGE="), "server lists Packet53");
        require(server.render().contains("PACKET3_CHAT="), "server lists Packet3");
        require(server.render().contains("PACKET14_BLOCK_DIG="), "server lists Packet14");
        require(server.render().contains("PACKET5_PLAYER_INVENTORY="), "server lists Packet5");
        require(server.render().contains("PACKET7_USE_ENTITY="), "server lists Packet7");
        require(server.render().contains("PACKET8_UPDATE_HEALTH="), "server lists Packet8");
        require(server.render().contains("PACKET15_PLACE="), "server lists Packet15");
        require(server.render().contains("PACKET16_BLOCK_ITEM_SWITCH="), "server lists Packet16");
        require(server.render().contains("PACKET21_PICKUP_SPAWN="), "server lists Packet21");
        require(server.render().contains("PACKET22_COLLECT="), "server lists Packet22");
        require(server.render().contains("PACKET29_DESTROY_ENTITY="), "server lists Packet29");
        require(server.render().contains("PACKET38_ENTITY_STATUS="), "server lists Packet38");
        require(server.render().contains("PACKET100_OPEN_WINDOW="), "server lists Packet100");
        require(server.render().contains("PACKET101_CLOSE_WINDOW="), "server lists Packet101");
        require(server.render().contains("PACKET102_WINDOW_CLICK="), "server lists Packet102");
        require(server.render().contains("PACKET103_SET_SLOT="), "server lists Packet103");
        require(server.render().contains("PACKET104_WINDOW_ITEMS="), "server lists Packet104");
        require(server.render().contains("PACKET105_UPDATE_PROGRESSBAR="), "server lists Packet105");
        require(server.render().contains("PACKET106_TRANSACTION="), "server lists Packet106");
        require(server.render().contains("PACKET200_STATISTIC="), "server lists Packet200");
        require(server.sites().size() >= 26, "server intercept sites");
        require(stationapi != null && "driver".equals(stationapi.kind())
                && stationapi.ownerPrefix().equals("worldline/stationapi/")
                && stationapi.render().contains("MANUAL_TICK="), "StationAPI driver boundary");
        require(aero != null && "extension".equals(aero.kind())
                && aero.ownerPrefix().equals("worldline/aero/"), "aero prefix");
        require(aero.render().contains("kind=extension"), "aero render kind");
        require(aero.render().contains("SAVE_CHUNKS="), "aero lists SAVE_CHUNKS");
        require(aero.render().contains("COMPILE_CHUNKS="), "aero lists COMPILE_CHUNKS");
        require(aero.render().contains("LOAD_RENDERERS="), "aero lists LOAD_RENDERERS");
        require(aero.render().contains("CAMERA_RENDER="), "aero lists CAMERA_RENDER");
        require(aero.render().contains("ADD_VERTEX="), "aero lists ADD_VERTEX");
        require(aero.render().contains("CHUNK_REBUILD="), "aero lists CHUNK_REBUILD");
        require(!aero.render().contains("SORT_RENDERERS="), "aero omits SORT_RENDERERS");
        require(!aero.render().contains("MARK_BLOCKS_FOR_UPDATE="), "aero omits mark-blocks");
        require(!aero.render().contains("AMBIENT_DARKNESS="), "aero omits ambient");
        require(!aero.render().contains("CHUNK_INVALIDATE="), "aero omits invalidate");
        require(aero.sites().size() == 9, "aero intercept sites");
        Path temp = Files.createTempDirectory("worldline-adapter-semantics");
        try {
            Path manifest = temp.resolve("rogue").resolve("semantics").resolve("manifest.properties");
            Files.createDirectories(manifest.getParent());
            Files.write(manifest, body("rogue", "extension", "aero/modellib/",
                    "worldline/b173/B173MemoryWorld#saveWorldInfo", "WORLD_SAVE", "").getBytes(
                    StandardCharsets.UTF_8));
            failure(() -> AdapterManifest.load(manifest, catalog), "Aero prefix");
            Files.write(manifest, body("rogue", "extension", "worldline/aero/modellib/",
                    "worldline/aero/modellib/SaveBudget#cap", "SAVE_CHUNKS", "").getBytes(
                    StandardCharsets.UTF_8));
            failure(() -> AdapterManifest.load(manifest, catalog), "Aero nested prefix");
            Files.write(manifest, body("rogue", "extension", "worldline/rogue/",
                    "worldline/rogue/SaveBudget#cap", "SAVE_CHUNKS",
                    "aero/modellib/Aero_BECellIndex.markDirty").getBytes(StandardCharsets.UTF_8));
            failure(() -> AdapterManifest.load(manifest, catalog), "Aero subject");
            Files.write(manifest, body("rogue", "driver", "worldline/rogue/",
                    "worldline/rogue/SaveBudget#cap", "SAVE_CHUNKS", "").getBytes(
                    StandardCharsets.UTF_8));
            failure(() -> AdapterManifest.load(manifest, catalog), "unknown driver");
            Path extension = temp.resolve("worldline").resolve("extensions")
                    .resolve("sample-mod").resolve("manifest.properties");
            Files.createDirectories(extension.getParent());
            Files.write(extension, body("sample-mod", "extension", "worldline/sample/",
                    "worldline/sample/Probe#onTick", "CLIENT_TICK_ROOT",
                    "net/minecraft/client/Minecraft.runTick").getBytes(StandardCharsets.UTF_8));
            AdapterManifest loaded = AdapterManifest.load(extension, catalog);
            require("extension".equals(loaded.kind()) && loaded.sites().size() == 1,
                    "worldline/extensions layout");
        } finally {
            Files.deleteIfExists(temp.resolve("worldline/extensions/sample-mod/manifest.properties"));
            Files.deleteIfExists(temp.resolve("worldline/extensions/sample-mod"));
            Files.deleteIfExists(temp.resolve("worldline/extensions"));
            Files.deleteIfExists(temp.resolve("worldline"));
            Files.deleteIfExists(temp.resolve("rogue/semantics/manifest.properties"));
            Files.deleteIfExists(temp.resolve("rogue/semantics"));
            Files.deleteIfExists(temp.resolve("rogue"));
            Files.deleteIfExists(temp);
        }
        System.out.println("AdapterManifestTest passed");
    }

    private static String body(String adapter, String kind, String prefix, String site,
            String role, String subject) {
        String text = "schema=" + AdapterManifest.SCHEMA + "\nadapter=" + adapter
                + "\nkind=" + kind + "\nowner.prefix=" + prefix + "\nsite.1=" + site
                + "\nrole.1=" + role + "\n";
        if (!subject.isEmpty()) text += "subject.1=" + subject + "\n";
        return text;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void failure(Checked action, String label) {
        try { action.run(); throw new AssertionError("expected fail-closed " + label); }
        catch (IllegalArgumentException expected) { }
        catch (Exception error) { throw new AssertionError(label, error); }
    }

    private interface Checked { void run() throws Exception; }
}
