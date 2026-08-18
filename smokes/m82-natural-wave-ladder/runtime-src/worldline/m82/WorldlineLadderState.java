package worldline.m82;

/** Primitive acknowledgement state shared without client or Aero types. */
public final class WorldlineLadderState {
    private static boolean ack;private static int x,y,z,nonce,targets;private WorldlineLadderState(){}
    public static synchronized void ack(int[]v){if(ack||v==null||v.length!=5)throw new IllegalStateException("invalid M82 ack");x=v[0];y=v[1];z=v[2];nonce=v[3];targets=v[4];ack=true;}
    public static synchronized boolean matches(int ex,int ey,int ez,int en,int et){return ack&&x==ex&&y==ey&&z==ez&&nonce==en&&targets==et;}
}
