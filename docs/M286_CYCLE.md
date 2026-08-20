# M286 qualification cycle

`CyanWoolCycle` rebuilds the raised stone fixture in two fresh official
server JVMs. Each run places cyan wool item `35` (damage `9`) on the
top face, freezes live `35:9`, and reloads that cell after save plus a
fresh login. The result is distinct from light-blue `35:3` and blue
`35:11`. One official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`1632e3056edc9c3fa6a76285a528128698313979964d10cedb2b03544c838e61`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
