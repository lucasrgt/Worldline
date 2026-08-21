package worldline.b173server;

import java.io.IOException;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteDispenserLoad;
import worldline.api.RemoteWindowKind;

/** Public dispenser window open/load boundary kept out of the capped play client. */
public final class B173DispenserWindows {
    private B173DispenserWindows() {}

    public static RemoteContainerWindow open(B173WireClient client, BlockPosition position, BlockFace face) {
        client.activateBlock(position, face);
        try {
            B173PlayChannel channel = client.channel();
            channel.inbound().beginWindow(RemoteWindowKind.DISPENSER);
            return channel.inbound().awaitChest();
        } catch (IOException error) {
            throw new IllegalStateException("dispenser window receive failed", error);
        }
    }

    public static RemoteDispenserLoad load(B173WireClient client, int personalSlot, int dispenserSlot) {
        try {
            B173PlayChannel channel = client.channel();
            return new B173DispenserChannel(channel.output, channel.inbound()).load(personalSlot, dispenserSlot);
        } catch (IOException error) {
            throw new IllegalStateException("dispenser load failed", error);
        }
    }
}
