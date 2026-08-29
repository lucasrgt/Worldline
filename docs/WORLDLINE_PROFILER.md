# Worldline Profiler

Worldline Profiler is the generic measurement boundary for Minecraft runtimes and
mods. It records complete frame rows, preserves which signals a driver can
actually observe, and produces deterministic evidence suitable for local
diagnosis, CI regression gates, and external dashboards.

It replaces smoke-private hitch counters as the reusable contract. A game driver
owns vanilla/runtime interception; a mod adapter owns only `mod.*` extension
metrics. No generic Minecraft hook belongs in a mod repository.

## Architecture

The profiler has five layers:

1. `WorldlineProfilerMetrics` is the neutral catalog. Its names are stable and
   cover frame, tick, world, chunk, render, display, JVM, I/O, network, thread,
   and streaming activity.
2. `ProfilerRegistry` declares the exact capabilities of one runtime. An absent
   metric means unsupported. A present value of zero means observed zero work.
3. `ProfilerSession` and `ProfilerRecorder` capture bounded, contiguous frames
   through preallocated primitive storage. Instrumented paths retain metric
   handles, so recording needs no map lookup or per-event object.
4. `ProfilerRun` and `ProfilerRunCodec` seal the schema, complete census, capture
   mode, epoch window, environment tags, and SHA-256 integrity into one canonical
   artifact.
5. `ProfilerSummary`, `ProfilerAttribution`, `ProfilerBudgetPolicy`,
   `ProfilerComparison`, and `ProfilerExport` turn evidence into aggregates,
   causal labels, release decisions, A/B verdicts, JSON, and OpenMetrics.

## Metric model

Every metric has five independent dimensions:

- name: a stable dotted identity such as `chunk.generate.nanos`;
- owner: `worldline` for driver signals or the mod id for an extension;
- unit: nanoseconds, bytes, count, or parts per million;
- kind: duration, interval delta, or point-in-time gauge;
- causality: root, top-level, nested, or diagnostic.

Category is derived deterministically from ownership and the first name segment.
This prevents schema files from disagreeing about whether a metric belongs to
render, chunk, JVM, or another category.

Extension metrics must have a non-Worldline owner and a `mod.*` name. Duplicate
names, unknown core names, invalid units, foreign handles, incomplete frames,
noncontiguous sequences, nonmonotonic clocks, negative values, corrupt artifacts,
and schema/census drift fail closed.

## Driver integration

At initialization, a driver builds one registry. It registers only hooks that
resolved successfully for that runtime and may add the available MXBean signals:

```java
ProfilerRegistry.Builder capabilities = ProfilerRegistry.builder()
    .support(WorldlineProfilerMetrics.FRAME_WALL,
             WorldlineProfilerMetrics.CLIENT_TICK,
             WorldlineProfilerMetrics.RENDER_CAMERA,
             WorldlineProfilerMetrics.DISPLAY_PRESENT);
JvmProfilerSampler.registerCapabilities(capabilities);
ProfilerRegistry registry = capabilities.build();
ProfilerSession session = new ProfilerSession(
    registry, captureFrames, new JvmProfilerSampler(registry));
```

Resolve handles once, outside the measured loop. A frame root opens and closes
the session; nested hooks add elapsed time to their retained handle:

```java
ProfilerRegistry.Handle render =
    registry.require(WorldlineProfilerMetrics.RENDER_CAMERA);

session.beginFrame(sequence, frameStartNanos);
long started = session.startTimer();
// The runtime executes the instrumented render stage here.
session.addElapsed(render, started, System.nanoTime());
session.endFrame(System.nanoTime());
```

Nested metrics may overlap. Never add them to infer total frame time. Causal
groups deliberately use the maximum within a group, and top-level attribution
is the only input to unattributed wall time.

## Mod extension integration

A mod publishes typed metrics during registry construction and retains the
returned handle after the driver finalizes the registry:

```java
ProfilerMetric draws = WorldlineProfilerMetrics.extensionCounter(
    "mod.example.models.count", "example-mod");
capabilities.extension(draws);
```

The driver owns lifecycle, time source, frame identity, capacity, and artifact
sealing. The extension only contributes measurements. This keeps multiple mods
composable and prevents one extension from impersonating a vanilla signal.

## StationAPI runtime binding

The StationAPI driver is the first production runtime binding. Enable it with
`-Dworldline.profiler.enabled=true`, choose a bounded row capacity with
`worldline.profiler.capacity`, and set `worldline.profiler.output` to a `.wlpr`
target. It records renderer frames, client ticks, display present, world render,
chunk compilation/rebuilds and queue depth, plus available JVM signals.
Automated drivers may set `worldline.profiler.autoStart=false` and arm capture
only after their world-readiness oracle, excluding startup and login noise.

Beta 1.7.3 performs tick and `Display.update` before the renderer frame root, so
those durations are accumulated and attributed to the following frame. Mods may
register an owned `mod.*` metric through
`worldline.profiling.ClientProfiler.register(...)`
before the first frame, then publish through its stable token while
`ClientProfiler.active()` is true. Schema registration closes permanently
when capture begins.

## ModLoader and Forge runtime binding

The Java 8-compatible `ClientProfilerRuntime` is shared by StationAPI,
ModLoader, and Forge;
there is no second metric implementation or artifact dialect. Legacy clients use
the source-injection hooks under `adapters/modloader-forge` because their
RetroMCP pipeline has no Mixin runtime. The six mapped boundaries mirror the
StationAPI capabilities: frame, tick, display present, world render, chunk
compile, and chunk rebuild. `worldline.profiler.loader` distinguishes
`modloader` from `forge` in sealed tags.

The hook boundary and Java 8 closure are maintained structurally. Official
ModLoader/Forge boot remains a separate qualification requirement and is not
inferred from the StationAPI runtime receipt.

| Client ecosystem | Binding | Current proof |
| --- | --- | --- |
| StationAPI/Babric | Mixin driver | official b1.7.3 two-session runtime receipt |
| ModLoader | RetroMCP source hooks | Java 8 compile plus loader-neutral artifact test |
| Forge | RetroMCP source hooks | Java 8 compile plus loader-neutral artifact test |

All three bindings expose the same owned `mod.*` extension API and emit the
same WLPR schema; loader identity is metadata, not a fork of the profiler.

## Capture modes and qualification

- `STEADY` is valid only when configured activity counters remain zero.
- `STREAMING` intentionally includes load, generation, population, save, compile,
  rebuild, teleport, or equivalent world movement.
- `MIXED` is diagnostic and must not be compared with a steady baseline.

Use environment tags for runtime version, driver id, loader version, mod set,
scenario, seed, renderer settings, JVM, hardware identity, warmup policy, and a
JFR recording id when one exists. The run epoch window is the correlation key
for JFR, GC logs, driver logs, and OS telemetry; the Java 8 core does not depend
on a particular JFR implementation.

## Analysis and release policy

Budgets name the metric, statistic (`MEAN`, `P95`, `P99`, or `MAX`), limit, and
severity. Findings report stable `budget.notice`, `budget.warning`, or
`budget.critical` codes plus exact excess. Missing budget metrics fail closed.

A/B comparison requires the metric on both runs and applies an explicit absolute
and relative noise band. The result is `IMPROVEMENT`, `EQUIVALENT`, or
`REGRESSION`, with baseline, candidate, absolute delta, relative ppm, and the
effective noise allowance. Baseline and candidate must use the same machine,
scenario, mode, warmup, view, JVM, and driver settings; validate those tags in
the campaign before accepting a verdict.

Attribution labels a hitch `unknown`, one causal group, or `mixed`. Unknown is a
valid result: it identifies missing instrumentation instead of forcing a false
cause.

## Integrations

- The binary `WLPR` artifact is canonical, bounded, checksummed, and replayable.
- JSON provides deterministic summaries for CI artifacts and report UIs.
- OpenMetrics provides mean, p95, and max series with owner, category, and
  statistic labels. Run/environment tags are intentionally omitted to avoid
  accidental high-cardinality production metrics.
- JFR and external traces correlate through capture epoch and explicit tags.
- Mod integrations use owned `mod.*` metrics rather than runtime-specific casts.

The CLI makes sealed evidence usable without application code:

```text
worldline profiler inspect capture.wlpr
worldline profiler export json capture.wlpr report.json
worldline profiler export openmetrics capture.wlpr metrics.txt
worldline profiler compare frame.wall.nanos p95 baseline.wlpr candidate.wlpr 100000 20000
```

The final comparison arguments are absolute noise in the metric unit and relative
noise in parts per million. A regression exits with status 3, matching Worldline's
other evidence-diff commands; malformed or corrupt evidence exits with status 1.

The profiler does not claim GPU time from CPU timing. GPU attribution requires a
driver capability backed by timer queries and a delayed-result collector. It
also does not treat FPS as sufficient evidence: frame percentiles, hitch counts,
causal stages, allocation, GC, streaming activity, and A/B controls remain the
release inputs.

## Operational invariants

- Warm up before the measured window and preserve warmup policy as evidence.
- Preallocate a bounded capacity; capacity exhaustion is an error, not silent
  sample loss.
- Use one monotonic clock on the measured thread.
- Seal only complete frames and retain the original artifact alongside exports.
- Separate steady and streaming campaigns.
- Compare optimizations individually before enabling the combined profile.
- Keep observer overhead measurable with a profiler-off control run.
- Promote an optimization only after deterministic correctness and runtime gates.

The module and CLI suites exercise the full structural contract. The
`controlled-client-tick` official/mapped differential is rerun when profiler
module closure changes, proving that profiler evolution does not alter the
canonical b1.7.3 client trace or state signature.
