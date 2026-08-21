# Worldline Semantic Catalog GO Audit

Status: **GO on the M67 line for catalog + adapter manifests**

The semantic catalog does not independently change the public product version.
The M9 trace `role=` aliases stay wired; this refresh adds oracled M48-M67
protocol-14 packet and GUI-window boundaries on top of the earlier packet,
tessellator, nibble, and GUI-slot promotions.

| Requirement | Authoritative evidence | Result |
| --- | --- | --- |
| Closed catalog | 24 categories and 238 required roles; unknown or duplicate symbols fail closed | PASS |
| Adapter manifests | `b173-client` and `b173-server` are drivers; `aero-model-lib` is the overlay extension pin with nine oracled `worldline/aero/` sites; extra in-tree adapters and `aero/modellib` types fail closed | PASS |
| Map coverage | Every named `symbols.map` symbol in the client, world, and M10 tessellator maps has a catalog role | PASS |
| Static graph | `SemanticGraph` fails closed on unknown read/write/dep tokens | PASS |
| Neutral CLI | `semantics show\|graph\|category\|role\|adapter` inspects without loading Minecraft | PASS |
| Aero types in `SemanticCatalog.standard()` | Forbidden | PASS |

Frozen catalog SHA-256:

```text
b50402df6214dd80292640092d986e943312627437cafd5dbde8085f474bdad5
```

Canonical qualification command:

```text
java tools/harness/Verify.java --runtime
```

The contract and non-claims are in `docs/SEMANTICS.md`. Domain inventories
from the M67 mapping audit are in `docs/SEMANTICS_AUDIT.md`. Driver versus
extension ownership is in `docs/EXTENSION_SDK.md`.
