# SEM-M4 Namespace Audit

SEM-M4 turns namespace disagreement into deterministic work queues. Findings
may overlap: for example, a symbol can be absent from Nostalgia while its two
RetroMCP side aliases also disagree.

## Pinned classification

| Classification | Count | Meaning |
| --- | ---: | --- |
| `MATCH` | 422 | Inventory-present record with no detected namespace issue |
| `NAME_DIFFERENCE` | 5,367 | At least two available Nostalgia/RetroMCP aliases differ |
| `SIDE_CONFLICT` | 0 | Alias appears on a side excluded by official inventory identity |
| `WORLDLINE_MISSING` | 562 | External named entry has no intermediary inventory identity |
| `NOSTALGIA_MISSING` | 147 | Official inventory identity has no Nostalgia entry |
| `RETROMCP_MISSING` | 2 | Official inventory identity has no resolved RetroMCP alias |
| `AMBIGUOUS` | 0 | More than one candidate survived an exact identity join |

`WORLDLINE_MISSING` is an audit queue label, not a bytecode-absence claim. The
562 entries require later official-bytecode classification; constructors,
enum helpers, bridge methods, inherited JVM methods, and genuine inventory
gaps must not be conflated.

The large `NAME_DIFFERENCE` count is expected because Nostalgia intentionally
uses names close to modern Mojang terminology while RetroMCP preserves an MCP
lineage. Worldline records both. A name difference is evidence to review, not
permission to select the more familiar spelling.

## Fail-closed rules

- exact intermediary identity owns graph membership;
- exact official owner/name/remapped-descriptor identity owns RetroMCP joins;
- client and server aliases remain separate;
- empty namespaces remain empty;
- same-side duplicate official identities fail construction;
- unresolved and ambiguous findings cannot be counted as covered;
- no classification promotes a behavioral semantic role.

Descriptor conflict analysis against official class files and semantic-role
qualification remain later milestones because they require official artifact
inspection and the curated Worldline catalog respectively.
