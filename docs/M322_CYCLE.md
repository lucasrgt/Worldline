# M322 qualification cycle

`DiamondArmorCraftsCycle` rebuilds the raised stone workbench fixture in two
fresh official server JVMs. Each run crafts the diamond armor family from
gems `264` and reloads those result ids from personal storage. One official
EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`b54acc14a0bba483871701ba342becc842fe45291b56aca8212a1b71a2b5269d`.

Run directly with:

```text
java tools/smoke/DiamondArmorCraftsCycle.java m322-diamond-armor-crafts
```

Canonical evidence uses two official server JVMs and four client sessions.
