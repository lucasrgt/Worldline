package worldline.b173server;

import java.io.*;

/** Freezes both signed-dimension encodings through the production Packet9 writer. */
public final class B173RespawnPacketFixture {
 private B173RespawnPacketFixture(){}
 public static void main(String[]a)throws Exception{ByteArrayOutputStream bytes=new ByteArrayOutputStream();DataOutputStream out=new DataOutputStream(bytes);B173RespawnPacket.write(out,0);B173RespawnPacket.write(out,-1);byte[]v=bytes.toByteArray();if(v.length!=4||v[0]!=9||v[1]!=0||v[2]!=9||v[3]!=(byte)255)throw new IllegalStateException("Packet9 encoding drift");System.out.println("WORLDLINE_M135_PACKET9=09-00|09-ff");}
}
