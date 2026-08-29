# B173-REDSTONE-TORCH-SUBSYSTEM-CONFORMANCE-CYCLE official Beta 1.7.3 redstone torch subsystem conformance

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

One public TestKit mini-subsystem maps the idle and active redstone torch blocks as one inverter. It proves every support face, signal-only idle materialization, idle lifecycle and item drop, chunk-NBT persistence, collision and light envelopes, the native two-tick inversion, the eight-cycle burnout threshold and recovery window, and support invalidation.

## Qualification cycle

Run executes two fresh mapped worlds and two fresh untouched official-server worlds. Every replica inverts all five supported metadata faces, cycles one torch through the native burnout threshold, proves it remains idle through the hundred-tick history window and recovers after expiration plus the scheduled delay, breaks and reloads idle state, measures collision and light tables, and removes support through RedstoneTorchSubsystemFixture.

Expected signal: `family=redstone-torch-subsystem,subjects=2,claims=13,domains=75+76:1..5,materialization=item76+signal,drop=76,persistence=chunk-nbt,collision=none+none,light=0+7,ticks=2+burnout,neighbors=invert+support,oracle=MATCH`.

Frozen semantic SHA-256: `4e566a6a43d01eddd3b108b5b0bf777516a930c5ca4613b4f638922b9bc90ce1`.
