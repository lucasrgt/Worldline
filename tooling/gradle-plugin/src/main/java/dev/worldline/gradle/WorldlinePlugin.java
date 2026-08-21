package dev.worldline.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.JavaCompile;

/** Published binary plugin; Gradle is an adapter over the independent TestKit runner. */
public final class WorldlinePlugin implements Plugin<Project> {
    public static final String VERSION = "0.2.1";
    @Override public void apply(Project project) {
        project.getPluginManager().apply(JavaPlugin.class);
        WorldlineExtension extension = project.getExtensions().create(
                "worldline", WorldlineExtension.class);
        applyProjectConfig(project, extension);
        SourceSetContainer sources = project.getExtensions().getByType(SourceSetContainer.class);
        SourceSet tests = sources.create("worldlineTest", source -> {
            source.getJava().setSrcDirs(java.util.Collections.singletonList("src/test/java"));
            source.getResources().setSrcDirs(java.util.Collections.singletonList("src/test/resources"));
        });
        Configuration implementation = project.getConfigurations().getByName(
                tests.getImplementationConfigurationName());
        tests.setCompileClasspath(tests.getCompileClasspath().plus(extension.getProductClasspath()));
        tests.setRuntimeClasspath(tests.getRuntimeClasspath().plus(extension.getProductClasspath()));
        implementation.defaultDependencies(dependencies -> dependencies.add(project.getDependencies().create(
                project.files(WorldlineDistribution.artifact(project, "api", WorldlineDistribution.API_SHA256)))));
        Configuration runner = project.getConfigurations().create("worldlineTestRunner", value -> {
            value.setCanBeConsumed(false); value.setCanBeResolved(true);
            value.defaultDependencies(dependencies -> dependencies.add(project.getDependencies().create(
                    project.files(WorldlineDistribution.artifact(project, "runner",
                            WorldlineDistribution.RUNNER_SHA256)))));
        });
        project.getTasks().named(tests.getCompileJavaTaskName(), JavaCompile.class).configure(task -> {
            task.getOptions().getRelease().set(extension.getJavaRelease());
            task.getOptions().getCompilerArgs().addAll(java.util.Arrays.asList("-Xlint:all,-options", "-Werror"));
        });
        registerRun(project, extension, tests, runner, "worldlineTest", "run");
        registerRun(project, extension, tests, runner, "worldlineTestList", "list");
        registerRun(project, extension, tests, runner, "worldlineTestInspect", "inspect");
        registerRun(project, extension, tests, runner, "worldlineTestWatch", "watch");
        registerRun(project, extension, tests, runner, "worldlineTestMinimize", "minimize");
        registerRun(project, extension, tests, runner, "worldlineTestUpdateSnapshots", "run", true);
        project.getTasks().register("worldlineDoctor", WorldlineDoctorTask.class, task -> task.setGroup("verification"));
        project.getTasks().register("worldlineVerifyOracle", WorldlineVerifyOracleTask.class, task ->
                task.setGroup("verification"));
        project.getTasks().register("worldlineAcquireRuntime", WorldlineAcquireTask.class, task ->
                task.setGroup("worldline"));
        project.getTasks().register("worldlineConfigure", WorldlineConfigureTask.class, task ->
                task.setGroup("worldline"));
        project.getTasks().register("worldlineMappings", org.gradle.api.tasks.JavaExec.class, task -> {
            task.setGroup("worldline"); task.setClasspath(runner);
            task.getMainClass().set("worldline.cli.WorldlineCli"); task.args("semantics", "show");
        });
        project.getTasks().named("check").configure(task -> task.dependsOn("worldlineTest"));
        project.afterEvaluate(ignored -> {
            WorldlineDiscovery.apply(project, extension); OraclePaths paths = OraclePaths.resolve(project, extension);
            project.getTasks().named("worldlineDoctor", WorldlineDoctorTask.class).configure(task -> task.configure(
                    project.getProjectDir().toPath().resolve("src/test/java"),
                    project.getRootDir().toPath().toAbsolutePath().normalize(), paths,
                    !extension.getNoRuntime().get()));
        });
    }
    private static void registerRun(Project project, WorldlineExtension extension, SourceSet tests,
            Configuration runner, String name, String command) {
        registerRun(project, extension, tests, runner, name, command, false);
    }
    private static void registerRun(Project project, WorldlineExtension extension, SourceSet tests,
            Configuration runner, String name, String command, boolean update) {
        project.getTasks().register(name, WorldlineRunTask.class, task -> {
            task.setGroup("verification"); task.dependsOn(tests.getClassesTaskName());
            task.getWorldlineCommand().set(command); task.getProviderName().set(extension.getProvider());
            task.getSeed().set(extension.getSeed()); task.getNoRuntime().set(extension.getNoRuntime());
            task.getUpdateSnapshots().set(update);
            task.getSpecClasses().set(project.getLayout().getBuildDirectory().dir(
                    "classes/java/" + tests.getName()));
            task.getProductClasspath().from(extension.getProductClasspath());
            task.getModFiles().from(extension.getModFiles());
            task.getRuntimeLock().set(project.getLayout().getProjectDirectory()
                    .dir(".local").file("official-runtime.lock"));
            task.getArtifacts().set(project.getLayout().getBuildDirectory().dir("worldline/results"));
            task.getSnapshots().set(project.getLayout().getProjectDirectory().dir("snapshots"));
            task.getJunitReport().set(project.getLayout().getBuildDirectory()
                    .file("test-results/worldlineTest/TEST-worldline.xml"));
            task.setClasspath(runner.plus(tests.getRuntimeClasspath()).plus(extension.getProductClasspath()));
            task.getMainClass().set("worldline.cli.WorldlineCli");
        });
    }
    private static void applyProjectConfig(Project project, WorldlineExtension extension) {
        WorldlineConfig config = WorldlineConfig.read(project.getProjectDir().toPath().resolve("worldline.toml"));
        extension.getRuntime().set(config.value("runtime", "b1.7.3"));
        extension.getOracleProfile().set(config.value("profile", "b173-local"));
        extension.getNoRuntime().set(Boolean.parseBoolean(config.value("noRuntime", "false")));
    }
}
