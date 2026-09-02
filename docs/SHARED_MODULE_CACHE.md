# Shared Module Cache

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
canonical bundle identity. It also requires an independently rebuilt inventory
to find exactly one signed index and one module bundle with no faults. All
fixture keys and objects are removed afterward.

## Read-only inventory

With the same environment configured, inspect the shared store without
hydrating or publishing anything:

```text
java tools/harness/Gate.java --shared-cas-inventory
```

The canonical JSON reports object digests, sizes, classifications, and states,
plus aggregate counts for verified module bundles, signed indexes, recognized
tracked smoke pins, valid orphans, corrupt objects, malformed entries, and
missing signed references. Paths and content are deliberately absent, and the
report has no verdict authority. Signed index and module references are
required; tracked smoke pins are recognized when present but are optional
because their authoritative copies already travel in the Git checkout.

`Gate --evidence-explorer-live` embeds this inventory when
`WORLDLINE_MODULE_CAS_MODE=shared-directory`. Disabled mode is represented as
`null`, not as an empty healthy store. Corruption, malformed layout, or a
missing signed object remains visible in the overlay and never becomes PASS
evidence.

## S3-compatible hydration

The same signed index and canonical bundle format can be hydrated from an
S3-compatible path-style endpoint. Select the backend and provide its closed
configuration together with the existing index trust values:

```text
WORLDLINE_MODULE_CAS_MODE=s3
WORLDLINE_MODULE_CAS_INDEX=<absolute canonical modules.wmi>
WORLDLINE_MODULE_CAS_INDEX_TRUST=<absolute signed trust envelope>
WORLDLINE_MODULE_CAS_PUBLIC_KEY=<canonical Ed25519 public key>
WORLDLINE_MODULE_CAS_CHANNEL=<reviewed release channel>
WORLDLINE_MODULE_S3_ENDPOINT=https://objects.example.invalid
WORLDLINE_MODULE_S3_BUCKET=worldline-cache
WORLDLINE_MODULE_S3_PREFIX=modules/v1
WORLDLINE_MODULE_S3_REGION=us-east-1
WORLDLINE_MODULE_S3_ACCESS_KEY=<scoped access key>
WORLDLINE_MODULE_S3_SECRET_KEY=<secret supplied outside the repository>
WORLDLINE_MODULE_S3_SESSION_TOKEN=<optional short-lived token>
```

Objects use the common
`objects/sha256/<first-two-hex>/<remaining-hex>` layout below the bucket and
optional prefix. Requests are signed with AWS Signature Version 4. Publication
uses `If-None-Match: *`; both a successful create and an existing-object reply
are followed by a complete authenticated download, exact metadata check,
bounded byte count, and local SHA-256 revalidation. Redirects, query-bearing
endpoints, unbounded responses, wrong credentials, missing objects, corrupt
objects, and non-HTTPS remote endpoints fail closed. Plain HTTP exists only
for the explicit loopback protocol fixture.

The local fixture anchors signing to AWS's published SigV4 example and proves
authenticated create, idempotent republish, hydration, miss, credential
rejection, and corruption rejection. It does not qualify a production object
store. Real TLS certificates, scoped credentials, retention, server-side
encryption, access logs, lifecycle rules, replication, publisher governance,
and operational recovery remain deployment responsibilities. Transport never
asserts behavioral PASS and never grants official or mapped runtime bytes
upload authority.
