# B173-BED-SUBSYSTEM-CONFORMANCE-CYCLE official Beta 1.7.3 bed subsystem conformance

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

One public TestKit mini-subsystem maps bed block 26 as a directional two-cell structure. It proves the native foot, head, and occupied-head metadata domains; finite removal of either half; the foot-only bed-item drop; nine-sixteenths collision with nonopaque unlit transport; unscheduled stable tick callbacks; and causal paired stability versus orphan cleanup. Existing placement, save-reload, and native-render evidence remain independently owned and are not count-farmed here.

## Qualification cycle

Run executes two fresh mapped worlds and two fresh untouched official-server worlds. Every replica constructs all four directional foot and head pairs, toggles the native occupied-head bit, harvests isolated foot and head halves, identifies the asymmetric bed-item drop, measures collision bounds and light tables, invokes inherited tick callbacks on both halves, and contrasts stable complete pairs with the native drop-producing foot cleanup and silent head cleanup through BedSubsystemFixture.

Expected signal: `family=bed-subsystem,subject=26,claims=7,domain=foot0..3+head8..15,break=both-removed,drop=foot355x1+head-none,collision=full-x-9/16,light=0:0,ticks=scheduled-F+callback-stable,neighbors=paired-stable+orphans-cleaned,oracle=MATCH`.

Frozen semantic SHA-256: `9fafcdf57291cb7dd2660a1468035f2d9f40f9b73f599889db777f4600b4a740`.
