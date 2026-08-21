# Isolated container smoke pool

Worldline can execute independent official-server smoke cycles concurrently in
small Docker containers. This is an opt-in isolation/CI backend, not the
default local Windows backend and not a weaker oracle. For the lighter native
path, see [`HOST_SMOKE_POOL.md`](HOST_SMOKE_POOL.md). Every container receives
the same hash-verified official server JAR and must still produce the smoke's
frozen evidence.

## Why process containers

Minecraft Beta 1.7.3 keeps substantial state in static fields. Multiple games
inside one JVM would share that state and are not a valid isolation boundary.
The pool therefore runs one smoke cycle per container. A cycle may start its
official server and protocol clients inside that container on loopback.

The implementation avoids the expensive parts of a naive Docker design:

- one immutable source image is shared by the whole batch;
- the host runs `Verify` once and the resulting product classes enter that
  image layer;
- each task receives only one Docker-managed evidence volume;
- worlds and compiler output stay on the Linux volume instead of crossing the
  Docker Desktop host-filesystem bridge during execution;
- the official JAR is mounted read-only and is never copied into the image;
- the scheduler uses Java 21 virtual threads while a semaphore enforces the
  admitted container count.

## Run the 25-smoke canary

Start the Docker Linux engine, place the official server JAR at the normal
ignored path, and run:

```text
java tools/containers/ContainerSmokePool.java run tools/containers/smokes-25.tsv --jobs auto
```

The bundled manifest covers M420 through M444. To request an exact width:

```text
java tools/containers/ContainerSmokePool.java run tools/containers/smokes-25.tsv --jobs 10
java tools/containers/ContainerSmokePool.java run tools/containers/smokes-25.tsv --jobs 25
```

The runner refuses a requested width that exceeds its resource admission
calculation. `auto` takes the minimum of 25, four tasks per Docker CPU, and the
available memory after reserving 25 percent (up to 2 GiB) for Docker and the
host. Defaults are a 192 MiB Java heap, 384 MiB container memory, and 0.75 CPU
per task. They can be changed explicitly:

```text
java tools/containers/ContainerSmokePool.java run tasks.tsv \
  --jobs 20 --memory 448m --heap 256m --cpus 0.75
```

Container memory must remain at least 128 MiB above the Java heap. Raise it
when a scenario creates a large world or has multiple live JVMs. Do not lower
limits merely to reach a larger displayed concurrency number.

The first run verifies the host checkout and builds
`worldline/smoke-server:local`. Later runs may use `--skip-build` only when the
source image is known to match the checkout. `--skip-verify` is intended for a
repeated benchmark of an already verified snapshot, not qualification.

## Isolation contract

Every worker has:

- its own JVM process namespace, writable evidence volume, `/tmp`, PID limit,
  memory limit, CPU quota, and file-descriptor limit;
- no external network and no published host port; loopback remains available
  to the server and its wire clients;
- a read-only root filesystem, dropped Linux capabilities, and
  `no-new-privileges`;
- the official JAR mounted from local storage as a read-only file;
- `SerialGC`, a small initial heap, and a bounded maximum heap.

The runner takes an exclusive batch lock within the checkout, measures actual
peak concurrency, records the Docker image ID and admitted capacity, captures
each console log, and copies the complete smoke evidence before deleting the
container and volume. If evidence copying fails, the volume is deliberately
preserved and named in the failure.

Results live under:

```text
.worldline/container-smokes/<UTC batch>/<task>/
```

`batch.properties` records task counts, pass/fail counts, requested and peak
width, Docker resources, and the exact image ID.

## Manifest format

Manifests are strict five-column UTF-8 TSV files:

```text
id  lane  source  smoke-argument  timeout-seconds
```

Only `server-headless` is currently admitted. Sources must be repository
`tools/smoke/*Cycle.java` files, IDs and arguments are bounded, duplicates are
rejected, and timeouts must be between 30 and 3600 seconds. These restrictions
prevent a smoke manifest from becoming an arbitrary host-command interface.

## What remains serialized

This pool intentionally excludes native graphical clients, OpenGL comparison,
RetroMCP mutation, Gradle worktree mutation, and any scenario that writes
outside `.worldline/smokes`. Those resource classes need separate images and
independent isolation proofs. A headless pass does not replace the final native
GUI gate when rendering is part of the contract.

Before increasing a machine from 10 toward 25 workers, run the same frozen
manifest twice at the new width and require identical signatures with no
timeouts, OOM kills, missing evidence, or peak-width violation. Container
parallelism changes scheduling and wall time; it must never change the oracle.
