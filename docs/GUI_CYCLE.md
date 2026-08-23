# Worldline Game UI Tree GO Audit

Status: **GO**

The Game UI Tree does not change the public product version. Worldline remains
v0.7.0 / M9. This audit promotes the inventory-tree evidence to a stable
milestone. It does not replace M10 native/offscreen render.

| Requirement | Authoritative evidence | Result |
| --- | --- | --- |
| Neutral tree | Subject uses only `UiMinecraftRuntime` and `GameUi` after factory creation | PASS |
| Official oracle | Independent process uses official obfuscated GUI symbols | PASS |
| Closed/open/closed | Empty tree, 46-node inventory tree, empty tree after Escape | PASS |
| Selector | `node(screen, inventory)` and `node(slot, 0)` match `slot(0)` | PASS |
| Click | Vanilla container click is executed on slot 0 | PASS |
| Four processes | Two subject and two oracle JVMs emit one frozen signature | PASS |

Frozen GUI-tree SHA-256:

```text
ab13a631ed766de32f2947fae1a6e0a86d9b6cde3cbc7e1557ff76f76ccc60cf
```

Canonical qualification command:

```text
java tools/harness/Gate.java --smoke
```

The contract and non-claims are in `docs/GUI_TREE.md`. Layout declaration
without pixels is in `docs/GUI_SPEC.md`.
