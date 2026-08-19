package worldline.api;

final class RemoteMobSpawnTest {
    private RemoteMobSpawnTest(){}
    static void run(){RemoteMobSpawn pig=new RemoteMobSpawn(7,90,144,2304,128,64,0,3,0);
        if(!pig.equals(new RemoteMobSpawn(7,90,144,2304,128,64,0,3,0))||pig.hashCode()!=new RemoteMobSpawn(7,90,144,2304,128,64,0,3,0).hashCode()
                ||pig.x()!=4.5D||pig.y()!=72D||pig.z()!=4D||pig.legacyType()!=90||pig.metadataEntries()!=3)throw new AssertionError("mob spawn value drift");
        fail(()->new RemoteMobSpawn(-1,90,0,0,0,0,0,1,0));fail(()->new RemoteMobSpawn(1,128,0,0,0,0,0,1,0));fail(()->new RemoteMobSpawn(1,90,0,0,0,0,0,0,0));}
    private static void fail(Runnable r){try{r.run();throw new AssertionError("expected mob failure");}catch(IllegalArgumentException expected){}}
}
