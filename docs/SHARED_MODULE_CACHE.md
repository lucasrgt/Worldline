# Shared-directory Module Cache

Worldline can hydrate signed, source-derived module bundles from an immutable
filesystem CAS shared by local worktrees, CI jobs, or a mounted network volume.
This backend is opt-in and never transports official or mapped runtime bytes.

Set these values before the canonical Gate:

```text
WORLDLINE_MODULE_CAS_MODE=shared-directory
WORLDLINE_MODULE_CAS_DIRECTORY=<absolute immutable CAS root>
WORLDLINE_MODULE_CAS_INDEX=<absolute canonical modules.wmi>
WORLDLINE_MODULE_CAS_INDEX_TRUST=<absolute signed trust envelope>
WORLDLINE_MODULE_CAS_PUBLIC_KEY=<canonical Ed25519 public key>
WORLDLINE_MODULE_CAS_CHANNEL=<reviewed release channel>
```

The normal `ModuleBuild` miss path verifies the public key, channel, signed
index envelope, index bytes, logical module/input/JDK identity, shared object
size and SHA-256, bundle schema, every class path and digest, and the extracted
immutable target. A missing logical entry remains an ordinary local compile.
Corrupt, linked, oversized, mismatched, or replaced objects fail closed.

`ModuleCacheSharedDirectory.publish` accepts only an already prepared
`ModuleCachePublication` plan. It revalidates the exact policy permit manifest,
streams each allowed object to its SHA-256 path with create-if-absent semantics,
and accepts a concurrent winner only when its bytes are identical. Filesystem
ACLs, mount encryption, retention, backup, and publisher-key governance remain
deployment responsibilities. This backend does not grant S3 or network upload
authority and never asserts a behavioral PASS.

After the canonical Gate, exercise the complete non-authoritative integration
with:

```text
java tools/harness/Gate.java --module-cache-shared-shadow
```

The shadow publishes the real `api` module bundle, enters through the same
`ModuleCacheRemote.hydrate` function used by `ModuleBuild`, re-extracts it into
a fresh immutable target, and re-packs the target to require the original
canonical bundle identity. All fixture keys and objects are removed afterward.
