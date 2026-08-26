# M655-RAIN-STOP-EVENT rain stop event

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M655 qualifies the exact live official Beta 1.7.3 rain-stop transition. A protocol-14 client first observes the raining login bootstrap Packet70 reason 1, then the same connection is armed and observes Packet70 reason 2 when the bounded rain countdown expires. Equatable public evidence records raining-before and dry-after. This does not claim rain rendering, precipitation effects, thunder, commands, or a minimum rain duration.

## Qualification cycle

DataDrivenCycle rebuilds two fresh official dedicated-server replicas. Each replica creates a vanilla world, patches only the level.dat weather fields to raining with a bounded positive rain countdown and thunder disabled, then connects one headless protocol-14 client. Login Packet70 reason 1 establishes the initial raining state before arming. The live WorldServer weather update must then broadcast Packet70 reason 2, which RainStopFixture binds to raining-before and dry-after evidence. The patch preserves seed, time, and spawn identity. Both client and server disconnect cleanly. No GUI. No Aero.

Expected signal: `dimension=0,bootstrap=packet70-reason1,live=packet70-reason2,state=raining-before-dry-after,thundering=false,identity=seed-spawn-preserved,clients=1,disconnect=clean`.

Frozen semantic SHA-256: `85f90247a2a5cb89148caa85762e0582afa507d9ddba8bd92047938c28de8660`.
