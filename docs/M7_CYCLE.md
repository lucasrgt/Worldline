# M7 General Mod Loading GO Audit

Status: GO.

## Promotion evidence

| Requirement | Evidence | State |
| --- | --- | --- |
| Game-independent package contract | `worldline-mods` compiles with no product dependencies | PASS |
| Canonical metadata | Unit and CLI tests accept the six-field descriptor and reject non-canonical input | PASS |
| Explicit compatibility | Runtime, API, and combined mismatch results are unit tested | PASS |
| Descriptor-selected loading | Two differently named entrypoints are loaded from independently described JARs | PASS |
| Provenance and typing | Whole-JAR SHA-256, entrypoint code source, and `B173Mod` subtype are checked | PASS |
| Fail-closed execution | Runtime/API mismatches cannot trigger an initialization-trap entrypoint; wrong type is rejected | PASS |
| Real controlled client | Glass (`20`) and gold (`41`) effects execute through the mapped b1.7.3 tick | PASS |
| Vanilla baseline | The required client cycle matches two direct official-JAR processes before M7 runs | PASS |
| Public boundary | Generated JARs/classes stay under ignored `.worldline`; no game or mod binary is released | PASS |

The frozen semantic compatibility report SHA-256 is:

```text
bd13989879dba605a0cf790312c24a0f6947e87fb0b4d3ecd6f8cb265cbfb537
```

The report covers both entrypoint identities and state signatures, compatible
inspection, runtime/API incompatibilities, entrypoint type failure, and
malformed/missing descriptor rejection. JAR hashes are reported as provenance
but excluded from this semantic signature because ordinary JAR timestamps are
not a reproducible-build contract.

## Qualification

The canonical `java tools/harness/Verify.java --smoke` gate passed first from
the prepared workspace and then after the complete generated `minecraft/`
workspace was moved aside. The cold run invoked RetroMCP decompile/recompile,
rebuilt the client adapter and all evidence, and passed M1 through M7 plus the
laboratory cycle. Rebuilt mapped `Minecraft.class` retained SHA-256
`f3aba176750d89e28559b9c85b070d1819ed310b83a6703df002e768ad8ee14a`.
The redundant pre-reconstruction directory was sent to the Windows Recycle Bin.
