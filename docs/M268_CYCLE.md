# M268 qualification cycle

`FlintSteelFireCycle` rebuilds the raised stone fixture in two fresh official
server JVMs. Each run uses flint-and-steel item `259` on the top face, proves
live fire `51:0`, then accepts only the official fire-or-air state during a
bounded hold and on fresh login while requiring the stone support. One official
EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`50fbd4ba9248b6647eee949cc037cb741948628611a039361dfe320c5099dc22`.

Canonical evidence uses two official server JVMs and four client sessions.
