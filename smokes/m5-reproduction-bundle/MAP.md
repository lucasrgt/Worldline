# M5 Reproduction Bundle Evidence Map

## Claim

Worldline v0.3.0 packages the oracle-qualified M4 snapshot into a deterministic,
path-portable reproduction bundle and restores it through a user-facing replay
CLI using separately verified local runtime inputs.

## Scenario

Two fresh JVMs independently package the frozen M4 snapshot and must write
byte-identical `.wlrb` artifacts. The runner copies one artifact into a nested
path containing spaces. Two new CLI processes replay the original and copied
paths; both must report the same bundle hash, snapshot hash, tick, and state.
A fifth fresh process runs the direct official client, whose tick-4 state must
match the CLI reports.

Five negative CLI runs must reject a corrupted embedded snapshot and canonical
bundles declaring the wrong runtime ID, Worldline version, official-client
hash, or RetroMCP revision.

Frozen full-bundle SHA-256:

```text
840dca117939412dbba24594a1091c44d4b312b1e9700cec7aab7f47e0cc0181
```

## Boundary

The bundle includes the M4 snapshot and declares external dependencies by
cryptographic identity. The repository launcher verifies those local inputs;
it never copies them into the bundle. The neutral CLI loads the b1.7.3 provider
at runtime and contains no mapped types.

## Non-claims

This fixture proves path portability across fresh local processes with the
same verified runtime. It is not a redistributable Minecraft package,
cross-version migration system, dependency downloader, mod pack, arbitrary
world archive, or remote execution protocol.
