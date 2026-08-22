package worldline.smoke.entitydynamics;
import java.nio.file.Paths;import worldline.api.MinecraftRuntime;import worldline.api.WorldSource;import worldline.kernel.ControlledMinecraftRuntime;import worldline.trace.CanonicalTrace;
/** Composite differential for M504, M505, M507, and M508. */
public final class EntityDynamicsSmoke{
 private static final long SEED=50450820240821L;private EntityDynamicsSmoke(){}public static void main(String[]a){CanonicalTrace t=new CanonicalTrace(SEED);run(t,"ghast-open",12);run(t,"ghast-roof",12);run(t,"slime-open",60);run(t,"slime-roof",60);run(t,"boat-open",20);run(t,"boat-wall",20);run(t,"cart-short",30);run(t,"cart-long",30);t.emitTo(System.out);}private static void run(CanonicalTrace t,String mode,int ticks){EntityDynamicsBackend b=new EntityDynamicsBackend(SEED,mode);MinecraftRuntime r=new ControlledMinecraftRuntime(b);r.bootHeadless();r.loadWorld(WorldSource.at(Paths.get("memory",mode)));try{b.snapshot(t,mode+"-seed");for(int n=1;n<=ticks;n++){r.tick();if(n==1||n==ticks)b.snapshot(t,mode+"-"+n);}b.assertOutcome();}finally{r.close();}}
}
