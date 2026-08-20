# M278 qualification cycle

`TrapdoorToggleCycle` rebuilds the raised stone plus trapdoor fixture in
two fresh official server JVMs. Each run places item 96 against the east
face, then empty-hand Packet15 opens `96:3` to `96:7`. One official EOF is
retried after a 5 second sleep.

The frozen semantic SHA-256 is
`a66063d6e5ac041de1eeb23cf5a56d2fe303a9759694e0dd69ce31347ef8442a`.

Canonical evidence uses two official server JVMs and four client sessions.
