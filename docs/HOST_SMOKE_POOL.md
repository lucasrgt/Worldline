# Native host smoke pool

The host pool is Runtime Fabric's low-overhead execution engine. On Windows it
places each complete child tree in a native Job Object. On Linux it delegates
each tree to cgroups v2, optionally adding namespaces through Bubblewrap. It
avoids the Docker Desktop VM, overlay filesystem, daemon, and Linux bridge.
Docker remains available when stronger filesystem and resource isolation is
more important than local Windows throughput.

## Simulate before running

Resource simulation never starts Minecraft:

```text
java tools/containers/HostSmokePool.java simulate tools/containers/smokes-25.tsv --jobs auto
java tools/containers/HostSmokePool.java simulate tools/containers/smokes-25.tsv --jobs 10
java tools/containers/HostSmokePool.java simulate tools/containers/smokes-25.tsv --jobs 25
```

The model reads the host's current logical CPU count, total physical memory,
and free physical memory. It combines those measurements with:

- a conservative total-memory estimate per smoke worker;
- the Java heap applied to both the cycle JVM and its official child JVM;
- a CPU-unit estimate per worker;
- memory reserved for the operating system and unrelated applications;
- the configured maximum parallelism and estimated task duration.

For each width it prints the number of waves, concurrent estimated RAM, CPU
demand, idealized duration, and `SAFE` or `REJECT`. The duration is a planning
estimate, not a benchmark. Admission is deliberately based on current free
memory so a width can be accepted on an idle machine and rejected while other
large workloads are active.

## Configure parallelism

Tracked defaults live in `tools/containers/host-pool.properties`. The default
width is 10 and the hard ceiling is 25. Configuration precedence, highest
first, is:

1. `--jobs auto|N`;
2. `WORLDLINE_SMOKE_JOBS`;
3. a file passed through `--config`;
4. ignored local `.worldline/host-pool.properties`;
5. tracked repository defaults.

Example local configuration:

```properties
parallelism=10
runtime.lock.path=C:/path/to/shared/official-runtime.lock
```

Use `--jobs 1` as the serial fallback. `auto` uses the largest currently safe
width up to `max.parallelism`. Explicit widths are not permission to
overcommit: `run` rejects any value above current admission.

The default resource assumptions are:

```properties
worker.heap=192m
worker.memory.estimate=448m
worker.memory.limit=3g
worker.cpu.units=0.50
worker.process.limit=64
host.memory.reserve=4g
task.duration.seconds=90
```

The worker estimate must cover two configured heaps plus at least 64 MiB of
native/runtime overhead. Tune estimates upward from observed peak resident
memory; do not tune them downward merely to admit more workers.

## Run

```text
java tools/containers/HostSmokePool.java run tools/containers/smokes-25.tsv --jobs 10
```

Before launching, the runner:

1. validates the strict five-column smoke manifest;
2. calculates admission from current resources;
3. acquires the configured cross-process official-runtime lock;
4. rejects an already active smoke, official JAR, or `runClient` process;
5. runs the canonical non-runtime `Verify` gate once;
6. checks for foreign official runtimes again.

Every admitted task receives a unique console log, temporary directory,
Gradle home, slot identity, and smoke evidence directory. The official JAR is
shared read-only by convention and verified by each smoke. `JAVA_TOOL_OPTIONS`
applies a small heap and `SerialGC` to the cycle and its child JVM. Timeouts
forcibly terminate the entire descendant process tree.

The scheduler uses Java 21 virtual threads plus a semaphore, measures actual
peak concurrency, and saves results under:

```text
.worldline/host-smokes/<UTC batch>/<task>/
```

Each successful task's evidence is copied into its immutable batch directory.
`batch.properties` records backend, tasks, pass/fail counts, requested width,
observed peak, host resources, admitted width, and elapsed time.

## Isolation boundary

Separate processes isolate Minecraft's static state. Unique workspaces isolate
worlds, classes, logs, caches, and temporary files. Existing server smokes
choose ephemeral loopback ports; the frozen 25-smoke canary is required to
demonstrate that this remains collision-free at each promoted width.

Windows Job Objects provide process-tree ownership, hard ceilings, timeout
termination, and accounting, but are not a filesystem security boundary.
The `windows-client-gui` lane assigns each complete two-replica cycle to one
Job Object, reserves a larger heap/memory/CPU profile, requires the Windows
backend, rejects mixed-lane manifests, and permits at most three concurrent
cycles. Start its promotion at two jobs. A pinned Aero artifact is built once
before admission. The prebuild and every GUI milestone use separate persistent
Gradle homes, while project worktrees, temporary paths, logs, worlds, and
evidence directories remain isolated as well.
Linux cgroups provide equivalent tree/resource ownership; `linux-sandbox`
adds read-only mounts and private namespaces. Native GUI, OpenGL, RetroMCP
mutation, Gradle worktree mutation, and client-oracle lanes retain their own
smaller or serial resource classes. The official-runtime lock must point at
the same shared file used by external coordinators; otherwise a noncompliant
process can begin after the preflight scan.

Promote concurrency in stages: 1, 10, 16, then 25. At every stage, run the
same frozen queue twice and require identical signatures, no port collision,
no timeout, no missing evidence, no OOM, and an observed peak no greater than
the configured width.
