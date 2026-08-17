package worldline.optimization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class OptimizationRefTest {
    public static void main(String[] arguments) throws Exception {
        Retention retention = OptimizationRef.class.getAnnotation(Retention.class);
        if (retention == null || retention.value() != RetentionPolicy.SOURCE)
            throw new AssertionError("OptimizationRef must be source-only");
        Target target = OptimizationRef.class.getAnnotation(Target.class);
        Set<ElementType> expected = new HashSet<ElementType>(Arrays.asList(ElementType.TYPE,
                ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD));
        if (target == null || !expected.equals(new HashSet<ElementType>(Arrays.asList(target.value()))))
            throw new AssertionError("OptimizationRef targets drifted");
        if (OptimizationRef.class.getMethod("value").getReturnType() != String[].class)
            throw new AssertionError("OptimizationRef value must be String[]");
        System.out.println("OptimizationRefTest passed");
    }
}
