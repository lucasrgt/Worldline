# M356 qualification cycle

`JackOLanternCraftsCycle` rebuilds the raised stone fixture in two fresh
official server JVMs. Each run crafts jack-o-lantern `91` from pumpkin
`86` plus torch `50` in personal window 0, places leftover pumpkin `86`
and crafted lantern `91` with look yaw `-90`, and reloads those cells
after save plus fresh login. One official EOF is retried after a 5 second
sleep.

The frozen semantic SHA-256 is
`b870de18f5f7c2616c607111ea332fc3f4426f8f5a3a82d713703270066ee5b1`.

Run directly with:

```text
java tools/smoke/JackOLanternCraftsCycle.java m356-jack-o-lantern-crafts
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
