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
| `windows-job` | native Windows | Job Object assigned before child resume; tree kill, CPU, memory, process limits, accounting | per-slot paths and loopback; no security boundary | default on Windows |
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
java tools/harness/Verify.java --runtime
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
