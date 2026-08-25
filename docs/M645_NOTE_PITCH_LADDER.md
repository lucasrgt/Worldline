# M645-NOTE-PITCH-LADDER note block pitch ladder

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M645 freezes the official note-block tuning ladder on a raised stone support. Each empty-hand activation click increments the tile pitch modulo twenty-five and then plays the new pitch through Packet54, so twenty-five clicks traverse pitches one through twenty-four and wrap back to zero, and a twenty-sixth click replays pitch one. The tile pitch after that full cycle is one, and one further activation across a save-stop-restart reload replays pitch two, which distinguishes a persisted tile from a fresh pitch-zero note and proves TileEntityNote pitch persistence. This milestone does not claim audio playback, client-side rendering, any dig-start tuning property, other supporting blocks or instruments, or any seed other than 17320110707.

## Qualification cycle

DataDrivenCycle runs two fresh replicas of the same official cycle. Each replica seeds a player with stone and one note block at 4.5:60:4.5, finds the deterministic dirt foundation in chunk 0,0, raises a stone column above any water, places note block 25 on top, performs twenty-six empty-hand Packet15 activations while awaiting each Packet54 event, verifies the cell stays 25:0, saves, stops the server, restarts a fresh server JVM on the same world, logs in again, and activates once more for the retained pitch. NotePitchFixture validates the complete ladder, the wrap to zero, and the retained pitch two, and publishes equatable evidence without coordinates. Both replicas must emit identical signal, trace, and signature.

Expected signal: `seed=17320110707,clicks=26,ladder=pitches1-24-wrap0,instrument=1,retained=2,persisted=true,replicas=2,disconnect=clean`.

Frozen semantic SHA-256: `f6dcb484fa9a8f017e4a462f0eaea706a5542a601d4536ea481da72f7e2e862d`.
