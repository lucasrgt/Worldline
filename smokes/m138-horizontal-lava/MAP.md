<!-- worldline-map-schema=1 -->
<!-- boundary=m138-horizontal-lava -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=f1d5832ac76c05b0cc786b294c8f29126f9d0a668c6326ca2ecae17b2824a760 -->

# M138 behavior map

The fixture raises a two-cell stone trench away from generated vegetation. A
still source `11:0` faces a dirt gate `3:0`; all other exits are stone. Packet14
removes the gate and Packet53 first confirms exact air. After 70 heartbeats,
the target is official lava `11:2`.

Only source and target enter the causal state hash. The source remains `11:0`
and the target contributes the sole persisted delta. Random ticks elsewhere in
the chunk are explicitly outside the evidence.

Frozen semantic SHA-256:
`f1d5832ac76c05b0cc786b294c8f29126f9d0a668c6326ca2ecae17b2824a760`.
