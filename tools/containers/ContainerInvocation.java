import java.nio.file.Path;
import java.util.List;

/** Builds the immutable Docker invocation for one canonical Gate worker. */
final class ContainerInvocation {
    private static final String LABEL = "dev.worldline.official-runtime=isolated";
    private ContainerInvocation() { }

    static List<String> command(String id, String milestone, String memory, String cpus, String heap,
            Path jar, String container, ContainerPoolContext.Context context, String image) {
        require(!jar.toString().contains(",") && !context.directory().toString().contains(","),
                "Docker bind paths may not contain a comma");
        String oracle = "type=bind,source=" + jar + ",target=/workspace/local/artifacts/"
                + "minecraft-b1.7.3-server.jar,readonly";
        String lease = "type=bind,source=" + context.directory()
                + ",target=/workspace/.worldline/runtime-fabric,readonly";
        return List.of("docker", "create", "--name", container, "--label", LABEL, "--label",
                "dev.worldline.smoke=" + id, "--init", "--network", "none", "--read-only",
                "--cap-drop", "ALL", "--security-opt", "no-new-privileges", "--pids-limit", "160",
                "--memory", memory, "--memory-swap", memory, "--cpus", cpus,
                "--ulimit", "nofile=1024:1024", "--tmpfs", "/tmp:rw,exec,nosuid,nodev,size=64m",
                "--tmpfs", "/runtime:rw,exec,nosuid,nodev,size=768m,uid=10001,gid=10001,mode=0700",
                "--mount", oracle, "--mount", lease,
                "--env", "JAVA_TOOL_OPTIONS=-XX:+UseSerialGC -Xms16m -Xmx" + heap
                        + " -Djava.io.tmpdir=/tmp", "--env", "WORLDLINE_CONTAINER_ISOLATED=1",
                "--env", "WORLDLINE_RUNTIME_POOL_FILE=/workspace/.worldline/runtime-fabric/lease.properties",
                "--env", "WORLDLINE_RUNTIME_POOL_TOKEN=" + context.secret(),
                "--env", "WORLDLINE_CONTAINER_HEAD=" + context.head(),
                "--env", "WORLDLINE_CONTAINER_TREE=" + context.tree(),
                "--env", "WORLDLINE_CONTAINER_IMAGE_ID=" + image,
                "--env", "WORLDLINE_TRACKED_FILES=/workspace/.worldline/runtime-fabric/tracked-files",
                "--env", "WORLDLINE_GATE_CONTROL=/runtime/control", "--env",
                "WORLDLINE_CONTROL_DIR=/runtime/locks", image,
                "tools/harness/Gate.java", "--milestone", milestone);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalArgumentException(message);
    }
}
