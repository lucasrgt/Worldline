package worldline.testkit;
import worldline.testapi.MobSpawnerSubsystemEvidence;
import worldline.testapi.MobSpawnerSubsystemFixture;
import worldline.testapi.MobSpawnerSubsystemObservation;

/** Locks the public mob-spawner mini-subsystem contract. */
public final class MobSpawnerSubsystemFixtureTest {
    private MobSpawnerSubsystemFixtureTest() { }
    public static void execute() {
        MobSpawnerSubsystemObservation observation = new MobSpawnerSubsystemObservation(
                "block=52:BlockMobSpawner,item=52:ItemBlock,tile=TileEntityMobSpawner",
                "item=52x1->0,placed=52:0,tile=Pig:20",
                "break=52:0->0:0,strength=finite,drops=none",
                "chunk-nbt=52:0+Zombie:37",
                "scheduled=F,out-of-range=20,near-player=19",
                "stone+lever=stable-52:0+Pig:20");
        MobSpawnerSubsystemEvidence first = MobSpawnerSubsystemFixture.execute(() -> observation);
        MobSpawnerSubsystemEvidence second = MobSpawnerSubsystemFixture.execute(() -> observation);
        require(first.equals(second) && first.hashCode() == second.hashCode(),
                "mob-spawner evidence equality drifted");
        require(first.canonical().contains("claims=7|")
                && first.canonical().contains("chunk-nbt=52:0+Zombie:37"),
                "mob-spawner evidence inventory drifted");
        System.out.println("mob-spawner subsystem fixture tests passed");
    }
    private static void require(boolean value, String message) {
        if (!value)
            throw new AssertionError(message);
    }
}
