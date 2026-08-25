<!-- worldline-map-schema=1 -->
<!-- boundary=multiplayer-sleep-quorum -->
<!-- nonclaims=monster-interruption,nether-bed-explosion,bed-spawn-respawn,exact-skip-tick,more-than-two-players,dimension-edges -->
<!-- frozen-trace=79b73b846eab2044bd3b25100f02b1b1067fe22cb3d9ea94ba7aba944f217e93 -->

# M643 multiplayer sleep quorum behavior map

The reusable boundary is `worldline.testkit.SleepQuorumFixture#await`. It requires at least two declared sleepers, probes that a solo sleeper keeps the open quorum intact at every bounded tick probe of the hold window, and accepts completion only when the completing action wakes exactly the declared sleeper count. The evidence is equatable over sleeper count, hold window, probe interval, and woken count; wall-clock latency and exact skip ticks are excluded.

On the official Beta 1.7.3 dedicated server the fixture surface raises two deterministic ocean-floor dirt pillars in chunk zero-zero from scanned dirt-under-water foundations at least three blocks apart, then places one valid bed per pillar with an orientation avoiding the other pillar. The action surface sets console night 18000, lets the first player occupy its bed through an empty-hand activation confirmed by Packet17, holds four hundred ticks across fifty-tick probes, and lets the second player occupy the other bed. The observation surface retains Packet17 occupancy coordinates on each sleeper's own channel, the peer's broadcast copy of the first Packet17, occupied-versus-cleared head-half metadata per sleeper's own client, and persisted level.dat Time classified into the day range; it removes absolute clock values, entity identifiers, orientation metadata, and precise positions. Peer clients observe sleep through the Packet17 broadcast, not bed-block metadata.

The proof uses two fresh official dedicated-server replicas with monsters disabled and no decompiled source, inserted packets, or controlled-runtime replacement. Bounded nonclaims: monster interruption of sleep, bed respawn setting, Nether bed explosion, single-player instant skip, quorums beyond two players, and dimension edge cases are not claimed.

Frozen signal: `players=2,beds=2,night=18000,hold<=400ticks,open-quorum=no-skip,completed-quorum=morning,wake=both,morning=persisted-day-range,disconnect=clean`.

Frozen SHA-256: `79b73b846eab2044bd3b25100f02b1b1067fe22cb3d9ea94ba7aba944f217e93`.
