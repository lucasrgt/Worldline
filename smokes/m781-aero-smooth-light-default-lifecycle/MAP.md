<!-- worldline-map-schema=1 -->
<!-- boundary=aero-smooth-light-default-lifecycle -->
<!-- nonclaims=gpu-performance-or-minecraft-rendering -->
<!-- frozen-trace=8ebf6d22267e4e6fd22f47dc275a5993b15d9b7c7188ea3cf69dd35ec6feef4d -->

# M781 Aero smooth-light default and lifecycle behavior map

The qualified boundary is the startup property policy and the single-active-world
lifecycle of Aero's production `Aero_SmoothLightCache` at revision
`06c0c22ce15454b45b14597332a92241fef0931e`.

The external Java 8 probe establishes these exact behaviors:

- no `aero.smoothlight.cache` property enables the cache;
- an explicit `false` disables it;
- an explicit `true` enables it;
- repeated claims in one world reuse the same color array;
- selecting a different world drops all entries from the previous world; and
- selecting the old world again cannot recover values from either generation.

The six fresh JVM arms are counterbalanced as
`default, false, true, true, false, default`. The runner pins a clean external
checkout, hashes both production source and probe, compiles with `javac --release 8`,
and compares every observation exactly.

This cycle does not launch Minecraft, render a frame, measure GPU performance, or
publish any visual-equivalence claim. Those concerns remain bounded by M780 and its
runtime qualification evidence.

Frozen trace:

`v1|consumer=aero-model-lib|revision=06c0c22ce15454b45b14597332a92241fef0931e|compile=javac-release8|jvms=6-fresh-default+false+true+true+false+default|default=enabled|explicit-false=disabled|explicit-true=enabled|same-world=array-reuse|world-switch=entries1-to0+old-world-miss|oracle=exact`

Frozen signal:

`consumer=aero-model-lib,revision=06c0c22ce15454b45b14597332a92241fef0931e,compile=javac-release8,jvms=6-fresh-counterbalanced,default=enabled,explicit-false=disabled,explicit-true=enabled,same-world=array-reuse,world-switch=entries1-to0+old-world-miss,oracle=exact`

Frozen SHA-256: `8ebf6d22267e4e6fd22f47dc275a5993b15d9b7c7188ea3cf69dd35ec6feef4d`.
