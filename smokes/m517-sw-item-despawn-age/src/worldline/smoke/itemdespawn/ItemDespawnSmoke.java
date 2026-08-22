package worldline.smoke.itemdespawn;
import java.nio.file.Paths;import worldline.api.MinecraftRuntime;import worldline.api.WorldSource;import worldline.kernel.ControlledMinecraftRuntime;import worldline.trace.CanonicalTrace;
/** Freezes the exact item age expiry boundary and collection alternative. */
public final class ItemDespawnSmoke{
 private static final long SEED=51720240821L;private ItemDespawnSmoke(){}public static void main(String[] a){CanonicalTrace t=new CanonicalTrace(SEED);expiry(t);live(t);collection(t);t.emitTo(System.out);}private static MinecraftRuntime open(ItemDespawnBackend b,String n){MinecraftRuntime r=new ControlledMinecraftRuntime(b);r.bootHeadless();r.loadWorld(WorldSource.at(Paths.get("memory",n)));return r;}
 private static void expiry(CanonicalTrace t){ItemDespawnBackend b=new ItemDespawnBackend(SEED);MinecraftRuntime r=open(b,"expiry");try{b.seed(5998);b.snapshot(t,"expiry-seed");r.tick();b.requireState(5999,false,true);b.snapshot(t,"expiry-5999");r.tick();b.requireState(6000,true,false);b.snapshot(t,"expiry-6000");}finally{r.close();}}
 private static void live(CanonicalTrace t){ItemDespawnBackend b=new ItemDespawnBackend(SEED);MinecraftRuntime r=open(b,"live");try{b.seed(100);r.tick();r.tick();b.requireState(102,false,true);b.snapshot(t,"live-102");}finally{r.close();}}
 private static void collection(CanonicalTrace t){ItemDespawnBackend b=new ItemDespawnBackend(SEED);MinecraftRuntime r=open(b,"collection");try{b.seed(100);b.collect();b.requireState(100,true,false);b.snapshot(t,"collected");}finally{r.close();}}
}
