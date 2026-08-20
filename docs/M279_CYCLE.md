# M279 qualification cycle

`ButtonPressCycle` rebuilds the raised stone plus east-face stone-button
fixture in two fresh official server JVMs. Each run places item 77 as
setup, empty-hand Packet15-presses it, and reloads the unpowered `77:1`
button. One official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`910692630d2dc89d5acd515f421970042c6dd218a9f6b2fbc97883e672bd3eb7`.

Canonical evidence uses two official server JVMs and four client sessions.
