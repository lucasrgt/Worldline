package worldline.b173server;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.BlockState;
import worldline.testapi.BlockCollisionExpectation;
import worldline.testapi.BlockCollisionProbe;

/** Caller-owned dirt- and sand-supported small-flora physical envelopes. */
final class B173GroundFloraPhysicalCatalog {
    static final BlockState DIRT = new BlockState(3, 0);
    static final BlockState SAND = new BlockState(12, 0);

    private B173GroundFloraPhysicalCatalog() {
    }

    static Subject[] subjects() {
        return new Subject[] {dandelion(), rose(), tallGrass(), deadBush()};
    }

    static Subject[] singletonStateSubjects() {
        return new Subject[] {dandelion(), rose(), deadBush()};
    }

    private static Subject dandelion() {
        return subject(37, "dandelion", DIRT, false);
    }

    private static Subject rose() {
        return subject(38, "rose", DIRT, false);
    }

    private static Subject tallGrass() {
        return subject(31, "tall-grass", DIRT, true);
    }

    private static Subject deadBush() {
        return subject(32, "dead-bush", SAND, false);
    }

    private static Subject subject(int id, String name, BlockState support, boolean stateful) {
        List<String> archetypes = stateful
                ? Arrays.asList("vegetation", "support-dependent", "stateful-metadata")
                : Arrays.asList("vegetation", "support-dependent");
        return new Subject(id, name, archetypes, support, 15,
                Collections.singletonList(new BlockCollisionProbe("level", 0D, 0D, 1D, 10,
                        BlockCollisionExpectation.PASSABLE)));
    }

    static final class Subject {
        final int id;
        final String name;
        final List<String> archetypes;
        final BlockState support;
        final int sky;
        final List<BlockCollisionProbe> probes;

        Subject(int id, String name, List<String> archetypes, BlockState support, int sky,
                List<BlockCollisionProbe> probes) {
            this.id = id; this.name = name; this.archetypes = archetypes;
            this.support = support; this.sky = sky; this.probes = probes;
        }

        String subject() { return String.format("b1.7.3:block/%03d", id); }
        String scenario() { return name + "-static-physical-envelope"; }
        BlockState state() { return new BlockState(id, 0); }
    }
}
