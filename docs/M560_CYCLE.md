# M560-PORTAL-SCALE-SET Portal scale set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M560 freezes the official Beta 1.7.3 Overworld-to-Nether 8:1 portal
coordinate scale. A known far Overworld portal at chunk `(20,20)` is
built and ignited, then entered for the vanilla residence interval.
Packet9 changes the live session to dimension `-1`. The corrected
Packet13 pose, quantized to block coordinates, lies within 128 blocks
of `(x/8, z/8)` and is farther than 128 blocks from the Overworld
source, so the mapping is 8:1 rather than 1:1 or "nether exists".

The frozen source column is `325,66,331`; the quantized 8:1 expectation
is `40,41`. This is distinct from M132 portal activation, M133 traversal
that only proves a Nether destination near spawn, M134 roundtrip travel,
and M382 frame-ignite without Packet9. Headless `B173WireClient` only.
No GUI. No Aero. `allow-nether` is true.

The frozen semantic SHA-256 is
`d7eb052e1bc5fe6a71f3850bd4fb75b9470be6a2767c6617fb41f7138c54c50b`.

## Qualification cycle

`PortalScaleSetCycle` rebuilds a far Overworld `4x5` obsidian frame in
two fresh official server JVMs, ignites six portal `90` cells, and
travels into the Nether. Each run requires Packet9 `0→-1` and a
quantized Packet13 pose matching official 8:1 scale. One official EOF
is retried after a 5 second sleep. Headless `B173WireClient` is the
only client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/PortalScaleSetCycle.java m560-portal-scale-set
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`d7eb052e1bc5fe6a71f3850bd4fb75b9470be6a2767c6617fb41f7138c54c50b`.

Expected signal: `dimension=0->-1,scale=8,source=325:66:331,expected=40:41,quantized=true,within=128,not1to1=true,sky=0,column=2,packet9=0->-1`.

Frozen semantic SHA-256: `d7eb052e1bc5fe6a71f3850bd4fb75b9470be6a2767c6617fb41f7138c54c50b`.
