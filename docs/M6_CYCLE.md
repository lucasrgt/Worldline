# Worldline v0.4.0 M6 Completion Audit

Status: **GO**

| Requirement | Authoritative evidence | Result |
| --- | --- | --- |
| Modular parsing | Trace and analysis modules compile without Minecraft, adapter, or reproduction dependencies | PASS |
| Strict viewer | Real 17-record `v2` trace renders schema, rows, count, and canonical signature | PASS |
| Oracle equality | Fresh mapped and official client traces compare equal | PASS |
| First divergence | One injected `tick9.slot` change reports record 9, field 11, and values 2 to 4 | PASS |
| Directionality | Reverse comparison retains the location and reverses ordered values | PASS |
| Invalid input | Duplicate schema field fails before viewing or comparison | PASS |
| Regression safety | All earlier release and laboratory signatures remain frozen | PASS |

Frozen divergence-report SHA-256:

```text
7eb4f707427c4e58ab3e481cc61f5801518325d5bbdfe045828325ab5ed2ea06
```

Canonical qualification command:

```text
java tools/harness/Verify.java --smoke
```

## Cold reconstruction qualification

On 2026-08-16, the generated RetroMCP `minecraft` workspace was moved aside
and the canonical command rebuilt it from the pinned toolchain and
hash-verified official client JAR. The full server, client, M3, M4, M5, M6,
and laboratory suite passed from that reconstruction. The rebuilt mapped
client entrypoint matched the pre-audit SHA-256:

```text
F3ABA176750D89E28559B9C85B070D1819ED310B83A6703DF002E768AD8EE14A
```

The previous generated workspace was sent to the Windows Recycle Bin and is
not part of the release or trace evidence boundary.
