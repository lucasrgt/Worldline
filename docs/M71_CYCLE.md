# M71 Cycle

Status: **GO** for balanced paired acquisition and descriptive selected-row
summaries; no causal or performance classification is promoted.

## Qualified path

- four fresh matched pairs run in balanced `C/E, E/C, C/E, E/C` order;
- every arm verifies official server hashes and pinned clean Aero provenance;
- the M70 fixture, seed, names, process configuration, heap, and logger are held constant;
- exact Packet3 chat is the common client-handler anchor in both arms;
- control emits no Packet18 or Packet7 and rejects matching Packet18/38 observations;
- event emits Packet18 before Packet7 and applies Packet18 before Packet38 before the first measured frame;
- each arm completes at least 300 warmup frames/five seconds and 480 measured frames/eight seconds;
- each arm has at least 30 strictly parsed threshold/GC/heartbeat-selected Aero rows;
- stdout-bracket rows occur in order in the asynchronously flushed post-exit file;
- per-arm summaries and per-pair deltas are descriptive dynamic evidence only;
- all graphical clients, wire sessions, and official servers shut down cleanly.

Frozen semantic SHA-256:

`0b26d07ed6b08195a067bf8730b43f49ec596dae274c74f335f8a44576cb1d2b`

Generated worlds, logs, numerical summaries, and official artifacts remain
local evidence and are not release artifacts.
