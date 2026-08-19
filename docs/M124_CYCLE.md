# M124 qualification cycle

`CrossChunkLightRecoveryCycle` verifies the official artifact and repeats the
complete add/witness/remove/final-reader sequence in two fresh worlds. Each run
uses one official server JVM and three client sessions.

The runs must match source restoration, both increase deltas, both decrease
deltas, both empty residual deltas, semantic trace and signature. Pending or
diagnostic descriptors cannot qualify.

The frozen semantic SHA-256 is
`60903e4d40e5297e01412eb69996ce5f3e2b641f1898d67f376ff357d016dbce`.
