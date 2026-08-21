# M436 qualification cycle

`RemainingArrowLifeSetCycle` rebuilds the raised-stone pad in two fresh
official server JVMs. Each run air-uses bow `261` so Packet23 type `60`
lands, drops the remaining arrow `262` as Packet21, and waits until
Packet103 restores that stack. One official EOF is retried after a 5
second sleep.

The frozen signal must name bow `261`, arrow `262`, type `60`, and
`pickup=262`. It must not collapse to M332 workbench crafts.

Run directly with:

```text
java tools/smoke/RemainingArrowLifeSetCycle.java m436-remaining-arrow-life-set
```

The frozen semantic SHA-256 is
`9a370fd980f9abd2ed3f852ff575a9dae9c9b0f461c73fa548d131b40077011c`.

Canonical evidence uses two official server JVMs and two client sessions.
Headless protocol-14 only. No GUI. No Aero.
