package worldline.b173server;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.BlockState;
import worldline.testkit.BlockCollisionExpectation;
import worldline.testkit.BlockCollisionProbe;

/** Caller-owned special-collision subjects and official AABB probes. */
final class B173SpecialCollisionPhysicalCatalog {
    static final BlockState STONE = new BlockState(1, 0);
    static final BlockState SAND = new BlockState(12, 0);

    private B173SpecialCollisionPhysicalCatalog() {
    }

    static Subject[] subjects() {
        return new Subject[] {cobweb(), snowLayer(), cactus(), soulSand()};
    }

    private static Subject cobweb() {
        return new Subject(30, "cobweb",
                Arrays.asList("transparent-solid", "special-collision"), STONE, 14,
                Collections.singletonList(new BlockCollisionProbe("level", 0D, 0D, 1D, 1,
                        BlockCollisionExpectation.PASSABLE)));
    }

    private static Subject snowLayer() {
        return new Subject(78, "snow-layer",
                Arrays.asList("support-dependent", "stateful-metadata", "special-collision"),
                STONE, 15, Collections.singletonList(passable("level", 0D, 1D)));
    }

    private static Subject cactus() {
        return new Subject(81, "cactus",
                Arrays.asList("vegetation", "random-tick", "special-collision"), SAND, 15,
                Arrays.asList(blocked("level", 0D, 1D), passable("inset-gap", 0D, 0.25D),
                        passable("surface", 0.9375D, 1D)));
    }

    private static Subject soulSand() {
        return new Subject(88, "soul-sand",
                Arrays.asList("special-collision", "simple-solid"), STONE, 0,
                Arrays.asList(blocked("level", 0D, 1D), blocked("half-step", 0.5D, 1D),
                        passable("surface", 0.9375D, 1D)));
    }

    private static BlockCollisionProbe blocked(String id, double rise, double travel) {
        return new BlockCollisionProbe(id, 0D, rise, travel, 10,
                BlockCollisionExpectation.BLOCKED);
    }

    private static BlockCollisionProbe passable(String id, double rise, double travel) {
        return new BlockCollisionProbe(id, 0D, rise, travel, 10,
                BlockCollisionExpectation.PASSABLE);
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
            this.id = id;
            this.name = name;
            this.archetypes = archetypes;
            this.support = support;
            this.sky = sky;
            this.probes = probes;
        }

        String subject() {
            return String.format("b1.7.3:block/%03d", id);
        }

        String scenario() {
            return name + "-static-physical-envelope";
        }

        BlockState state() {
            return new BlockState(id, 0);
        }
    }
}
