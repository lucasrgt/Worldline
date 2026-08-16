# M7 Mod Loading Evidence Map

## Claim

Worldline can inspect a locally supplied, descriptor-bearing mod JAR without
Minecraft, reject incompatible metadata before execution, and load a compatible
descriptor-selected `B173Mod` entrypoint from that exact JAR into the controlled
b1.7.3 client.

## Executable evidence

The M7 runner starts only after the controlled-client cycle has rebuilt the
adapter and matched mapped execution against two direct official-JAR processes.
It then builds local ignored packages and starts eleven fresh JVMs:

| Boundary | Required result |
| --- | --- |
| Primary mod, twice | `worldline.glass-probe` deterministically changes block 65 from air (`0`) to glass (`20`) |
| Secondary mod | `worldline.gold-probe`, selected by a different descriptor entrypoint, changes it to gold (`41`) |
| Compatible CLI inspection | Exit 0 and `COMPATIBLE` with descriptor metadata and a 64-digit artifact SHA-256 |
| Wrong runtime/API inspections | Exit 3 with exact mismatch results |
| Malformed/missing descriptors | Exit 1 before entrypoint discovery |
| Wrong runtime/API loads | Loader refusal before an initialization-trap entrypoint can run |
| Wrong entrypoint type | A metadata-compatible package fails the adapter subtype check |

Both real mod executions verify that the entrypoint code source equals the
inspected JAR. The two primary processes must emit identical evidence. The
semantic report is frozen as:

```text
bd13989879dba605a0cf790312c24a0f6947e87fb0b4d3ecd6f8cb265cbfb537
```

Generated JARs, classes, and evidence live only under ignored `.worldline/`.

## Boundary

The b1.7.3 callback is explicitly controlled modification, so its intended
glass/gold delta is not compared to vanilla. Vanilla-observable behavior with
no mod enabled remains owned by the immediately preceding four-process client
oracle. See `docs/M7_MODS.md` for package grammar, lifecycle, and non-claims.
