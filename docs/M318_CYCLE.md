# M318 qualification cycle

`GoldDiamondToolCraftsCycle` rebuilds the raised workbench fixture in two
fresh official server JVMs. Each run crafts gold tools `283,285,286,284`
and diamond tools `276,278,279,277`. One official EOF is retried after a
5 second sleep.

The frozen semantic SHA-256 is
`ea2a3772ad997141d967212b9f93a52ec0b5f633dde29b2b0192e844a377005e`.

Run directly with:

```text
java tools/smoke/GoldDiamondToolCraftsCycle.java m318-gold-diamond-tool-crafts
```

Canonical evidence uses two official server JVMs and two client sessions.
