<!-- worldline-map-schema=1 -->
<!-- boundary=spider-daylight-aggression -->
<!-- nonclaims=leap,pursuit-motion,damage,drops,natural-spawning,successful-probe-number -->
<!-- frozen-trace=909666d2c0443153ce8e4afa39b620f35db7776336e979cd7bbce6130caa6cf8 -->

# M661 spider daylight aggression behavior map

## Boundary

The official fixture joins one spider at `11:65:8` and one player at
`8:65:8` on unchanged open-sky stone geometry. It records both native
identities and cells. At time `6000`, calculated entity brightness must be
above the spider's daylight threshold and every call within the four-probe
maximum must return no target. The fixture then changes only world time to
`14000`, recalculates light, and requires the same spider to return exactly
the same player within the same fixed maximum.

`SpiderDaylightAggressionEvidence` rejects a daylight target, a different
night target, identity drift, geometry drift, or an altered maximum. It does
not retain the successful night probe number.

## Distinction and nonclaims

M457 proves a spider leap and touch damage at night. M455 proves zombie and
skeleton pursuit vectors. M661 proves only the spider's light-sensitive
target-selection differential. It does not claim movement, contact damage,
drops, natural spawning, or a general hostile-mob rule.

Frozen trace:

```text
v1|seed=66120260826|daylight:time=6000,entities=2,column=1.1.1.1.1.4|night:time=14000,entities=2,column=1.1.1.1.1.4
```

Frozen semantic signal:

```text
oracle=MATCH,fixture=same-spider-player-geometry,daylight=target-absent,night=target-same-player,transition=6000-to-14000,attempt-cap=4
```

Frozen semantic SHA-256:
`909666d2c0443153ce8e4afa39b620f35db7776336e979cd7bbce6130caa6cf8`.
