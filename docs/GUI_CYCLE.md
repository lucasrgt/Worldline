# Game UI Tree Cycle

Status: experimental evidence. This is not a versioned product milestone and
does not replace M10 native/offscreen render.

## Promotion evidence

| Requirement | Evidence | State |
| --- | --- | --- |
| Neutral tree | Subject uses only `UiMinecraftRuntime` and `GameUi` after factory creation | PASS |
| Official oracle | Independent process uses official obfuscated GUI symbols | PASS |
| Closed/open/closed | Empty tree, 46-node inventory tree, empty tree after Escape | PASS |
| Selector | `node(screen, inventory)` and `node(slot, 0)` match `slot(0)` | PASS |
| Click | Vanilla container click is executed on slot 0 | PASS |
| Four processes | Two subject and two oracle JVMs emit one frozen signature | PASS |

The frozen GUI-tree SHA-256 is:

```text
ab13a631ed766de32f2947fae1a6e0a86d9b6cde3cbc7e1557ff76f76ccc60cf
```

Canonical command:

```text
java tools/harness/Verify.java --smoke
```
