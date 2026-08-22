# StationAPI and Mixin review

## Establish the environment

- Pin Minecraft b1.7.3, loader, StationAPI, mappings, Mixin, Java, and every runtime mod revision.
- Read `fabric.mod.json`, entrypoints, mixin JSON, Gradle source sets, run configurations, and produced mod metadata.
- Build a side graph from actual entrypoints and references. An `environment: *` manifest does not make referenced client classes server-safe.

## Verify Mixins

- Resolve the target against the exact mapped JAR used by the build.
- Inspect bytecode descriptors, inheritance, access, bridge methods, constructors, and client/server presence.
- Require exact targets for fragile injections. Treat optional targets as an explicit compatibility policy, not a convenient default.
- Prefer narrow injections and accessors over broad overwrites.
- Document what vanilla-observable invariant an invasive change preserves.
- Add a negative fixture for a missing or drifted target and a runtime invariant for behavior that compilation cannot establish.

## Verify StationAPI boundaries

- Register stable namespaced identifiers in the correct event and phase.
- Keep common block, block-entity, item, packet, and persistence classes free of client imports and transitive client dependencies.
- Put renderer, texture, model, input, GUI, and OpenGL registration behind client entrypoints.
- Do not assume block-entity registration provides network state synchronization. Inspect the exact update-packet or message path.
- Validate packet side, payload shape, coordinates, identity, duplicates, conflicting replay, chunk readiness, and disconnect lifecycle.
- Test a dedicated server without client-only libraries on its classpath when claiming server safety.

## Evidence hierarchy

Use mappings and decompiled code to navigate. Use bytecode for structural claims. Use the official JAR and controlled scenarios for behavior.
