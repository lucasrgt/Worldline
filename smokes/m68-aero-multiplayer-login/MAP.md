# M68 Aero Multiplayer Login Evidence Map

| Boundary | Exact evidence |
| --- | --- |
| Provenance | Pinned, clean Aero `436d65b` checkout before and after both runs |
| Client | Real b1.7.3 Fabric/StationAPI client loads Aero 3.0.0 |
| Server | Hash-verified official b1.7.3 dedicated JAR on localhost/offline mode |
| Login | Production `ConnectScreen` sends the named login; Packet1 is applied |
| Play readiness | The first Packet13 is processed by the vanilla network handler |
| Remote world | Packet51 is counted only after the vanilla handler applies it |
| Rendering | Twenty `GameRenderer.onFrameUpdate` completions occur after readiness |
| Aero logging | At least one parseable Aero frame row is written after readiness |
| Lifecycle | Client disconnects explicitly, exits normally, and server stops cleanly |

M68 proves composition and runtime compatibility for a client-modified vanilla
multiplayer world. It does not claim Aero model content on the server, pixels,
performance, a lag spike, combat, causal attribution, or generic remote-server
compatibility.

Frozen expected signature SHA-256: `a7978b0bb7e1277d846528036ff3ded3c5541ea5b11bd0935d32580b574e969f`
