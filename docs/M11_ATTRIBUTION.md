# M11 Render-Work Attribution and Aero Qualification

## Stable bounded contract

`FrameAttribution` compares two frames using elapsed frame time, explicit host
pause time, and immutable named non-negative work counters. A frame must be at
least 5 ms and 1.5 times slower before it is a spike candidate. A named counter
must grow by at least four units and two times before work expansion is claimed.

The result is one of:

- `LOGICAL_WORK`: the frame is slower and named work expanded;
- `RUNTIME_STALL`: the frame is slower while named work stayed stable;
- `MIXED`: work expanded and a host pause of at least 5 ms was observed;
- `INCONCLUSIVE`: the frame did not cross the bounded spike threshold.

The result includes the top expanded counter, before/after values, frame ratio
in integer tenths, and host-pause microseconds. Counter names are adapter-owned;
the neutral analyzer has no Aero, Minecraft, LWJGL, or StationAPI dependency.

## Native work boundary

The existing M10 scenario now counts work before submitting it to Minecraft's
`Tessellator`: one draw, one color change, four vertices, and zero texture
binds. The mapped and official renderers still produce the frozen M10 RGBA hash.
This establishes attribution above, rather than inside, the qualified Pbuffer.

## Exact Aero candidate

The ignored local checkout must be clean and match:

- repository: `https://github.com/lucasrgt/aero-model-lib.git`;
- commit: `436d65b38c53346b465e5e793bd943177ebfaa32`;
- library version: `3.0.0`;
- Minecraft: Beta 1.7.3;
- StationAPI: `2.0.0-alpha.5.4` in the test consumer.

The M11 cycle runs all 222 pure-Java core tests with the budget configuration
the tests expect, builds the StationAPI JAR, compiles the composite test mod,
validates the mod descriptor and diagnostic classes, and loads the artifact in
an isolated process. It also boots the real Fabric Loader/StationAPI client,
observes Aero and both test-mod entrypoints, and exits through the eight-second
benchmark hook.

The upstream `runClient` task contains a multiline Groovy `println` whose
leading unary `+` fails before JavaExec. M11 removes only that one task action
through a checked init script. The Aero checkout remains clean and exact.

## Observed startup diagnostic

The successful runtime boot catches and logs one showcase-block UV lookup
before the StationAPI atlas is ready. It is recorded as
`chunk-bake-uv-atlas-unready`; it did not prevent loader or entrypoint success.

## Non-claims

M11 does not reproduce the historical random lag spike, create or enter the
dense saved-world scene, claim frame-time determinism across hosts, or identify
a production root cause. It proves that the exact stack loads and that future
captured frame records can answer the first bounded question: expanded logical
work or stable-work runtime stall.
