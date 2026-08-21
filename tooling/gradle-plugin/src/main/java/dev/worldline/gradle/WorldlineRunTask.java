package dev.worldline.gradle;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

/** Isolated runner process used by all Worldline Gradle commands. */
public abstract class WorldlineRunTask extends JavaExec {
    @Input public abstract Property<String> getWorldlineCommand();
    @Input public abstract Property<String> getProviderName();
    @Input public abstract Property<Long> getSeed();
    @Input public abstract Property<Boolean> getNoRuntime();
    @Input public abstract Property<Boolean> getUpdateSnapshots();
    @InputDirectory @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getSpecClasses();
    @InputFiles @Optional @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getProductClasspath();
    @InputFiles @Optional @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getModFiles();
    @Internal public abstract RegularFileProperty getRuntimeLock();
    @Internal public abstract DirectoryProperty getArtifacts();
    @Internal public abstract DirectoryProperty getSnapshots();
    @Internal public abstract RegularFileProperty getJunitReport();

    @TaskAction @Override public void exec() {
        List<String> arguments = new ArrayList<>();
        arguments.add("test"); arguments.add(getWorldlineCommand().get());
        arguments.add(getSpecClasses().get().getAsFile().getAbsolutePath());
        List<File> products = getProductClasspath().getFiles().stream()
                .sorted(Comparator.comparing(File::getAbsolutePath)).collect(Collectors.toList());
        if (!products.isEmpty()) arguments.add("--classpath=" + products.stream()
                .map(File::getAbsolutePath).collect(Collectors.joining(File.pathSeparator)));
        List<File> mods = getModFiles().getFiles().stream()
                .sorted(Comparator.comparing(File::getAbsolutePath)).collect(Collectors.toList());
        if (mods.size() > 1) throw new GradleException("Worldline 0.x accepts exactly one mod artifact");
        if (!mods.isEmpty()) arguments.add("--mod=" + mods.get(0).getAbsolutePath());
        if (getNoRuntime().get()) arguments.add("--no-runtime");
        else arguments.add("--provider=" + getProviderName().get());
        arguments.add("--seed=" + getSeed().get());
        arguments.add("--runtime-lock=" + getRuntimeLock().get().getAsFile().getAbsolutePath());
        arguments.add("--artifacts=" + getArtifacts().get().getAsFile().getAbsolutePath());
        arguments.add("--snapshots=" + getSnapshots().get().getAsFile().getAbsolutePath());
        if (getWorldlineCommand().get().equals("run")) {
            arguments.add("--reporter=default,agent,junit");
            arguments.add("--junit=" + getJunitReport().get().getAsFile().getAbsolutePath());
        }
        if (getUpdateSnapshots().get()) arguments.add("--update-snapshots");
        setArgs(arguments); super.exec();
    }
}
