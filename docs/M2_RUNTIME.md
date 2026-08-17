# M2 Controlled Runtime Boundaries

M2 promotes the process boundaries that make a headless Beta 1.7.3 client
tick deterministic. It does not add types to `worldline-api`. The public
lifecycle remains `MinecraftRuntime`. Boundary controls stay on the b1.7.3
adapter (`B173Runtime` and its collaborators).

## Stable contracts

| Boundary | Stable M2 behavior |
| --- | --- |
| Clock | `B173VirtualClock` starts at a fixed millisecond origin. Each controlled tick advances it by 50 ms. Instrumented client `System.currentTimeMillis` calls read the hook only while the runtime is installed. Outside controlled mode the hook returns wall time. |
| Input | Programmable keyboard and mouse queues preserve vanilla event-loop semantics. `key`, `tap`, and `mouse` enqueue events that the next `runTick` consumes. |
| RNG | Vanilla `Random` implementations remain. Controlled world and player streams are explicitly reseeded; the seed is part of the v2 state trace. |
| Filesystem | World persistence is an in-memory journal. Load records `world.loadInfo` and `chunk.load`. `failNext` injects a one-shot failure for a named operation. |
| Network | An offline `Session` is installed. `networkConnected()` is always false. No client network handler, socket, resource downloader, or server is constructed. |
| Scheduler | `B173Scheduler.afterTicks` queues actions on the controlled client thread. Actions run at the start of the target tick, in insertion order. Snapshots require a drained queue. |
| Threading | Headless boot captures the vanilla daemon timer-hack thread, observes it alive during the run, and interrupts/joins it on close. |

The first-cycle fixture and official-JAR differential that prove these
boundaries are normative in `smokes/controlled-client-tick/MAP.md`.

## Concurrency

Boundary controls are designed for the single externally controlled runtime
thread. Concurrent calls, pending `Runnable` serialization, and replacing
arbitrary JVM threads are outside M2.

## Non-claims

M2 does not virtualize audio, OpenGL, or a real network stack. It does not
replace wall time for code that never hits the installed clock hook. It does
not load arbitrary saved worlds from disk, serialize pending scheduler
callbacks, or claim that every Minecraft thread is supervised. Window/LWJGL
headlessness remains a v0.0.1 first-cycle claim. Later milestones reuse these
controls; they do not widen this contract.
