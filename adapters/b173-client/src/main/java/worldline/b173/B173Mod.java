package worldline.b173;

/** Stable b1.7.3 entrypoint implemented by a descriptor-selected mod JAR. */
@FunctionalInterface
public interface B173Mod {
    void onTick(B173ModContext context);

    /** Called once when the mod is installed into a loaded controlled world. */
    default void onLoad(B173ModContext context) {}

    /** Called once in reverse install order before the runtime closes. */
    default void onDispose() {}
}
