package worldline.smoke.spawnlightsetb173;

import worldline.api.BlockPosition;import worldline.api.RemoteMobSpawn;import worldline.b173server.B173HostileAccess;import worldline.b173server.B173WireClient;

/** Nearby Packet24 50/54 probe: present in the dark, rejected under torch light 14. */
final class SpawnLightProbe{
 private SpawnLightProbe(){}
 static boolean near(RemoteMobSpawn s,BlockPosition a,BlockPosition b){return in(s,a)||in(s,b);}
 static boolean in(RemoteMobSpawn s,BlockPosition p){return Math.abs(s.x()-(p.x()+0.5D))<=4.5D&&Math.abs(s.y()-p.y())<=2D&&Math.abs(s.z()-(p.z()+0.5D))<=4.5D;}
 static RemoteMobSpawn awaitDark(B173WireClient a,BlockPosition first,BlockPosition second){int player=a.state().entityId();for(int n=0;n<64;n++){RemoteMobSpawn s=B173HostileAccess.next(a);int t=s.legacyType();SpawnLightPad.require(s.entityId()!=player&&t!=90&&(t==50||t==51||t==52||t==54),"dark Packet24 identity drift");if((t==50||t==54)&&near(s,first,second))return s;}throw new IllegalStateException("dark Packet24 type 50 or 54 absent near pad");}
 static void requireTorchReject(B173WireClient a,BlockPosition first,BlockPosition second,int ticks){for(int i=0;i<ticks;i++){a.sustainTicks(1);RemoteMobSpawn peek=B173HostileAccess.peekDespawnFamily(a);while(peek!=null){RemoteMobSpawn s=a.awaitMobSpawn(peek.legacyType());SpawnLightPad.require(!((s.legacyType()==50||s.legacyType()==54)&&near(s,first,second)),"torch Packet24 type "+s.legacyType()+" near pad");peek=B173HostileAccess.peekDespawnFamily(a);}}}
}
