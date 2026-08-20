# M282 qualification cycle

`LimeWoolCycle` rebuilds the raised stone fixture in two fresh official
server JVMs. Each run places lime wool item `35` (damage `5`) on the
top face, freezes live `35:5`, and reloads that cell after save plus a
fresh login. The result is distinct from M253 green wool `35:13`.

The frozen semantic SHA-256 is
`9b8eadf13d246083c150829c5914921e95f5f55d4dadc11e35ae365d392615c6`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
