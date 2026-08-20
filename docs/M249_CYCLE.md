# M249 qualification cycle

`YellowWoolCycle` rebuilds the raised stone fixture in two fresh official
server JVMs. Each run places yellow wool item `35` (damage `4`) on the
top face, freezes live `35:4`, and reloads that cell after save plus a
fresh login. The result is distinct from M197 white wool `35:0`.

The frozen semantic SHA-256 is
`1aa0065907c89647235eddd412bad95e322d6ecd1ecfdb97dfdd1a8a7f20e599`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
