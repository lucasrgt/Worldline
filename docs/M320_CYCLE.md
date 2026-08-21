# M320 qualification cycle

`LeatherArmorCraftsCycle` rebuilds the raised stone workbench fixture in two
fresh official server JVMs. Each run crafts the leather armor family from
leather `334` and reloads those result ids from personal storage. One
official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`48274c2675afd82a6d376e7ec9ceb1e8896adc3761f59461d619a2ae378b90f4`.

Run directly with:

```text
java tools/smoke/LeatherArmorCraftsCycle.java m320-leather-armor-crafts
```

Canonical evidence uses two official server JVMs and four client sessions.
