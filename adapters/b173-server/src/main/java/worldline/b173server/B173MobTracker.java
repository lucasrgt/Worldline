package worldline.b173server;

import java.io.*;import java.util.*;import worldline.api.RemoteMobSpawn;

/** Bounded Packet24 queue with strict protocol-14 entity-metadata decoding. */
final class B173MobTracker {
    private static final int MAX=64;private final ArrayList<RemoteMobSpawn> pending=new ArrayList<>();
    void spawn(DataInputStream in)throws IOException{int entity=in.readInt(),type=in.readUnsignedByte(),x=in.readInt(),y=in.readInt(),z=in.readInt(),yaw=in.readUnsignedByte(),pitch=in.readUnsignedByte();Metadata metadata=metadata(in);if(pending.size()==MAX)throw new IOException("mob spawn queue overflow");try{pending.add(new RemoteMobSpawn(entity,type,x,y,z,yaw,pitch,metadata.entries,metadata.flags));}catch(IllegalArgumentException e){throw new IOException("invalid mob spawn",e);}}
    RemoteMobSpawn take(int type){if(type<0||type>127)throw new IllegalArgumentException("invalid expected mob type");for(int i=0;i<pending.size();i++)if(pending.get(i).legacyType()==type)return pending.remove(i);return null;}
    private static Metadata metadata(DataInputStream in)throws IOException{int mask=0,entries=0,flags=-1;while(true){int header=in.readUnsignedByte();if(header==127)break;int index=header&31,type=header>>5,bit=1<<index;if((mask&bit)!=0)throw new IOException("duplicate mob metadata index "+index);mask|=bit;entries++;switch(type){case 0:int value=in.readUnsignedByte();if(index==0)flags=value;break;case 1:in.readShort();break;case 2:in.readInt();break;case 3:in.readFloat();break;case 4:B173InboundPacket.string(in,32767);break;case 5:int item=in.readShort();if(item>=0){in.readByte();in.readShort();}break;case 6:in.readInt();in.readInt();in.readInt();break;default:throw new IOException("invalid mob metadata type "+type);}}if(entries<1||flags<0)throw new IOException("mob flags metadata absent");return new Metadata(entries,flags);}
    private static final class Metadata{final int entries,flags;Metadata(int e,int f){entries=e;flags=f;}}
}
