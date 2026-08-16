package worldline.b173;

/** Alternative intervention applied to a restored checkpoint. */
@FunctionalInterface
public interface B173Hypothesis {
    void apply(B173Runtime runtime);
}
