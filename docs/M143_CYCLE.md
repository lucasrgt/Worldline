# M143 qualification cycle

`PistonRetractionCycle` rebuilds and extends the M142 fixture, saves it, and
uses a fresh client to perform the retraction. A third client proves the final
states after another save. The complete sequence repeats in two fresh official
server JVMs.

The frozen semantic SHA-256 is
`ed36c9824aa5c765b651fa5a53fa268e5427568f47fabaeb082ec26f7639e2e1`.

Canonical evidence uses two official server JVMs and six client sessions.
