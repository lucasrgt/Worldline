# M12 Completion Cycle

Status: **GO - real Aero scene capture and bounded spike reproduction**.

| Requirement | Evidence | Result |
| --- | --- | --- |
| Preserve modularity | Test-only Mixin and Gradle overlay; no Aero checkout edits or core dependency | PASS |
| Control the input | Fixed seed, 3 by 3 chunk load, position, view, velocity, and 240 ticks | PASS |
| Exercise dense Aero work | Measurement starts after at least 500 live BlockEntities | PASS |
| Repeat the observation | Two fresh same-input clients produce qualifying frames | PASS |
| Ingest real records | `AeroFrameLog` maps the actual runtime log into neutral counters | PASS |
| Classify the spike | Both captures identify expanded `chunks.compiled` logical work | PASS |
| Minimize evidence | Each record window is one-minimal at one qualifying frame | PASS |
| Preserve limits | Every product, harness, smoke, and adapter file stays below its enforced ceiling | PASS |
| Record the save boundary | Save is hashed; missing custom BEs after reload is a non-claim | PASS |

Frozen M12 evidence SHA-256:
`a1d766e049b7382ae81e13e9b4ef4de3531c870869d7fe442e053f507af9169e`.

The canonical gate is `java tools/harness/Verify.java --smoke`. Aero source,
Minecraft binaries, saves, frame logs, native libraries, and generated build
outputs remain ignored local artifacts.
