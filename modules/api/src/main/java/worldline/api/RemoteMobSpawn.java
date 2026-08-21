package worldline.api;

import java.util.Objects;

/** Immutable protocol-14 Packet24 identity, quantized pose and bounded metadata summary. */
public final class RemoteMobSpawn {
    private final int entityId, legacyType, fixedX, fixedY, fixedZ, yaw, pitch, metadataEntries, flags;
    public RemoteMobSpawn(int entityId, int legacyType, int fixedX, int fixedY, int fixedZ,
            int yaw, int pitch, int metadataEntries, int flags) {
        if (entityId < 0) throw new IllegalArgumentException("invalid mob entity id");
        if (legacyType < 0 || legacyType > 127) throw new IllegalArgumentException("invalid mob type");
        if (yaw < 0 || yaw > 255 || pitch < 0 || pitch > 255) throw new IllegalArgumentException("invalid mob rotation");
        if (metadataEntries < 1 || metadataEntries > 32 || flags < 0 || flags > 255)
            throw new IllegalArgumentException("invalid mob metadata summary");
        this.entityId=entityId;this.legacyType=legacyType;this.fixedX=fixedX;this.fixedY=fixedY;this.fixedZ=fixedZ;
        this.yaw=yaw;this.pitch=pitch;this.metadataEntries=metadataEntries;this.flags=flags;
    }
    public int entityId(){return entityId;}public int legacyType(){return legacyType;}
    public int fixedX(){return fixedX;}public int fixedY(){return fixedY;}public int fixedZ(){return fixedZ;}
    public double x(){return fixedX/32.0D;}public double y(){return fixedY/32.0D;}public double z(){return fixedZ/32.0D;}
    public int yaw(){return yaw;}public int pitch(){return pitch;}public int metadataEntries(){return metadataEntries;}public int flags(){return flags;}
    @Override public boolean equals(Object other){if(!(other instanceof RemoteMobSpawn))return false;RemoteMobSpawn v=(RemoteMobSpawn)other;
        return entityId==v.entityId&&legacyType==v.legacyType&&fixedX==v.fixedX&&fixedY==v.fixedY&&fixedZ==v.fixedZ
                &&yaw==v.yaw&&pitch==v.pitch&&metadataEntries==v.metadataEntries&&flags==v.flags;}
    @Override public int hashCode(){return Objects.hash(entityId,legacyType,fixedX,fixedY,fixedZ,yaw,pitch,metadataEntries,flags);}
}
