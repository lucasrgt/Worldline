# M400 qualification cycle

`RemainingTorchFacesCycle` rebuilds the raised stone column in two fresh
official server JVMs. Each run places torch item `50` on the east, west,
south, and north faces, then reloads wall-torch damages `50:1`, `50:2`,
`50:3`, and `50:4` after save plus fresh login. The signal must include
multiple `50` damages. One official EOF is retried after a 5 second sleep.
Headless `B173WireClient` is the only client. There is no GUI and no Aero
path.

Run directly with:

```text
java tools/smoke/RemainingTorchFacesCycle.java m400-remaining-torch-faces
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`ce7b2efbd3293b6dc413e9dd2c1b1c8af938af338cd70f50f3b973772d173868`.
