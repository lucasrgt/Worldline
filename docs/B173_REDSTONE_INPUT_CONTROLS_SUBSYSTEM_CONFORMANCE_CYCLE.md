# B173-REDSTONE-INPUT-CONTROLS-SUBSYSTEM-CONFORMANCE-CYCLE official Beta 1.7.3 redstone input-control subsystem conformance

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

One public TestKit mini-subsystem maps lever 69, stone pressure plate 70, wooden pressure plate 72, and stone button 77 across their reachable active-state domains, state-dependent selection bounds, empty collision boxes, zero light opacity and emission, native latch or twenty-tick policies, entity selectivity, and support-loss removal with one item drop. Existing lifecycle, persistence, native rendering, and downstream wire propagation evidence remain independently owned and are not counted here.

## Qualification cycle

Run executes two fresh mapped worlds and two untouched official-server worlds. Every replica toggles a floor lever, pulses a wall button, contrasts player and dropped-item pressure-plate activation, releases timed controls through native update ticks, measures native block bounds and light tables, and removes each required support through RedstoneInputControlsSubsystemFixture.

Expected signal: `family=redstone-input-controls,subjects=69+70+72+77,claims=20,states=lever-toggle+button-pulse+plate-selectivity,bounds=stateful,collision=none,light=0/0,ticks=latch+20,neighbors=support-drop,oracle=MATCH`.

Frozen semantic SHA-256: `273182979913a3ae4281a7301f39e63fe7dc822c0444df8be30fa245e34afe7b`.
