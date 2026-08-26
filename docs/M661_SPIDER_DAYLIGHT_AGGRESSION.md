# M661-SPIDER-DAYLIGHT-AGGRESSION spider daylight aggression

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M661 freezes the official Beta 1.7.3 spider target-selection differential. One spider and one nearby player retain their identities and cells while open-sky daylight produces no target. After the only environmental change, time 6000 to 14000, the same spider selects exactly that player within the declared four-probe maximum. Leap, pursuit motion, damage, drops, and natural spawning are not claimed.

## Qualification cycle

Run compares two fresh mapped executions with two direct official-server oracle executions. Each in-memory world joins one native spider and one native player three blocks apart on unchanged open-sky stone geometry. Both paths calculate daylight at time 6000, exhaust the bounded daylight probes without a target, change only time to 14000, and require the same player from the same spider within the four-probe bound. Equatable public evidence normalizes process-local entity IDs into explicit identity and geometry preservation facts; it records the maximum rather than a successful probe number.

Expected signal: `oracle=MATCH,fixture=same-spider-player-geometry,daylight=target-absent,night=target-same-player,transition=6000-to-14000,attempt-cap=4`.

Frozen semantic SHA-256: `909666d2c0443153ce8e4afa39b620f35db7776336e979cd7bbce6130caa6cf8`.
