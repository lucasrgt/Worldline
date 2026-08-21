# M114 behavior map

The fixed-seed server generates still water `9:0` directly above dirt `3:0`.
Worldline does not inject a fluid or call a light/world implementation. The
actor sends Packet14 begin and finish against the dirt; Packet53 confirms the
intermediate air cell. Ordinary Packet10 heartbeats then let the official
scheduled block logic run.

The live world cache must observe water in the opened cell. After disconnect
and save, a new client receives Packet51 and must expose the same `9:8` state.
Every `(x,y,z,beforeId,beforeMeta,afterId,afterMeta)` difference in the full
chunk is hashed in X/Z/Y order. Exactly one transition is permitted.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|chunk=0,0|cause=packet14-break-below-generated-water|confirmation=packet53-air|settle=40ticks|observation=live-packet53+fresh-login-packet51|source=4:55:4:9:0,opened=4:54:4:3:0->0:0->9:8,states=1:1:0:33f402b3ec13c94b9dbba6028315449e5d84fc251c16206ed92d09748f9299b2|disconnect=clean
```

SHA-256: `658a1cbfc4555fb57b3cef83375f655232f18b834afe547330fd96e64c8a5e3e`.

This is a behavioral oracle for one downward water transition, not a generic
fluid API, algorithm, tick scheduler, or performance qualification.
