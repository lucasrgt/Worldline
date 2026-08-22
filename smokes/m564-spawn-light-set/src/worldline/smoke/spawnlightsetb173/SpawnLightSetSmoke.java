package worldline.smoke.spawnlightsetb173;

import java.nio.charset.StandardCharsets;import java.nio.file.*;import java.security.MessageDigest;import java.time.Duration;import worldline.api.RemoteMobSpawn;import worldline.b173server.*;

/** Contrasts nearby Packet24 50/54 at night light <=7 with torch-light 14 rejection on the same pad. */
public final class SpawnLightSetSmoke{
 private SpawnLightSetSmoke(){}
 public static void main(String[]a)throws Exception{
  if(a.length!=7)throw new IllegalArgumentException("usage: SpawnLightSetSmoke server.jar workspace port seed username chunkX chunkZ");
  Path jar=Paths.get(a[0]),workspace=Paths.get(a[1]),dark=workspace.resolve("dark"),lit=workspace.resolve("lit");int port=Integer.parseInt(a[2]);long seed=Long.parseLong(a[3]);String user=a[4];int cx=Integer.parseInt(a[5]),cz=Integer.parseInt(a[6]);
  SpawnLightPad.require(seed==17320110707L&&user.equals("SpawnLight564")&&user.length()<=16,"spawn-light-set identity drift");
  Duration timeout=Duration.ofSeconds(180);Files.createDirectories(dark);SpawnLightPad pad=SpawnLightPad.build(jar,dark,port,seed,user,cx,cz,timeout);SpawnLightPad.copyWorld(dark,lit);
  B173DedicatedServer server=B173DedicatedServer.monsters(jar,dark,port,seed,timeout,3,true);B173WireClient actor=new B173WireClient("127.0.0.1",port,user,timeout);RemoteMobSpawn spawned;
  try{server.boot();actor.connect();actor.synchronizePose();SpawnLightPad.require(actor.awaitInventory().occupiedSlots()>=1,"spawn-light-set dark inventory drift");server.setTime(14000L);spawned=SpawnLightProbe.awaitDark(actor,pad.first,pad.second);SpawnLightPad.require((spawned.legacyType()==50||spawned.legacyType()==54)&&spawned.legacyType()!=90&&spawned.legacyType()!=51&&spawned.legacyType()!=52,"dark arm collapsed to M141 pig or M390 spider identity");actor.close();SpawnLightPad.awaitPlayers(server,0);server.save();
  }finally{actor.close();server.close();}
  server=B173DedicatedServer.monsters(jar,lit,port,seed,timeout,3,true);actor=new B173WireClient("127.0.0.1",port,user,timeout);
  try{server.boot();actor.connect();actor.synchronizePose();SpawnLightPad.require(actor.awaitInventory().occupiedSlots()>=1,"spawn-light-set torch inventory drift");int torches=SpawnLightPad.lightPad(actor,pad.first,pad.second);server.setTime(14000L);SpawnLightProbe.requireTorchReject(actor,pad.first,pad.second,200);actor.close();SpawnLightPad.awaitPlayers(server,0);server.save();
   String evidence="column="+pad.column+",platform=7x7-48grass,spawners="+SpawnLightPad.cell(pad.first,52,0)+"+"+SpawnLightPad.cell(pad.second,52,0)+",entityid=Creeper+Zombie,dark=type50-or-54,torch=50:5x"+torches+",torch-light=14,torch-arm=absent,night=14000,clients=3,disconnect=clean";
   String trace="v1|server=official-b1.7.3|seed="+seed+"|fixture=raised-7x7-grass-platform+two-spawner52|cause=nbt-entityid-creeper+zombie+time-14000+torch-50-5-light-14|wire=packet24-type50-or-54-dark+packet24-type50-or-54-absent-torch|oracle=dark-spawn-torch-reject-not-m435-natural-not-m390-identity-not-m141-pig|"+evidence;
   System.out.println("WORLDLINE_M564_SET="+evidence);System.out.println("WORLDLINE_M564_TRACE="+trace);System.out.println("WORLDLINE_M564_SIGNATURE="+sha(trace));
  }finally{actor.close();server.close();}
 }
 private static String sha(String s)throws Exception{byte[]b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder v=new StringBuilder();for(byte x:b)v.append(String.format("%02x",x&255));return v.toString();}
}
