# M292 qualification cycle

`BirchLeavesCycle` rebuilds the raised stone fixture in two fresh official
server JVMs. Each run places oak log item `17` adjacent first, then
birch leaves item `18` (damage `2`) on the top face, and reloads leaf
`18:10`. One EOF reconnect waits five seconds and retries the same
official JVM.

The frozen semantic SHA-256 is
`909703a5406842a4c1becff13064c13eebc661300e0ebe15b3400c822771f912`.

Canonical evidence uses two official server JVMs and four client sessions.
