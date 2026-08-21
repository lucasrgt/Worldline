# M301 qualification cycle

`AxeLogBreaksCycle` rebuilds the raised stone fixture in two fresh
official server JVMs. Each run places oak `17:0`, spruce `17:1`, and
birch `17:2`, holds stone axe item `275` through Packet16, fully breaks
each log with Packet14, and requires Packet21 stacks `17:1:0`, `17:1:1`,
and `17:1:2` plus each block becoming air. One official EOF is retried
after a 5 second sleep.

The frozen semantic SHA-256 is
`6e62367a3c72d64d2bda9180cb0e5b0484671ef7530e74968d511330d7a06365`.

Run directly with:

```text
java tools/smoke/AxeLogBreaksCycle.java m301-axe-log-breaks
```

Canonical evidence uses two official server JVMs and two client
sessions. Headless protocol-14 only. No GUI. No Aero.
