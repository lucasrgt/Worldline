package worldline.b173server;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import worldline.api.BlockFace;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;
import worldline.testapi.BlockConformancePlan;
import worldline.testapi.BlockConformanceProfile;
import worldline.testapi.BlockConformanceTemplate;
import worldline.testapi.BlockLifecycleSlot;
import worldline.testapi.BlockLightExpectation;
import worldline.testapi.BlockLightPlacement;
import worldline.testapi.BlockLightProbe;
import worldline.testapi.BlockLightScenario;
import worldline.testapi.ConformanceLayer;

/** Official b1.7.3 static skylight attenuation and block-light propagation rows. */
public final class B173LightScenarioFactory {
    private static final int HOTBAR = 1;
    private static final int INVENTORY = 37;
    private static final BlockState AIR = new BlockState(0, 0);

    private B173LightScenarioFactory() { }

    public static List<BlockLightScenario> staticTransportFamily() {
        return Collections.unmodifiableList(Arrays.asList(
                glass(), leaves(), ice(), torch(), glowstone(), redstoneTorch(), jackOLantern()));
    }

    public static BlockLightScenario glass() {
        return sky("glass-skylight-transparency", "b1.7.3:block/020", 20, 0,
                new BlockState(20, 0), 15, Arrays.asList("transparent", "simple-solid"));
    }
    public static BlockLightScenario leaves() {
        return sky("leaves-skylight-attenuation", "b1.7.3:block/018", 18, 0,
                new BlockState(18, 8), 14, Arrays.asList("translucent", "stateful-metadata"));
    }
    public static BlockLightScenario ice() {
        return sky("ice-skylight-attenuation", "b1.7.3:block/079", 79, 0,
                new BlockState(79, 0), 12, Arrays.asList("translucent", "simple-solid"));
    }
    public static BlockLightScenario torch() {
        return emission("torch-block-light-propagation", "b1.7.3:block/050", 50, 0,
                new BlockState(50, 5), 14,
                Arrays.asList("luminous", "support-dependent", "directional"), 0F);
    }
    public static BlockLightScenario glowstone() {
        return emission("glowstone-block-light-propagation", "b1.7.3:block/089", 89, 0,
                new BlockState(89, 0), 15, Arrays.asList("luminous", "simple-solid"), 0F);
    }
    public static BlockLightScenario redstoneTorch() {
        return emission("redstone-torch-block-light-propagation", "b1.7.3:block/076", 76, 0,
                new BlockState(76, 5), 7,
                Arrays.asList("luminous", "support-dependent", "redstone"), 0F);
    }
    public static BlockLightScenario jackOLantern() {
        return emission("jack-o-lantern-block-light-propagation", "b1.7.3:block/091", 91, 0,
                new BlockState(91, 2), 15,
                Arrays.asList("luminous", "directional", "simple-solid"), 0F);
    }

    private static BlockLightScenario sky(String id, String subject, int item, int damage,
            BlockState state, int sky, List<String> archetypes) {
        return scenario(id, subject, item, damage, state, archetypes, 0F,
                Collections.singletonList(new BlockLightProbe("source", B173LightArena.SOURCE,
                        expectation(AIR, 0, 15), expectation(state, 0, sky))));
    }
    private static BlockLightScenario emission(String id, String subject, int item, int damage,
            BlockState state, int level, List<String> archetypes, float yaw) {
        return scenario(id, subject, item, damage, state, archetypes, yaw, Arrays.asList(
                new BlockLightProbe("source", B173LightArena.SOURCE,
                        expectation(AIR, 0, 15), expectation(state, level,
                                BlockLightExpectation.ANY_LIGHT)),
                new BlockLightProbe("distance-1", B173LightArena.NEAR,
                        expectation(AIR, 0, 15), expectation(AIR, level - 1, 15)),
                new BlockLightProbe("distance-2", B173LightArena.FAR,
                        expectation(AIR, 0, 15), expectation(AIR, level - 2, 15))));
    }
    private static BlockLightScenario scenario(String id, String subject, int item, int damage,
            BlockState state, List<String> archetypes, float yaw, List<BlockLightProbe> probes) {
        BlockConformancePlan plan = new BlockConformancePlan(Collections.singletonList(
                new BlockConformanceProfile(subject, archetypes, false,
                        Collections.<String, ConformanceLayer>emptyMap())),
                Collections.singletonList(new BlockConformanceTemplate(
                        "light-behavior", ConformanceLayer.ARCHETYPE)));
        return new BlockLightScenario(id, plan.caseFor(subject, "light-behavior"),
                new BlockLifecycleSlot(HOTBAR, INVENTORY,
                        new RemoteItemStack(item, 1, damage), null), yaw, 0F,
                Collections.singletonList(new BlockLightPlacement(B173LightArena.SOURCE_SUPPORT,
                        BlockFace.UP, state)), probes);
    }
    private static BlockLightExpectation expectation(BlockState state, int block, int sky) {
        return new BlockLightExpectation(state, block, sky);
    }
}
