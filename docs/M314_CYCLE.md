# M314 qualification cycle

`ArmorCraftsCycle` rebuilds the raised stone workbench fixture in two fresh
official server JVMs. Each run crafts the iron armor family from ingots
`265` and reloads those result ids from personal storage. One official EOF
is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`914b42df18b53c2afcbb40f2f5c87b8848dc19e4e816eaef927067915c98b437`.

Run directly with:

```text
java tools/smoke/ArmorCraftsCycle.java m314-armor-crafts
```

Canonical evidence uses two official server JVMs and four client sessions.
