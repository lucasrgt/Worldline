# M366 qualification cycle

`MapFillSetCycle` rebuilds the raised stone fixture in two fresh official
server JVMs. Each run air-uses seeded empty map `358` and reloads the
held stack after save plus fresh login. The frozen signal includes `358`
plus the official filled result `358:1:0->358:1:0`, a protocol-14 no-op.
One official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`048613204222ae9dce7fb157d74dc94b69573ce8faaa9dd90cff64f7aab8f31f`.

Run directly with:

```text
java tools/smoke/MapFillSetCycle.java m366-map-fill-set
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
