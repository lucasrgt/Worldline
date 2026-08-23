# M12 Completion Cycle

Status: **GO - real Aero scene capture and bounded spike reproduction**.

| Requirement | Evidence | Result |
| --- | --- | --- |
| Preserve modularity | Test-only Mixin and Gradle overlay; no Aero checkout edits or core dependency | PASS |
| Control the input | Fixed seed, 3 by 3 chunk load, position, view, velocity, and 240 ticks | PASS |
| Exercise dense Aero work | Measurement starts after at least 500 live BlockEntities | PASS |
| Repeat the observation | Two fresh same-input clients produce qualifying frames | PASS |
| Ingest real records | `AeroFrameLog` maps the actual runtime log into neutral counters | PASS |
| Localize the spike | Both captures spend at least 10 ms in chunk compilation | PASS |
| Minimize evidence | Each record window is one-minimal at one qualifying frame | PASS |
| Preserve limits | Every product, harness, smoke, and adapter file stays below its enforced ceiling | PASS |
| Record the save boundary | Save is hashed; M13 later isolates real versus phantom BEs | PASS |

Frozen M12 evidence SHA-256:
`804915ae89a1adef9f350adc020ed8a77986b2d3d4c1d84205009a4382ed051c`.

The canonical gate is `java tools/harness/Gate.java --smoke`. Aero source,
Minecraft binaries, saves, frame logs, native libraries, and generated build
outputs remain ignored local artifacts.
