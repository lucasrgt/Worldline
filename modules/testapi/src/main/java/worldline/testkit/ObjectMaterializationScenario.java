package worldline.testkit;

import worldline.api.RemoteObjectSpawn;

/** Driver-neutral Packet23 materialization boundary. */
public interface ObjectMaterializationScenario {
    RemoteObjectSpawn materialize(int expectedType);
}
