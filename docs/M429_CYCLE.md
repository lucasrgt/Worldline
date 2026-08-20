# M429 qualification cycle

`RemainingAttachFacesCycle` rebuilds the three-cell raised stone column in
two fresh official server JVMs. Each run places remaining west, south, and
north attachments of ladder `65`, trapdoor `96`, and wall sign `68`, then
reloads those nine damages after save plus fresh login. The signal must
include remaining `65`, `96`, and `68` facings and must not claim climb,
toggle, or text. One official EOF is retried after a 5 second sleep.
Headless `B173WireClient` is the only client. There is no GUI and no Aero
path.

Run directly with:

```text
java tools/smoke/RemainingAttachFacesCycle.java m429-remaining-attach-faces
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`d00079b30c3f58f9f2a197e5a0a27c88880e15c28c3eaf88806d4502ebc2eb2b`.
