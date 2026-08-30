# Worldline Semantic Catalog GO Audit

Status: **GO on the M67 line for catalog + adapter manifests**

The semantic catalog does not independently change the public product version.
The M9 trace `role=` aliases stay wired; this refresh adds oracled M48-M67
protocol-14 packet and GUI-window boundaries on top of the earlier packet,
tessellator, nibble, and GUI-slot promotions.

| Requirement | Authoritative evidence | Result |
| --- | --- | --- |
| Closed catalog | 26 categories and 290 required roles; unknown or duplicate symbols fail closed | PASS |
| Adapter manifests | `b173-client`, `b173-server`, and `stationapi` are drivers; `aero-model-lib` is the overlay extension pin with nine oracled `worldline/aero/` sites; extra in-tree adapters and `aero/modellib` types fail closed | PASS |
| Map coverage | Every named `symbols.map` symbol in the client, world, and M10 tessellator maps has a catalog role | PASS |
| Static graph | `SemanticGraph` fails closed on unknown read/write/dep tokens | PASS |
| Neutral CLI | `semantics show\|graph\|category\|role\|adapter` inspects without loading Minecraft | PASS |
| Aero types in `SemanticCatalog.standard()` | Forbidden | PASS |

Frozen catalog SHA-256:

```text
d07ee9d73b0a6b7e15f788b4393159129680b8a62dbc20d2e1090875dc5237de
```

Canonical qualification command:

```text
java tools/harness/Gate.java --runtime
```

The contract and non-claims are in `docs/SEMANTICS.md`. Domain inventories
from the M67 mapping audit are in `docs/SEMANTICS_AUDIT.md`. Driver versus
extension ownership is in `docs/EXTENSION_SDK.md`.
