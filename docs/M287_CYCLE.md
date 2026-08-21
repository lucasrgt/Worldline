# M287 qualification cycle

`PurpleWoolCycle` rebuilds the raised stone fixture in two fresh official
server JVMs. Each run places purple wool item `35` (damage `10`) on the
top face, freezes live `35:10`, and reloads that cell after save plus a
fresh login. The result is distinct from blue `35:11` and magenta
`35:2`. One official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`5dc40bd722b0e06eda7a5458a94b93ca0bdccfc730d99fdbe3204f19d850a7a8`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
