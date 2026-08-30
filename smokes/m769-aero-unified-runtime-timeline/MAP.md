<!-- worldline-map-schema=1 -->
<!-- boundary=profiler-jfr-timeline -->
<!-- nonclaims=gpu-time,optimization-verdict,cross-machine-generality -->
<!-- frozen-trace=9c8a961cea0ddf9ebf0b0734f32c44ae8f8424667367cb94b4186574f5ebe8c4 -->

# M769 Aero unified runtime timeline behavior map

The boundary is one real-client retained interval over the restored solid
sixteen-floor, 576-machine Aero tower. A canonical WLPR complete-frame census
and a JFR recording share periodic custom anchors containing frame sequence,
monotonic nanoseconds, epoch milliseconds, and route phase.

The verifier joins JFR garbage collection, safepoint, allocation-sample, and
file-I/O events to WLPR frames. WLPR simultaneously carries tick, render,
display, chunk, JVM, save, Aero enqueue, Aero flush, and page counters.

Expected signal:

`scene=solid-576,timeline=wlpr+jfr,events=gc+safepoint+allocation+file-io,join=frame`

Frozen trace:

`v1|scene=mega-solid-16x4x3x3-576|arm=solid-aero-save|retained-min=180s|wlpr=complete-frame|jfr=profile+anchors|join=monotonic-epoch|events=gc+safepoint+allocation+file-io|aero=save+enqueue+flush+pages|route=stationary+look-jump-spin+stationary|observer=single-process|cleanup=normal`

This milestone qualifies acquisition and frame-level temporal correlation. It
does not claim GPU time, an optimization verdict, causal generality beyond the
fixture, or cross-machine performance.
