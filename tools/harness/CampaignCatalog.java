import java.nio.file.Path;
import java.util.List;

/** Closed campaign adapter registry shared by dispatch and local evidence views. */
record CampaignCatalog(String id, String main) {
    private static final List<CampaignCatalog> VALUES = List.of(
            new CampaignCatalog("nightly-quality", "NightlyQualityCampaign"),
            new CampaignCatalog("structural-instability", "StructuralInstabilityCampaign"),
            new CampaignCatalog("worldgen-matrix", "WorldGenerationCampaign"));

    static CampaignCatalog require(String id) {
        return VALUES.stream().filter(value -> value.id.equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unsupported campaign: " + id));
    }

    static List<Path> reportRoots(Path root) {
        return VALUES.stream().map(value -> root.resolve(".worldline/reports").resolve(value.id)).toList();
    }

    static void selfTest() {
        require(require("worldgen-matrix").main.equals("WorldGenerationCampaign"),
                "world-generation campaign registration drifted");
        require(VALUES.stream().map(CampaignCatalog::id).distinct().count() == VALUES.size(),
                "duplicate campaign adapter id");
        expectFailure(() -> require("unknown"));
    }

    private static void expectFailure(Runnable action) { try { action.run();
        throw new IllegalStateException("unknown campaign adapter was accepted");
    } catch (IllegalArgumentException expected) { } }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
}
