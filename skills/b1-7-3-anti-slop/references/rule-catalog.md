# Rule catalog

## Implemented

### B173-SIDE-001: no client references in common or server code

Common and dedicated-server Java sources must not reference client Minecraft, client StationAPI, LWJGL, or configured client-only package prefixes. Run `audit_side_safety.py` with explicit source roots. The rule checks imports and fully qualified references and fails closed on missing roots.

Correction: move rendering, GUI, input, texture, and client networking code behind a client entrypoint. Keep common block entities and messages free of client class closure.

## Review-only candidates

These IDs are not enforced by the initial scanner. Do not describe them as automated gates.

- `B173-MIXIN-001`: every injection target and descriptor exists in the pinned target bytecode.
- `B173-MIXIN-002`: invasive injection and overwrite sites carry a specific safety invariant and runtime regression.
- `B173-MAP-001`: intermediary or obfuscated names have pinned mapping provenance and no incompatible-name contamination.
- `B173-NBT-001`: persistent state has explicit schema, superclass calls, bounded decoding, and restart evidence.
- `B173-NET-001`: inbound payloads validate length, identity, side, replay policy, and lifecycle before mutation.
- `B173-HOT-001`: tick and render hot paths perform no unbounded traversal, logging, formatting, file I/O, or avoidable allocation.
- `B173-STORAGE-001`: indexed storage updates only changed entries; global rebuilds are explicit, bounded, and observable.
- `B173-LICENSE-001`: build and release artifacts exclude official JARs, original assets, and decompiled sources.

Promote a candidate only through the process in `evidence-promotion.md`.
