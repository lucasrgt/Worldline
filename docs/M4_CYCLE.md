# Worldline v0.2.0 M4 Completion Audit

Status: **GO**

| Requirement | Authoritative evidence | Result |
| --- | --- | --- |
| Neutral API | `SnapshotMinecraftRuntime` and bounded immutable `RuntimeSnapshot` compile without adapter classes | PASS |
| Durable format | Canonical versioned UTF-8 document contains reconstruction inputs, events, fingerprint, and checksum | PASS |
| Deterministic capture | Two fresh capture JVMs produce byte-identical artifacts | PASS |
| Cross-process restore | Two fresh restore JVMs reproduce the captured fingerprint and exact artifact | PASS |
| Vanilla behavior | Restored tick-4 state equals the direct official-client oracle | PASS |
| Input safety | Modified state, unknown version, and wrong runtime inputs fail their specific validation boundaries | PASS |
| Regression safety | Earlier server, client, M3, and laboratory signatures remain frozen | PASS |

Frozen snapshot SHA-256:

```text
a6e6589f9fdac1e40170f7a3b7fca7fc06b643b20a86249a464f9b2ab5b53bd2
```

Canonical qualification command:

```text
java tools/harness/Verify.java --smoke
```

## Cold reconstruction qualification

On 2026-08-16, the generated RetroMCP `minecraft` workspace was moved aside
and the canonical command rebuilt it from the pinned toolchain and
hash-verified official client JAR. The full server, client, M3, M4, and
laboratory suite passed from that reconstruction. The rebuilt mapped client
entrypoint matched the pre-audit SHA-256:

```text
F3ABA176750D89E28559B9C85B070D1819ED310B83A6703DF002E768AD8EE14A
```

The previous generated workspace was sent to the Windows Recycle Bin and is
not part of the release or evidence boundary.
