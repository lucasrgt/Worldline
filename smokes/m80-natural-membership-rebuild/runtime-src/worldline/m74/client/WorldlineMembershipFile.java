package worldline.m74.client;

import java.io.*;import java.nio.file.*;import java.security.*;import worldline.m74.*;

/** Writes the request/event identity only after the complete bracket seals. */
public final class WorldlineMembershipFile {
    private static boolean written;private WorldlineMembershipFile(){}public static boolean written(){return written;}
    public static void write(){if(written||!WorldlinePagedBridge.sealed()||!WorldlineMembershipEvent.valid())throw new IllegalStateException("invalid M80 write state "+WorldlineMembershipEvent.diagnostic());Path p=Paths.get(System.getProperty("worldline.membership.file","")).toAbsolutePath();try{Files.createDirectories(p.getParent());try(DataOutputStream o=new DataOutputStream(Files.newOutputStream(p,StandardOpenOption.CREATE_NEW))){o.writeInt(0x574c3830);o.writeInt(1);o.writeInt(36);o.writeInt(WorldlineCensusProbe.nonce());o.writeInt(WorldlineCensusSync.x());o.writeInt(WorldlineCensusSync.y());o.writeInt(WorldlineCensusSync.z());o.writeInt(WorldlineMembershipEvent.requestIndex);o.writeInt(WorldlineMembershipEvent.eventIndex);}byte[]b=Files.readAllBytes(p);StringBuilder h=new StringBuilder();for(byte v:MessageDigest.getInstance("SHA-256").digest(b))h.append(String.format("%02x",v&255));written=true;System.out.println("[WorldlineMembership] complete requestIndex="+WorldlineMembershipEvent.requestIndex+" eventIndex="+WorldlineMembershipEvent.eventIndex+" bytes="+b.length+" sha256="+h);}catch(IOException|NoSuchAlgorithmException e){throw new IllegalStateException("M80 artifact write failed",e);}}
}
