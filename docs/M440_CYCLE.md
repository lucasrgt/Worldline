# M440 qualification cycle

`RemainingDyeRestSetCycle` rebuilds the remaining-dye-rest fixture in two
fresh official server JVMs. Each run mixes light gray from ink plus two
bone meal, light gray from gray plus bone meal, and magenta from purple
plus pink in the personal 2x2 grid and reloads those stacks. One official
EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`0334f546ce0368581cb95d0fcb41d97e63d257acb76e91c53b41c849cfac594d`.

Run directly with:

```text
java tools/smoke/RemainingDyeRestSetCycle.java m440-remaining-dye-rest-set
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
