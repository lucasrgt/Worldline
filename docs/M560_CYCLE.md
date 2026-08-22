# M560 qualification cycle

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
