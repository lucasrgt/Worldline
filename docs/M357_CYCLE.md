# M357 qualification cycle

`GlowstoneDustCraftsCycle` rebuilds one official personal 2x2 glowstone-dust
SET in two fresh official server JVMs. Each run seeds dust `348x4`, crafts
glowstone `89x1`, and reloads that stack after save plus a fresh login. One
official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`af0a81cf89ec64afd6056fb4755ef7ed9350bac34875caa333cc150d99d7955c`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero. This cycle does not place
blocks.

Run directly with:

```text
java tools/smoke/GlowstoneDustCraftsCycle.java m357-glowstone-dust-crafts
```
