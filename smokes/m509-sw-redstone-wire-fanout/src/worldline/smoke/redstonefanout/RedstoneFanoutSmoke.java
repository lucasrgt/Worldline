package worldline.smoke.redstonefanout;
import java.nio.file.Paths;import worldline.api.*;import worldline.kernel.ControlledMinecraftRuntime;import worldline.trace.CanonicalTrace;
public final class RedstoneFanoutSmoke{
 private static final long SEED=50920240820L;private RedstoneFanoutSmoke(){}public static void main(String[]a){RedstoneFanoutBackend b=new RedstoneFanoutBackend(SEED);MinecraftRuntime r=new ControlledMinecraftRuntime(b);r.bootHeadless();try{r.loadWorld(WorldSource.at(Paths.get("memory","worldline-smoke")));CanonicalTrace t=new CanonicalTrace(SEED);b.power();r.tick();b.snapshot(t,"powered-t");b.disconnect();r.tick();b.snapshot(t,"south-disconnected");b.depower();r.tick();b.snapshot(t,"source-removed");b.assertFinal();t.emitTo(System.out);}finally{r.close();}}
}
