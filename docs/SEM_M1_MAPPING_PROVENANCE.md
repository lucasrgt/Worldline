# SEM-M1 Mapping Provenance

SEM-M1 establishes a reproducible, non-proprietary namespace bridge for the
complete Minecraft Beta 1.7.3 client/server symbol inventory. It does not
promote external names into Worldline's behaviorally qualified semantic
catalog.

## Pinned public inputs

| Input | Coordinate | Namespaces | SHA-256 | Bytes |
| --- | --- | --- | --- | ---: |
| Ornithe Calamus intermediary gen2 | `net.ornithemc:calamus-intermediary-gen2:b1.7.3:v2` | `intermediary`, `clientOfficial`, `serverOfficial` | `534b5afd5e53f1cfb9cdd40265eccd18915b959cbb4495271b6243553a9c3dd7` | 60,180 |
| Nostalgia | `me.alphamode:nostalgia:b1.7.3+build.60:v2` | `intermediary`, `named` | `6ea6496be70018e36e47af4af63b32c654af5d71806069b3c6084f97f1aa83c8` | 104,824 |

Nostalgia source provenance is pinned to revision
`7e60ac33973c21553b5e40e94ad2773453b9153d` and its CC0-1.0 license. Exact
URLs, artifact filenames, checksums, and source links live under
`mappings/b1.7.3/`.

Public mapping JARs are downloaded only to the ignored `local/mappings/`
directory:

```text
java tools/mappings/AcquireMappings.java mappings/b1.7.3/calamus-intermediary-gen2.properties mappings/b1.7.3/nostalgia.properties
```

The downloader fails closed on non-HTTPS URLs, unsafe output names, unexpected
byte lengths, checksum drift, redirects, and non-200 responses. Mapping JARs
are never committed.

## Reproducible initial differential

Run the canonical gate and compare exact intermediary identities:

```text
java tools/harness/Gate.java
java -cp .worldline/build/classes/symbolgraph worldline.symbolgraph.MappingAuditMain local/mappings/calamus-intermediary-gen2-b1.7.3-v2.jar local/mappings/nostalgia-b1.7.3-build.60-v2.jar
```

The pinned inputs produce:

| Kind | Intermediary inventory | Nostalgia entries | Missing from Nostalgia | Extra in Nostalgia |
| --- | ---: | ---: | ---: | ---: |
| Class | 719 | 719 | 0 | 0 |
| Field | 2,908 | 2,838 | 70 | 0 |
| Method | 2,297 | 2,782 | 77 | 562 |

The 562 extra method entries are not automatically errors. They include
constructors and JVM or unobfuscated members that the intermediary inventory
does not enumerate. Conversely, the 70 fields and 77 methods absent from the
named set are concrete coverage gaps. Later milestones must classify each
difference rather than manipulating denominators or inventing names.

## Completion boundary

SEM-M1 is complete when the pins, strict Tiny v2 reader, exact identity audit,
fixture tests, and canonical repository verification all pass. It establishes
provenance and measurable inventory coverage only. Official-bytecode
resolution, side classification, RetroMCP aliases, semantic qualification,
and generated coverage reports remain SEM-M2 and later work.
