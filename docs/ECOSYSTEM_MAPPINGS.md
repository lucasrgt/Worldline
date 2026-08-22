# Loader, API, and mapping provenance

Worldline models loaders, modding APIs, mapping sets, and namespaces as
different concepts. A project using Fabric Loader does not thereby use a
particular API or named mapping set.

| Record | What Atlas can support | Status |
| --- | --- | --- |
| Fabric Loader | Babric documentation and Ornithe both describe Fabric-style loader support | `STRONG` |
| StationAPI | Its primary repository describes a general legacy Minecraft API and identifies the Beta 1.7.3 Babric target | `STRONG` |
| RetroAPI | Its primary repository describes cross-version registration features and optional StationAPI integration | `STRONG` |
| BINY | The primary repository documents the Beta 1.7.3 mappings, Ornithe port, Maven coordinate, and CC0 license | `STRONG` |
| Nostalgia | Public artifacts and SourceGen support establish availability for client and server; license and coverage remain unresolved | `EXPERIMENTAL` |

Primary sources:

- [Babric development guide](https://babric.github.io/develop/)
- [Ornithe](https://ornithemc.net/) and its [development guide](https://ornithemc.net/develop/)
- [StationAPI](https://github.com/ModificationStation/StationAPI)
- [RetroAPI](https://github.com/matthewperiut/RetroAPI)
- [BINY mappings](https://github.com/calmilamsy/biny-mappings)
- [Nostalgia artifacts](https://mvn.devos.one/releases/me/alphamode/nostalgia/)
- [SourceGen](https://github.com/Lenni0451/SourceGen)

## Community observations

The Atlas also preserves the 2026-08-21 Calmilamsy report that Babric is the
current mainstream Beta 1.7.3 stack, StationAPI may eventually migrate to
Ornithe, recent StationAPI and Ornithe versions reportedly clash, BINY is
fairly complete, and Nostalgia is the most complete mapping set. These are
valuable routing signals, not primary-source proofs. They therefore remain
`OBSERVATIONAL` or `UNKNOWN` and cannot fill an oracle or reproducibility cell.

## Mapping policy

Worldline does not copy third-party mappings into its semantic catalog. It
records the mapping set, namespace, source, coordinate, license when known,
and a content hash when a future build pins an artifact. Coverage or
compatibility claims require a reproducible scan against the hash-pinned
official client or server JAR. Decompiled names never override behavior
observed from the official JAR.
