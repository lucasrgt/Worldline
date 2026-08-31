# M782-AERO-CHUNK-SCHEDULER-LIFECYCLE-SOAK Aero chunk scheduler lifecycle soak

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

An independent external consumer builds the pinned StationAPI artifact and proves across two fresh Java 8 JVMs that one-rebuild scheduling drains 164096 work items, bounds hidden work to 157 frames under sustained visible arrivals, and clears pending state, queue identity, invocation, and metrics on every one of 256 world transitions.

## Qualification cycle

The cycle hashes the scheduler, platform budget, lifecycle Mixin, and external probe; runs StationAPI remapJar; compiles the exact core scheduler with javac release 8; then repeats a 256-epoch deterministic soak in two fresh JVMs. Each epoch admits 128 hidden chunks plus 512 visible arrivals, drains completely under budget one, seeds sixteen pending chunks, calls production reset, and inspects the scheduler state fail-closed. It does not launch Minecraft or claim GPU performance.

Expected signal: `consumer=aero-model-lib,revision=31dfe0c03b0ee454bd3996cec0bb76705f52835b,platform=stationapi-remapJar,compile=javac-release8,jvms=2-fresh,epochs=256,frames=163840,rebuilds=164096,hidden-wait=157<=160,transitions=256,pending-before-reset=16,reset=states0+queue-null+invocation0`.

Frozen semantic SHA-256: `4a0d5d1e57b9845e4b80fde1e9934b73b61e50d11e31b37629be5f5f06188e54`.
