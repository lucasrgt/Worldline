package worldline.m793;
import java.lang.reflect.Method;
import net.minecraft.client.render.Tessellator;
import net.modificationstation.stationapi.mixin.render.client.TessellatorAccessor;
/** Observes the optional candidate without adding it to the immutable baseline. */
final class ProductionCandidate {
    private static final Method MATCHES = method("matchedThisFrame"), BYTES = method("bufferBytes");
    private ProductionCandidate() {}
    static long matches() { return MATCHES == null ? 0 : call(MATCHES); }
    static long bytes() {
        return BYTES == null ? ((TessellatorAccessor) (Object) Tessellator.INSTANCE)
            .stationapi$getBufferSize() * 4L : call(BYTES);
    }
    private static Method method(String name) {
        try { return Class.forName("aero.modellib.Aero_MultiPoseReuse").getMethod(name); }
        catch (ClassNotFoundException absent) { return null; }
        catch (Exception error) { throw new IllegalStateException(error); }
    }
    private static long call(Method method) {
        try { return ((Number) method.invoke(null)).longValue(); }
        catch (Exception error) { throw new IllegalStateException(error); }
    }
}
