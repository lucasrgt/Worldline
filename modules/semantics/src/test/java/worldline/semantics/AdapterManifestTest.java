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
        require(manifests.size() == 1, "expected one adapter manifest");
        AdapterManifest b173 = manifests.get(0);
        require("b173-client".equals(b173.adapter()), "b173 adapter id");
        require(b173.sites().size() >= 28, "b173 site count");
        require(b173.render().contains("WORLD_SAVE="), "b173 lists WORLD_SAVE");
        Path temp = Files.createTempDirectory("worldline-adapter-semantics");
        try {
            Path manifest = temp.resolve("rogue").resolve("semantics").resolve("manifest.properties");
            Files.createDirectories(manifest.getParent());
            Files.write(manifest, ("schema=" + AdapterManifest.SCHEMA + "\n"
                    + "adapter=rogue\nowner.prefix=aero/modellib/\n"
                    + "site.1=worldline/b173/B173MemoryWorld#saveWorldInfo\n"
                    + "role.1=WORLD_SAVE\n").getBytes(StandardCharsets.UTF_8));
            failure(() -> AdapterManifest.load(manifest, catalog), "Aero prefix");
            Files.write(manifest, ("schema=" + AdapterManifest.SCHEMA + "\n"
                    + "adapter=rogue\nowner.prefix=worldline/rogue/\n"
                    + "site.1=worldline/rogue/SaveBudget#cap\nrole.1=SAVE_CHUNKS\n"
                    + "subject.1=aero/modellib/Aero_BECellIndex.markDirty\n")
                    .getBytes(StandardCharsets.UTF_8));
            failure(() -> AdapterManifest.load(manifest, catalog), "Aero subject");
        } finally {
            Files.deleteIfExists(temp.resolve("rogue/semantics/manifest.properties"));
            Files.deleteIfExists(temp.resolve("rogue/semantics"));
            Files.deleteIfExists(temp.resolve("rogue"));
            Files.deleteIfExists(temp);
        }
        System.out.println("AdapterManifestTest passed");
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
