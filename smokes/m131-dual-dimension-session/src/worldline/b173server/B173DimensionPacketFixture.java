package worldline.b173server;

import java.io.*;import worldline.api.*;

/** Freezes Packet9 signed-byte transition and old-dimension cache invalidation. */
public final class B173DimensionPacketFixture{
 private B173DimensionPacketFixture(){}
 public static void main(String[]a)throws Exception{ByteArrayOutputStream wire=new ByteArrayOutputStream();DataOutputStream packet=new DataOutputStream(wire);packet.writeByte(9);packet.writeByte(-1);B173PlayInbound inbound=new B173PlayInbound(new DataInputStream(new ByteArrayInputStream(wire.toByteArray())),new DataOutputStream(new ByteArrayOutputStream()),1000,1,"DimensionFixture",0);inbound.cache().enableImplicitLoads();RemoteChunkObservation o=new RemoteChunkObservation(0,0,0,16,128,16,1);inbound.cache().accept(new RemoteChunkSnapshot(o,new byte[32768],new byte[16384],new byte[16384],new byte[16384]));require(inbound.cache().decoded()==1&&inbound.awaitDimension(-1)==-1&&inbound.dimension()==-1&&inbound.cache().decoded()==0&&inbound.cache().tracked()==0,"Packet9 dimension cache reset drift");System.out.println("WORLDLINE_M131_PACKET9=0->-1,cache=1->0");}
 private static void require(boolean v,String m){if(!v)throw new IllegalStateException(m);}
}
