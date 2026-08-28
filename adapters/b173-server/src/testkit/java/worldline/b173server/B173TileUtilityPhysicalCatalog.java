package worldline.b173server;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.BlockState;

/** Canonical placed states for opaque tile-backed utility blocks. */
public final class B173TileUtilityPhysicalCatalog {
    private static final List<Subject> SUBJECTS = Collections.unmodifiableList(Arrays.asList(
            new Subject(23, "dispenser", 2,
                    "dispenser", "container", "directional", "tile-entity"),
            new Subject(25, "note-block", 0,
                    "note-block", "tile-entity", "redstone-component"),
            new Subject(52, "mob-spawner", 0, "mob-spawner", "tile-entity"),
            new Subject(54, "chest", 0,
                    "chest", "container", "multi-block", "directional", "tile-entity"),
            new Subject(61, "furnace", 2,
                    "furnace", "container", "directional", "tile-entity", "stateful-metadata"),
            new Subject(84, "jukebox", 0, "jukebox", "tile-entity")));

    private B173TileUtilityPhysicalCatalog() { }

    public static List<Subject> subjects() { return SUBJECTS; }

    public static final class Subject {
        final int id, placedMetadata;
        final String name;
        final List<String> archetypes;

        Subject(int id, String name, int placedMetadata, String... archetypes) {
            this.id = id; this.name = name; this.placedMetadata = placedMetadata;
            this.archetypes = Collections.unmodifiableList(Arrays.asList(archetypes));
        }

        String subject() { return String.format("b1.7.3:block/%03d", id); }
        String scenario() { return name + "-tile-utility-physical-envelope"; }
        BlockState state() { return new BlockState(id, placedMetadata); }
    }
}
