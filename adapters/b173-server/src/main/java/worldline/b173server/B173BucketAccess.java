package worldline.b173server;

import java.io.IOException;

/** Public smoke boundary for empty-bucket Packet7 button-0 entity interact. */
public final class B173BucketAccess {
    private B173BucketAccess() {}

    public static void useOnMob(B173WireClient client, int entity) {
        try { client.channel().useBucketOnMob(entity); }
        catch (IOException error) { throw new IllegalStateException("bucket mob use failed", error); }
    }
}
