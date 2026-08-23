# Worldline Game UI Tree GO Audit

Status: **GO for inventory; workbench extension pending official qualification**

The Game UI Tree does not change the public product version. Worldline remains
v0.7.0 / M9. This audit promotes the inventory-tree evidence to a stable
milestone. It does not replace M10 native/offscreen render.

| Requirement | Authoritative evidence | Result |
| --- | --- | --- |
| Neutral tree | Subject observes both screens through `GameUi` after driver-owned opening | PASS |
| Official oracle | Independent process uses official obfuscated GUI symbols | PASS |
| Closed/open/closed | Empty, 46-node inventory, empty, 47-node workbench, empty | PENDING |
| Authored workbench | `GameUiSpec.workbench().matchesStructure(runtime.ui().nodes())` | PENDING |
| Selector | `node(screen, inventory)` and `node(slot, 0)` match `slot(0)` | PASS |
| Click | Vanilla container click is executed on slot 0 | PASS |
| Four processes | Two subject and two oracle JVMs emit one frozen signature | PASS |

Frozen GUI-tree SHA-256:

```text
87256440f1db54387671e3ad0c47a464afbe991d1933d339f37afce460e11b00
```

Canonical qualification command:

```text
java tools/harness/Gate.java --milestone gui-tree
```

The contract and non-claims are in `docs/GUI_TREE.md`. Layout declaration
without pixels is in `docs/GUI_SPEC.md`.
