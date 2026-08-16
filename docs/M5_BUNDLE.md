# M5 Portable Reproduction Bundle

Worldline v0.3.0 packages a durable M4 snapshot with the exact runtime identity
needed to replay it. The bundle is original Worldline metadata and snapshot
history; it never contains the official Minecraft JAR, assets, mapped classes,
decompiled source, or RetroMCP binaries.

## Canonical format

The `.wlrb` artifact is strict UTF-8, uses LF line endings, and is limited to
2 MiB:

```text
WORLDLINE-REPRODUCTION/1
runtime=<lowercase runtime token>
worldline=<semantic version>
client.sha256=<64 lowercase hexadecimal characters>
toolchain.revision=<40 lowercase hexadecimal characters>
snapshot.sha256=<embedded snapshot SHA-256>
snapshot=<unpadded base64url M4 snapshot bytes>
sha256=<lowercase SHA-256 of every preceding byte, including the prior LF>
```

Parsing validates strict UTF-8, exact framing and field order, identifiers,
hash spelling, embedded snapshot integrity, body checksum, size limits, and an
exact decode/re-encode comparison. Unknown formats fail closed.

## Replay command

Prepare the local controlled runtime once with:

```text
java tools/harness/Verify.java --smoke
```

Then replay a bundle from any local path:

```text
java tools/replay/Replay.java replay path/to/reproduction.wlrb
```

The launcher verifies the local official artifact and pinned toolchain, builds
the runtime classpath only from ignored local/generated inputs, and invokes the
neutral CLI. The b1.7.3 provider rejects mismatched runtime ID, Worldline
version, client SHA-256, or RetroMCP revision before restoring the M4 snapshot.
Successful output includes stable `WORLDLINE_REPLAY=PASS`, bundle and snapshot
hashes, runtime ID, tick, and canonical state fields.

## Portability and non-claims

The bundle is path-portable and contains everything Worldline itself generated
to describe the replay. A receiving workspace must separately possess the
matching legitimate game artifact and pinned open-source toolchain. This is a
deliberate legal and provenance boundary, not a fully standalone executable.

M5 does not package arbitrary saved worlds, mods, assets, JVMs, native
libraries, rendering state, or network sessions. It does not migrate snapshots
between runtime versions, download missing dependencies, or yet display trace
differences. Trace exploration is M6; broader dependency packaging requires a
future manifest extension and independent evidence.
