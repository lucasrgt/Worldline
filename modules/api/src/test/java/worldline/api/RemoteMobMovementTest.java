package worldline.api;

final class RemoteMobMovementTest {
    private RemoteMobMovementTest(){}
    static void run(){RemoteMobMovement move=new RemoteMobMovement(7,33,144,2304,128,145,2304,126,64,0);
        if(!move.equals(new RemoteMobMovement(7,33,144,2304,128,145,2304,126,64,0))||move.hashCode()!=new RemoteMobMovement(7,33,144,2304,128,145,2304,126,64,0).hashCode()||move.fromX()!=4.5D||move.toFixedZ()!=126||move.packetId()!=33)throw new AssertionError("mob movement value drift");
        fail(()->new RemoteMobMovement(7,32,0,0,0,1,0,0,0,0));fail(()->new RemoteMobMovement(7,31,0,0,0,0,0,0,0,0));}
    private static void fail(Runnable r){try{r.run();throw new AssertionError("expected movement failure");}catch(IllegalArgumentException expected){}}
}
