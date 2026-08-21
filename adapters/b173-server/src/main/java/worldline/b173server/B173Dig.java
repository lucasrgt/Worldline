package worldline.b173server;

import java.io.DataOutputStream;
import java.io.IOException;
import worldline.api.BlockPosition;

/** Packet14 digging statuses used by the official protocol-14 play channel. */
final class B173Dig {
    private B173Dig() {}
    static void write(DataOutputStream output, BlockPosition position, int status) throws IOException {
        if (position == null || position.y() < 0 || position.y() >= 128)
            throw new IllegalArgumentException("invalid dig position");
        output.writeByte(14); output.writeByte(status); output.writeInt(position.x());
        output.writeByte(position.y()); output.writeInt(position.z()); output.writeByte(1); output.flush();
    }
}
