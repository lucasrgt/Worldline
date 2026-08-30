<!-- worldline-map-schema=1 -->
<!-- boundary=aero-counterbalanced-hitch-rate -->
<!-- nonclaims=negative-control-only,no-optimization-promotion,no-cross-machine-absolute-fps -->
<!-- frozen-trace=7806dcff03e9d9470ac22e35cb3a39c1f837e75e29bae6c7a0addb1738944859 -->

# M771 Aero counterbalanced hitch-rate behavior map

M771 binds the neutral `worldline.profiling.HitchRateGate` to eight fresh
StationAPI clients over one restored 576-machine Aero scene. Four pairs use
the order AB, BA, BA, AB. A and B are intentionally identical negative-control
labels, so order bias and laboratory noise are observable without claiming an
optimization effect.

Each arm retains at least 60 seconds and seals every complete frame in a
checksum-protected WLPR artifact. A hitch is a `frame.wall.nanos` value at or
above 50,000,000 ns. The gate reports aggregate hitch rates per million frames,
paired rate quartiles and median, p99 median delta, and sign direction. A
regression requires aggregate and paired-median deltas above 500 ppm with a
strict majority of positive pairs.

The qualification proves that the gate, ordering, artifacts, and real-client
negative control operate together. It does not claim an Aero optimization,
absolute performance portability, or causal removal of the historical hitch.

Expected signal:
`scene=solid-576,pairs=4,arms=8,order=AB+BA+BA+AB,hitch=50ms,gate=negative-control`.

Frozen semantic SHA-256:
`7806dcff03e9d9470ac22e35cb3a39c1f837e75e29bae6c7a0addb1738944859`.
