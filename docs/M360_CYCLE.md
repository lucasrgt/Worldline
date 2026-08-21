# M360 qualification cycle

`FishingCatchSetCycle` rebuilds the raised stone plus still-water dock in
two fresh official server JVMs. Each run casts fishing rod item `346`,
correlates Packet23 type `90`, and reels until Packet21 fish `349`
appears. One official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`b81e3dfcba437f67fee01101898bab64442120affa5b0cdb60dc16f69a2549b0`.

Run directly with:

```text
java tools/smoke/FishingCatchSetCycle.java m360-fishing-catch-set
```

Canonical evidence uses two official server JVMs and two client sessions.
Headless protocol-14 only. No GUI. No Aero.
