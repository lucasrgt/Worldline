# M343 qualification cycle

`FireFamilySetCycle` rebuilds the raised netherrack-plus-wool fixture in
two fresh official server JVMs. Each run uses flint-and-steel item `259`
to place fire `51`, holds the netherrack flame, and waits a bounded tick
window until adjacent wool `35` is consumed. One official EOF is retried
after a 5 second sleep.

The frozen semantic SHA-256 is
`b04d10e87e540d454627a3960abbf311c9912ca625d00f3e71af970ea08e77f6`.

Run directly with:

```text
java tools/smoke/FireFamilySetCycle.java m343-fire-family-set
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
