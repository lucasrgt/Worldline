package worldline.modloader.testkit;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Constructs the pinned Java 8 launch command for one prepared legacy client. */
final class LegacyClientCommand {
    private LegacyClientCommand() {}

    static List<String> create(LegacyClientSettings settings, Path sessionRoot, Path artifact,
            int port, long seed, String session, String username) {
        String classpath = settings.classpath.stream().map(Path::toString)
                .collect(Collectors.joining(File.pathSeparator));
        List<String> command = new ArrayList<String>();
        add(command, settings.java.toString(), "-Xms128M", "-Xmx512M",
                "-Duser.home=" + sessionRoot,
                "-Djava.library.path=" + settings.natives,
                "-Dminecraft.launcher.brand=Worldline",
                "-Dminecraft.launcher.version=legacy-testkit-v1",
                "-Dworldline.legacy.testkit.loader=" + settings.loader,
                "-Dworldline.legacy.testkit.controlPort=" + port,
                "-Dworldline.legacy.testkit.session=" + session,
                "-Dworldline.legacy.testkit.world=worldline-" + session,
                "-Dworldline.legacy.testkit.seed=" + seed,
                "-Dworldline.profiler.enabled=true", "-Dworldline.profiler.autoStart=false",
                "-Dworldline.profiler.capacity=64",
                "-Dworldline.profiler.loader=" + settings.loader,
                "-Dworldline.profiler.scenario=legacy-testkit",
                "-Dworldline.profiler.output=" + artifact,
                "-cp", classpath, "org.mcphackers.launchwrapper.Launch",
                "--username", username, "--uuid", "-", "--session", "-",
                "--version", "b1.7.3", "--gameDir", sessionRoot.resolve("game").toString(),
                "--assetsDir", sessionRoot.resolve("assets").toString(), "--assetIndex", "b1.7",
                "--accessToken", "-", "--userProperties", "{}", "--userType", "legacy",
                "--versionType", "release", "--skinProxy", "pre-b1.9-pre4");
        return command;
    }

    private static void add(List<String> target, String... values) {
        java.util.Collections.addAll(target, values);
    }
}
