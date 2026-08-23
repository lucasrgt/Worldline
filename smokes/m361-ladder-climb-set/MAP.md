<!-- worldline-map-schema=1 -->
<!-- boundary=client-runtime-equivalence -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=113dccdda9b6bd0140c7aea5b255db993bb9063c6d64ef9370f1fb9925c26340 -->

# M361 behavior map

Frozen expected signature SHA-256: 113dccdda9b6bd0140c7aea5b255db993bb9063c6d64ef9370f1fb9925c26340

The server half sends Packet15 for ladder item `65`, observes two east-facing
Packet53 cells `65:5`, saves, and verifies both cells through a fresh login. Its
trace explicitly claims placement and persistence only.

The climb half belongs to the controlled client differential.
`B173PhysicsProbe.ladder` builds equivalent wall-air and two-cell ladder
fixtures and calls the mapped Beta 1.7.3 player movement root. The official
oracle calls the corresponding obfuscated root with the same fixture and tick
count. Two mapped and two official processes must produce the same trace.

No Packet13 climb or Worldline vertical-motion equation is accepted as the
physics oracle. The four-process client differential freezes `air=0` and
`climb=1058` after ten ticks in shared physics signature
`c2508b3dfff5f7852ce6b3155c5257ba781482031001cfdd38326b3363a5c014`.
