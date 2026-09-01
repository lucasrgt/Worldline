# B173-PORTAL-BLOCK-SUBSYSTEM-CONFORMANCE-CYCLE official Beta 1.7.3 portal block subsystem conformance

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

One public TestKit mini-subsystem maps portal block 90 from valid frame materialization through destruction. It proves both frame axes and the metadata-zero state domain, six-cell interiors, empty drops, chunk-NBT persistence, null collision, opacity and emission, unscheduled stable tick-callback behavior, and full collapse after frame loss.

## Qualification cycle

Run executes two fresh mapped worlds and two fresh untouched official-server worlds. Every replica builds canonical obsidian frames in both axes through the native portal materialization path, invokes the native tick callback while proving the block is not scheduler-enrolled, removes one supporting frame cell, breaks a portal cell through the native drop path, reloads six cells through native chunk NBT, and measures collision and light tables through PortalBlockSubsystemFixture.

Expected signal: `family=portal-block-subsystem,subject=90,claims=9,domain=90:0@X+Z,materialization=frame-6+6,drop=none,persistence=chunk-nbt-6,collision=none,light=11,ticks=scheduled-F+callback-stable,neighbors=frame-collapse,oracle=MATCH`.

Frozen semantic SHA-256: `cd3b0d0c468f293cee48a82a4461eca23103ca95ccdaca187a3264023e467fd3`.
