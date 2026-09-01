# B173-SIGN-SUBSYSTEM-LIFECYCLE-CYCLE official Beta 1.7.3 sign subsystem lifecycle

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

One public TestKit mini-subsystem executes standing sign 63 and wall sign 68 on the unmodified official Beta 1.7.3 server. It proves all sixteen standing rotations, gameplay item consumption, direct break and sign-item drop, paired Packet130 text persistence, passable collision, native light transport, bounded tick stability, and causal support invalidation with final persisted air.

## Qualification cycle

DataDrivenCycle runs two fresh official-server replicas through SignSubsystemFixture. Each replica builds raised supports and measured movement lanes through gameplay, places and breaks every standing rotation, observes the first direct drop, places and writes a standing and wall pair, crosses a clean save and login, measures collision and light, holds both blocks for 240 ticks, breaks each support, and crosses a second clean save and login.

Expected signal: `family=sign-subsystem-lifecycle,claims=13,standing-domain=0..15,placed=63:4+68:5,inventory=20->19,break=63:0->0:0,drop=323x1:0,collision=UU,light=0:15x2,tick=240,reload=FRESH_LOGINx2,support=63:4+68:5->0:0+0:0,evidence=9e2f1dfce178bfdb3bb2e8bd2d38f0e0448288cedfa2c04601ce6455f8e817e8`.

Frozen semantic SHA-256: `1a6ccdee8901506c433e50ef0b630fadb7f65ba0e3704686cbd19a85cb0e27a9`.
