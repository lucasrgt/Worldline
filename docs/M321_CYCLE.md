# M321 qualification cycle

`GoldArmorCraftsCycle` rebuilds the raised stone workbench fixture in two
fresh official server JVMs. Each run crafts the gold armor family from
ingots `266` and reloads those result ids from personal storage. One
official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`a44c48c91eba492305c1faa7963dd3ad1023d9a9a97bd6ccd92c2b8abcec9fbf`.

Run directly with:

```text
java tools/smoke/GoldArmorCraftsCycle.java m321-gold-armor-crafts
```

Canonical evidence uses two official server JVMs and four client sessions.
