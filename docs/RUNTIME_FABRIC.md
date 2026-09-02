# Worldline Runtime Fabric

Runtime Fabric is the common admission and isolation boundary for parallel
official-runtime evidence. It keeps the manifest, scheduler, batch lease,
timeouts, logs, metrics, and evidence contract consistent while choosing the
lowest-overhead backend that the host can actually prove.

```text
java tools/containers/RuntimeFabric.java doctor
java tools/containers/RuntimeFabric.java simulate tools/containers/smokes-25.tsv --backend auto --jobs auto
java tools/containers/RuntimeFabric.java run tools/containers/smokes-25.tsv --backend auto --jobs 10
```

`--jobs` accepts `auto` or 1 through 25. `--isolation` accepts `fast`,
`balanced`, or `sealed`. An unavailable requested capability is an error; the
fabric never silently weakens isolation.

## Backends

| Backend | Intended host | Process/resource boundary | Filesystem/network boundary | Status |
| --- | --- | --- | --- | --- |
| `rust-windows-job` | native Windows | Rust-owned Job Object assigned before child resume; tree kill, CPU, memory, process limits, accounting, canonical receipt | explicit environment, per-slot paths, inherited loopback; no filesystem security boundary | default for `server-headless` on Windows |
| `windows-job` | native Windows | legacy C# Job Object with the same process controls | per-slot paths and loopback; no security boundary | explicit rollback |
| `windows-appcontainer` | native Windows | Job Object plus AppContainer | restricted token and capability ACLs | experimental, fail-closed until Java/loopback probe passes |
| `linux-cgroup` | native Linux or WSL2 ext4 | delegated systemd cgroup v2 service; memory, CPU, tasks, tree kill | per-slot paths and host loopback | default on Linux |
| `linux-sandbox` | native Linux or WSL2 ext4 | cgroup v2 plus user/PID/IPC/UTS/network namespaces | read-only root, writable `.worldline`, private loopback | sealed Linux mode |
| `docker` | CI or either desktop | container cgroup/PID boundary | immutable image, read-only oracle, private volume/network | explicit fallback |

All backends enter official execution through `Gate --milestone`. Docker
workers receive a read-only, exact-commit capability and tracked-file manifest;
their writable `/runtime` tmpfs is exposed to the canonical Gate through
symlinks for harness classes, reports, logs, and receipt objects. The image is
otherwise read-only, has no network, and receives the verified server JAR as a
read-only bind. Direct `tools/smoke/*.java` execution is not a backend escape.

The Windows runner is distributed as C# source. `WindowsJobBootstrap.java`
compiles it into ignored `.worldline/tools` storage and tests it with a
synthetic process. No binary is committed. The child is created suspended,
assigned to the Job Object, and only then resumed, so descendants cannot race
outside the tree boundary.

On native Windows, `auto` selects `rust-windows-job` for `fast` and
`balanced`. A missing or invalid `WORLDLINE_EXECUTOR` fails closed; it never
silently falls back. Operators may select `--backend windows-job` explicitly
while the rollback remains supported. The normal pool capability value is
injected only into the native launcher's environment. Its name is inherited by
the child plan, but its value is absent from plans, receipts, metrics, and
logs. GUI manifests remain ineligible for the Rust default until their broader
driver profiles qualify and therefore require explicit `windows-job`.

The Linux launcher requires cgroups v2 and a delegated systemd user manager.
Sealed mode additionally requires Bubblewrap; it creates a
private network namespace and enables only its loopback interface. A missing
tool is a hard failure.

## WSL2 layout

WSL2 is a good Linux/Windows bridge, but dense runs must live in the
distribution's ext4 filesystem, for example `~/src/worldline`. Running a pool
from `/mnt/c` is rejected because metadata and cross-VM filesystem traffic
destroy the density advantage. WSL distributions share one lightweight VM;
per-slot isolation comes from cgroups and namespaces, not from pretending each
distribution is a separate VM.

The distribution needs a Linux JDK 21, systemd with a delegated user manager,
and Bubblewrap for sealed mode. A Windows JDK cannot execute inside Linux
namespaces. `doctor` reports WSL separately so an installed distribution is
not mistaken for a ready runtime.

### Ubuntu setup from Windows

Run the following in PowerShell. Package installation is the only command that
runs as the WSL root user:

```powershell
wsl --install -d Ubuntu
wsl -d Ubuntu -u root -- bash -lc 'apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends openjdk-21-jdk-headless bubblewrap git'
wsl -d Ubuntu -- bash -lc 'java -version && javac -version && systemctl --user show-environment >/dev/null && bwrap --version'
```

The canonical repository gate also requires `tokei` 14 or newer. Ubuntu's
distribution package may be older; when necessary, install the pinned major
through an existing Rust toolchain, then confirm its version:

```bash
cargo install --locked --version 14.0.0 tokei
tokei --version
```

Do not reuse the checkout under `C:\` through `/mnt/c`. Create the execution
checkout inside WSL's ext4 virtual disk:

```powershell
wsl -d Ubuntu -- bash -lc 'mkdir -p ~/src && cd ~/src && git clone https://github.com/lucasrgt/Worldline.git worldline'
wsl -d Ubuntu -- bash -lc 'cd ~/src/worldline && java tools/containers/RuntimeFabric.java doctor'
```

The clone intentionally does not contain proprietary runtime inputs. Acquire
hash-verified local copies inside that checkout, or copy already verified
files into its ignored `local/artifacts/` directory:

```bash
cd ~/src/worldline
java tools/artifacts/Acquire.java all
java tools/toolchains/Bootstrap.java retromcp
java tools/harness/Gate.java --runtime
```

Validate both Linux isolation tiers without starting Minecraft:

```bash
cd ~/src/worldline
bash tools/containers/linux-runtime.sh --self-test
java tools/containers/RuntimeFabric.java simulate tools/containers/smokes-25.tsv --jobs auto
```

Expected `doctor` state is `linux-cgroup AVAILABLE` and `linux-sandbox
AVAILABLE`. From a Windows-side invocation it should report `wsl2=installed;
java=ready; cgroup+sandbox=ready`. A checkout under `/mnt` remains deliberately
ineligible even when all packages are installed.

## Resource profiles

Tracked defaults are in `tools/containers/host-pool.properties`; local
overrides belong in ignored `.worldline/host-pool.properties`.

```properties
parallelism=10
max.parallelism=25
backend=auto
worker.heap=192m
worker.memory.estimate=448m
worker.memory.limit=3g
worker.cpu.units=0.50
worker.process.limit=64
host.memory.reserve=4g
```

The estimate drives admission; the larger hard limit is a safety ceiling for
short Gradle/Minecraft bursts, not reserved memory. Reduce it only after peak
metrics prove that the complete child tree fits. Promotion is staged at 1,
10, 16, then 25, with two frozen repetitions and identical signatures.

## Capability classes

The bundled manifests grant parallel permission only to declared
`server-headless` and `windows-client-gui` cycles. GUI/OpenGL batches require
the native `windows-job` backend, contain a single lane, and are capped at
three jobs; promote them at two jobs before attempting three. Replicas inside
one cycle remain sequential. `client-headless`, mutable toolchains, mixed
manifests, and unknown lanes fail closed. New lanes must define their network,
filesystem, process, and evidence needs and pass a cross-backend conformance
run before entering `auto` selection.

The first frozen GUI canary is:

```text
java tools/containers/RuntimeFabric.java simulate tools/containers/aero-gui-m104-m110.tsv --backend windows-job --jobs 2
java tools/containers/RuntimeFabric.java run tools/containers/aero-gui-m104-m110.tsv --backend windows-job --jobs 2
```

The private nightly workflow routes missing `server-headless` proofs explicitly
through Docker and missing `windows-client-gui` proofs explicitly through the
Windows Job Object. A missing backend is fail-closed. Runtime Fabric accelerates
evidence; it does not relax the official JAR oracle, canonical verification,
mapping review, or frozen-signature requirements.

## Native executor qualification

The structural Candidate promotion compares three static milestone builds
without starting Minecraft:

```text
java tools/harness/Gate.java --executor-candidate
```

The closed matrix covers powered-rail propagation, player respawn, and the
multiplayer door-sound event. Legacy and Rust arms use independent disposable
exact-commit worktrees and private build/test caches. Within each arm, every
workload receives a separate `Gate --candidate`, output root, plan, compiled
class digest, and telemetry sample. Promotion requires byte-equal legacy/Rust
class evidence, complete output, zero descendants, and a lower-only admission
floor that cannot increase worker width.

Private immutable module and test cache publication retries only Windows
`AccessDenied` from an absent-target sibling move. The retry is bounded to
eight attempts with less than one second of total backoff. An occupied target,
an invalid staging tree, interruption, or exhaustion fails closed; the caller
then revalidates the exact `.complete` digest. No retry may replace an existing
cache entry.

The Windows server promotion cycle compares the legacy C# Job Object launcher
with the Rust Windows Job backend under one continuous official-runtime lease:

```text
java tools/harness/Gate.java --executor-server
```

Disposable server cells classify their preparatory full Gate as qualification work through the
closed `executor-server-prepare` marker. Content checks remain unchanged, while ordinary restored
Gates retain the hot total and per-stage latency ratchets.
Failed server legs preserve only their known local logs, output manifests, receipts, and metrics
under `.worldline/executor-server-failures/` before the disposable worktree is removed. These
diagnostics remain ignored, non-portable, and non-authoritative.

Failed native GUI and Aero legs apply the same pre-cleanup boundary under
`.worldline/executor-gui-failures/` and `.worldline/executor-aero-failures/`.
Only the closed console, smoke log, legacy metrics, Rust streams, metrics,
manifest, receipt, plan, verification report, and failure metadata set may be
retained. Capture failure is suppressed onto the original qualification error;
it can never replace the runtime verdict.

The closed matrix covers dedicated-server bootstrap, an offline protocol-14
multiplayer session, persisted Nether login, same-dimension player respawn, a
peer-observed wooden-door sound event, and powered-rail propagation across a
slope boundary.
The multiplayer contract projects its stable protocol/session signal while
retaining the server-assigned entity ID only as a diagnostic; that volatile ID
cannot enter repeat equality or canonical evidence. Each arm uses one
independent disposable exact-commit worktree, an explicit environment, a
locally verified server JAR, and per-behavior output roots. Every milestone
still enters through `Gate --milestone`; the cycle requires byte-equal
canonical evidence, complete Rust output, and zero remaining descendants
before recording observational telemetry. The Windows profile observes
explicit loopback use under inherited network access and does not claim
OS-enforced network isolation.

That matrix also authorizes the first A8 promotion: the Windows
`server-headless` pool uses `rust-windows-job` under `auto`, while the legacy
launcher remains an explicit rollback. Host-pool task plans preserve bounded
stdout and stderr, telemetry, output manifests, canonical receipts, and the
zero-descendant invariant for every milestone invocation. This promotion does
not cover GUI lanes, retire the C# source, or strengthen Windows filesystem or
network isolation claims.

## Cross-platform workspace transaction qualification

The deferred WPM materializer depends on one atomic no-replace workspace
primitive. Its explicit structural qualification runs the same fixture on
native Windows NTFS and inside WSL on Linux ext4, then requires one identical
content digest:

```text
java tools/harness/Gate.java --workspace-transaction
```

This profile requires `wsl.exe`, a Linux JDK 21, `findmnt`, and an ext4
`/var/tmp`. It is intentionally separate from the canonical Gate so ordinary
Windows verification does not acquire a WSL dependency. The fixture proves
atomic sibling promotion, collision preservation, digest equality, and known
path cleanup. The separate WPM repository has its own closed staging,
content-addressed acquisition, generation update, and two-way rollback path.
Its 16-test corpus passes on native Windows NTFS and WSL Linux with transaction
temporaries placed on ext4 at implementation commit `2c945ab`; WPM records the
qualification at `c52e817`. It does not call this Worldline-owned primitive,
so the two implementations remain independent invariant evidence rather than
shared code. Worldline consumes only WPM's bounded post-install attestation and
retains independent official-artifact policy.
