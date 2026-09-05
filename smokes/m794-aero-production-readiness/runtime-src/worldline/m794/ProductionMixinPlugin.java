package worldline.m794;
import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
public final class ProductionMixinPlugin implements IMixinConfigPlugin {
  public void onLoad(String mixinPackage) {}
  public String getRefMapperConfig() {
    return null;
  }
  public boolean shouldApplyMixin(String target, String mixin) {
    return !mixin.contains(".Diagnostic") || Boolean.getBoolean("worldline.m794.diagnostic");
  }
  public void acceptTargets(Set<String> own, Set<String> other) {}
  public List<String> getMixins() {
    return null;
  }
  public void preApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
  public void postApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
}
