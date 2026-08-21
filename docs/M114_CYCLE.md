# M114 qualification cycle

`CausalWaterFlowCycle` verifies the official server artifact, compiles the
protocol-14 adapter and smoke, and runs two fresh server workspaces. Each run
uses one actor session for the dig and live water observation and one fresh
reader session for the authoritative full-chunk reload.

The actor descends within official reach, sends begin/finish dig with a bounded
three-second mining interval, observes air, sustains forty heartbeats, then
disconnects before save. The reader must receive exactly the settled water
state through Packet51. The runner requires both state rows, traces and
signatures to match before checking frozen evidence. Diagnostic mode cannot
qualify.

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`658a1cbfc4555fb57b3cef83375f655232f18b834afe547330fd96e64c8a5e3e`.
