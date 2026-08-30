<!-- worldline-map-schema=1 -->
<!-- boundary=gpu-driver-present-attribution -->
<!-- nonclaims=optimization-verdict,cross-machine-generality,glfinish-production-use -->
<!-- frozen-trace=3fab7170a84da1b7d26206d9eb489ff4d7a2b38247f9ad0d4097760b722e0c11 -->

# M770 Aero GPU and present attribution behavior map

The boundary is three fresh real-client retained intervals over one restored
solid sixteen-floor, 576-machine Aero tower. Every arm records a canonical
WLPR complete-frame census and a separate checksum-protected map from frame
sequence to `GL_TIME_ELAPSED` timer-query nanoseconds.

The async arm leaves the driver pipeline unsynchronized. The two diagnostic
arms call `glFinish` immediately before `Display.update`; one keeps VSync off
and the other enables it. This separates forced driver/GPU drain from swap,
event processing, and vertical-blank wait without calling CPU render spans
GPU time.

Expected signal:

`scene=solid-576,arms=query-async+finish-off+finish-vsync,gpu=timer-query,display=isolated`

Frozen trace:

`v1|scene=mega-solid-16x4x3x3-576|arms=query-async-off+finish-off+finish-vsync|retained-min=60s-each|wlpr=complete-frame|gpu=arb-timer-query-frame-map|finish=pre-display-update|display=swap+events+vblank|vsync=off+off+on|query=nonblocking-ring-64|route=stationary+look-spin|observer=fresh-process-per-arm|cleanup=normal`

`glFinish` is an intentionally intrusive attribution probe, not a production
optimization. The milestone does not claim an optimization verdict or
cross-machine timing generality.
