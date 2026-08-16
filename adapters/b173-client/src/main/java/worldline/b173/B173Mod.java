package worldline.b173;

/** Stable b1.7.3 tick entrypoint implemented by a descriptor-selected mod JAR. */
@FunctionalInterface
public interface B173Mod {
    void onTick(B173ModContext context);
}
