# SEM-M2 Complete Symbol Graph

SEM-M2 joins the pinned intermediary inventory, client and server official
aliases, and Nostalgia names without promoting any external alias into the
Worldline semantic catalog.

## Identity and schema

The graph identity is always:

```text
kind + intermediary owner + intermediary name + intermediary descriptor
```

Every deterministic TSV row contains:

- kind, owner, intermediary name, and descriptor;
- client-official and server-official aliases;
- the Nostalgia alias;
- `client`, `server`, `shared`, or `unresolved` side classification;
- explicit inventory-presence and Nostalgia-presence flags.

An external-only entry remains `unresolved`; it is not silently assigned to a
side or counted as an official identity. Empty aliases remain empty.

## Pinned result

The SEM-M1 artifacts produce 6,486 union records:

| Side | Records |
| --- | ---: |
| Client | 2,377 |
| Server | 462 |
| Shared | 3,085 |
| Unresolved | 562 |

The canonical UTF-8 TSV SHA-256 is
`ff47e7013e3fb89adead20ad54cf60af744b56dacaa6ffe98e42d413a94a5ebd`.
The 562 unresolved records exactly match the Nostalgia-only method set found
in SEM-M1.

Generate the ignored local report after the canonical gate:

```text
java -cp .worldline/build/classes/symbolgraph worldline.symbolgraph.SymbolGraphMain local/mappings/calamus-intermediary-gen2-b1.7.3-v2.jar local/mappings/nostalgia-b1.7.3-build.60-v2.jar local/mappings/b1.7.3-symbol-graph.tsv
```

The report is generated under ignored `local/`; no mapping JAR or generated
full-game dump is committed. Fixture tests cover namespace parsing, exact
member identity, union behavior, side classification, unresolved external
entries, and deterministic hashing.

## Non-claims

SEM-M2 does not prove what any symbol does, resolve the 562 external-only
methods against bytecode, import RetroMCP aliases, or mark all named symbols as
semantically qualified. Those remain separate, measurable later milestones.
