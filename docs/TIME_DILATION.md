# Time Dilation Proof Program

Optimization `worldline.runtime.time-dilation` scaffolds uniform wall-clock
dilation for pooled Linux containers. The official Beta 1.7.3 server paces
itself with real sleeps, so a dilated monotonic clock finishes the same tick
work in less real time. The scaffold is default-off and fails closed: when
`tools/containers/time-dilation.properties` is absent, every container
invocation stays byte-identical to the undilated form.

## Scope

- Only pooled container executions (`ContainerSmokePool`) consult the
  configuration. Host pools, the canonical serial lease, and every
  non-container path never dilate.
- Dilation applies to the whole container: harness, wire client, and official
  server share one `LD_PRELOAD` libfaketime environment, so relative pacing
  between processes is preserved.
- The official JAR, world data, and every observation stay untouched; only
  `CLOCK_*` reads inside the container scale.

## Configuration contract

`tools/containers/time-dilation.properties` (absent by default):

```properties
schema=1
factor=20
preload=/usr/lib/faketime/libfaketime.so
```

`factor` must be an integer between 1 and 40; `1` keeps dilation disabled.
`preload` must be an existing absolute `.so` path on the container host. Any
invalid value fails the pool run closed.

## Required proof before any activation

The record stays `status=candidate` and `default.enabled=false` until all of
the following hold on the Linux lane:

1. Differential signatures: a reviewed smoke set runs undilated and dilated;
   every behavioral signature and observation hash must be byte-identical.
2. Await telemetry: dilated runs must not introduce await failures, retries,
   or timeout expansions relative to the undilated baseline.
3. Latency evidence: recorded wall-clock savings and the dilation factor are
   published with the differential receipts.

Dilated executions must never mint qualification evidence until the record is
promoted to `status=active` with that differential proof attached.
