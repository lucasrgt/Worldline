package worldline.m72;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.modificationstation.stationapi.api.network.packet.MessagePacket;

/** Common BE synchronized by one explicit StationAPI content message. */
public final class WorldlineContentBlockEntity extends BlockEntity {
  private int nonce;
  public int nonce() {
    return nonce;
  }
  public void setNonce(int value) {
    nonce = value;
  }
  @Override
  public void readNbt(NbtCompound nbt) {
    super.readNbt(nbt);
    nonce = nbt.getInt("WorldlineNonce");
  }
  @Override
  public void writeNbt(NbtCompound nbt) {
    super.writeNbt(nbt);
    nbt.putInt("WorldlineNonce", nonce);
  }
  @Override
  public Packet createUpdatePacket() {
    MessagePacket packet = new MessagePacket(WorldlineContentMod.SYNC);
    packet.ints = new int[] {x, y, z, nonce};
    return packet;
  }
}
