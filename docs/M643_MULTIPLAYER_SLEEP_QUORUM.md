# M643-MULTIPLAYER-SLEEP-QUORUM multiplayer sleep quorum

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M643 freezes the Beta 1.7.3 multiplayer sleep quorum on the official dedicated server. With two Overworld players and two valid raised beds at night 18000, the first player alone enters a bed, receives its Packet17 occupancy, and remains asleep for a bounded 400-tick hold window during which morning does not occur. The second player then enters the second bed, completing the quorum; the official loop skips to morning and both sleepers wake, observed as each occupied bed head half clearing on its own sleeper's client plus a persisted level.dat time inside the day range. This claim excludes monster interruption of sleep, bed respawn setting, Nether bed explosions, single-player instant skip latency, more than two players, dimension edge cases, and any exact skip tick.

## Qualification cycle

DataDrivenCycle runs two fresh official dedicated-server replicas at seed 17320110707 with view distance three and monsters disabled. Each replica connects two protocol-14 wire clients, raises two deterministic ocean-floor dirt pillars at least three blocks apart in chunk zero-zero from scanned dirt-under-water foundations, and places one valid bed per pillar with an orientation that avoids the other pillar. The server clock is set to night 18000 by console. The first sleeper occupies its bed and SleepQuorumFixture probes every fifty ticks for four hundred ticks that the occupied head half never clears; the peer's broadcast copy of the first Packet17 is consumed as an explicit peer-visibility observation. The second sleeper then occupies the other bed; the fixture requires each occupied head half to clear on its own sleeper's client before persisting the world and classifying level.dat Time as morning. No decompiled source, inserted packets, or controlled-runtime replacements are used.

Expected signal: `players=2,beds=2,night=18000,hold<=400ticks,open-quorum=no-skip,completed-quorum=morning,wake=both,morning=persisted-day-range,disconnect=clean`.

Frozen semantic SHA-256: `79b73b846eab2044bd3b25100f02b1b1067fe22cb3d9ea94ba7aba944f217e93`.
