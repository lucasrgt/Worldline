package dev.worldline.gradle;

import javax.inject.Inject;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

/** Lazy public configuration for the Worldline Gradle integration. */
public class WorldlineExtension {
    private final Property<String> runtime;
    private final Property<String> oracleProfile;
    private final Property<String> provider;
    private final Property<Long> seed;
    private final Property<Integer> javaRelease;
    private final Property<Boolean> noRuntime;
    private final ConfigurableFileCollection productClasspath;
    private final ConfigurableFileCollection modFiles;

    @Inject public WorldlineExtension(ObjectFactory objects) {
        runtime = objects.property(String.class).convention("b1.7.3");
        oracleProfile = objects.property(String.class).convention("b173-local");
        provider = objects.property(String.class).convention("worldline.b173.B173TestRuntimeProvider");
        seed = objects.property(Long.class).convention(173L);
        javaRelease = objects.property(Integer.class).convention(8);
        noRuntime = objects.property(Boolean.class).convention(false);
        productClasspath = objects.fileCollection(); modFiles = objects.fileCollection();
    }
    public Property<String> getRuntime() { return runtime; }
    public Property<String> getOracleProfile() { return oracleProfile; }
    public Property<String> getProvider() { return provider; }
    public Property<Long> getSeed() { return seed; }
    public Property<Integer> getJavaRelease() { return javaRelease; }
    public Property<Boolean> getNoRuntime() { return noRuntime; }
    public ConfigurableFileCollection getProductClasspath() { return productClasspath; }
    public ConfigurableFileCollection getModFiles() { return modFiles; }
}
