# M294 qualification cycle

`PistonPlaceCycle` rebuilds the raised stone fixture in two fresh official
server JVMs. Each run places piston item `33` with look yaw `180` and
reloads facing metadata `1`. One official EOF is retried after a 5 second
sleep.

The frozen semantic SHA-256 is
`3fa31fff0d03751901d6283ff022999a5d94d205d79d1a77106294cc8b041624`.

Run directly with:

```text
java tools/smoke/PistonPlaceCycle.java m294-piston-place
```

Canonical evidence uses two official server JVMs and four client sessions.
