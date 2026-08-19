package worldline.b173server;

import java.io.*;import worldline.api.RemoteMobSpawn;

/** Byte fixture for the exact production Packet24 decoder. */
public final class B173MobPacketFixture {
 private B173MobPacketFixture(){}
 public static void main(String[]a)throws Exception{ByteArrayOutputStream bytes=new ByteArrayOutputStream();DataOutputStream out=new DataOutputStream(bytes);out.writeInt(123);out.writeByte(90);out.writeInt(144);out.writeInt(2304);out.writeInt(128);out.writeByte(64);out.writeByte(0);out.writeByte(0);out.writeByte(0);out.writeByte(33);out.writeShort(300);out.writeByte(127);byte[]payload=bytes.toByteArray();B173MobTracker tracker=new B173MobTracker();tracker.spawn(new DataInputStream(new ByteArrayInputStream(payload)));RemoteMobSpawn pig=tracker.take(90);if(payload.length!=25||pig==null||pig.entityId()!=123||pig.x()!=4.5D||pig.y()!=72D||pig.z()!=4D||pig.yaw()!=64||pig.metadataEntries()!=2||pig.flags()!=0||tracker.take(90)!=null)throw new AssertionError("Packet24 fixture drift");System.out.println("WORLDLINE_M141_PACKET24=payload25,type90,metadata2:flags0");}
}
