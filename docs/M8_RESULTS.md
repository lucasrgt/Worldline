# M8 Differential Mod Test Contract

## Scope

M8 makes a mod scenario result durable and directly comparable. A result binds
one inspected compatible mod artifact to one canonical `v2` state trace. Two
results can then be compared without Minecraft, the mod JARs, mapped classes,
RetroMCP, or native libraries.

The independent `worldline-modtest` module depends on the M7 package model, the
M6 canonical trace parser, and the M6 first-divergence analyzer. It does not
implement a second comparison order.

## Canonical `.wlmtest` format

The document is strict UTF-8, LF-only, checksum-protected, at most 6 MiB, and
contains exactly these ordered lines:

```text
WORLDLINE-MOD-TEST/1
mod.id=worldline.example
mod.version=1.0.0
mod.entrypoint=example.ExampleMod
artifact.sha256=<64 lowercase hexadecimal digits>
runtime=b1.7.3
worldline.api=1
trace.sha256=<64 lowercase hexadecimal digits>
trace=<unpadded URL-safe Base64 of the canonical v2 trace>
sha256=<SHA-256 of every preceding line including LF>
```

Parsing validates framing, field order and spelling, metadata grammar, document
checksum, strict Base64/UTF-8, the embedded trace's own SHA-256 and grammar, and
an exact byte-for-byte canonical reconstruction. Embedded trace bytes remain
bounded by the M6 4 MiB CLI boundary.

## Recording

```text
worldline mod test record <mod.jar> <trace.wltrace> <result.wlmtest>
```

Recording re-inspects the JAR for b1.7.3 / mod API 1 compatibility, strictly
parses the trace, creates the result, and uses create-new filesystem semantics:
an existing destination is never overwritten. Exit status is 0 for success, 1
for invalid input or I/O failure, and 2 for usage errors.

The command binds caller-supplied inputs; it does not attest that the supplied
trace was produced by executing that JAR. The M8 smoke supplies that stronger
execution evidence by owning both the runtime process and resulting trace path.

## Differential comparison

```text
worldline mod test diff <left.wlmtest> <right.wlmtest>
```

The output identifies both `id@version` values, whether mod ID, version,
runtime, and API match, both artifact hashes, and the exact M6 first divergence.
Metadata differences remain provenance facts: exit 0 means the embedded traces
are behaviorally equal, while exit 3 means they diverge. Invalid/corrupt input
exits 1 and usage errors exit 2. Left/right values preserve command order.

## Non-claims

M8 does not execute mods from the neutral CLI, authenticate caller-supplied
traces, compare arbitrary heap or mod-private state, prove compatibility across
different Minecraft runtimes, resolve dependencies, minimize a divergent
scenario, or claim Aero compatibility. Runtime/version matrix execution and
automatic minimization require later explicit evidence.
