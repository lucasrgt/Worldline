package worldline.b173;

/** Stable entrypoint implemented by an isolated b1.7.3 benchmark mod JAR. */
@FunctionalInterface
public interface B173Mod {
    void onTick(B173ModContext context);
}
