# M399 qualification cycle

`WoodenButtonSetCycle` rebuilds the raised stone column in two fresh
official server JVMs. Each run places stone button `77` on the east,
west, south, and north faces, then reloads `77:1`, `77:2`, `77:3`, and
`77:4` after save plus fresh login. The signal must include multiple `77`
damages. A wooden-button `143` result is impossible in b1.7.3 and fails.
A single-face pulse matching M165, M279, or M340 fails. One official EOF
is retried after a 5 second sleep. Headless `B173WireClient` is the only
client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/WoodenButtonSetCycle.java m399-wooden-button-set
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`898b58fa0f849df159f7bfcfde243b0957fddcd580770518251b1721cbf21c90`.
