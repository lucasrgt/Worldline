# M285 qualification cycle

`LightGrayWoolCycle` rebuilds the raised stone fixture in two fresh
official server JVMs. Each run places light-gray wool item `35`
(damage `8`) on the top face, freezes live `35:8`, and reloads that
cell after save plus a fresh login. One official EOF is retried after
a 5 second sleep.

The frozen semantic SHA-256 is
`f98cb91704701be85feaa966d2fbe24aa8b5b4df58daeabe7fc7a799836f7ae5`.

Canonical evidence uses two official server JVMs and four client sessions.
