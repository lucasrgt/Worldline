<!-- worldline-map-schema=1 -->
<!-- boundary=state-world-differential -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=17747e296bd5c3d985ae22b817598da213acc55c7d078016447b2f0869ed1d28 -->

# M503-SW behavior map

## Boundary

This candidate compares the same seeded `EntityPig` passive-AI path directly
across mapped b1.7.3 classes and the hash-verified official server JAR. It uses
an in-memory 5-by-5 stone world, calls `World.tick()` followed by
`World.updateEntities()`, and fixes both `World.rand` and the pig's inherited
`Entity.rand` to seed `50320240820`.

The positive fixture starts one pig at `(8.5, 65.0, 8.5)` on an open stone
surface. The negative fixture starts the same pig in the center air cell of a
two-block-high 3-by-3 stone ring. Both fixtures run 240 composed ticks and
sample exact world time, entity count, milliblock position, entity age, and
dead state every five ticks.

## Mapping anchors

- `World` maps to client `fd` and server `dj`; `rand` is `r`, `tick()` is
  client `l` / server `h`, and `updateEntities()` is client `g` / server `e`.
- `Entity` maps to client `sn` and server `lq`; `posX/Y/Z` are client
  `aM/aN/aO` and server `aP/aQ/aR`; `rand` is client `bs` / server `bv`;
  `ticksExisted` is client `bt` / server `bw`; `isDead` is client `be` /
  server `bh`; and `setPosition` is client `e` / server `c`.
- `EntityPig` maps to client `wh` and server `oc`.

## Oracle independence

Mapped sources compile against the controlled mapped runtime. The official
oracle uses only obfuscated names compiled directly against the hash-verified
server JAR. The paths share only `CanonicalTrace` serialization and the
literal deterministic fixture.

## Pass condition

Two fresh mapped runs and two fresh official-oracle runs must be deterministic
inside each pair and byte-identical across the mapping boundary. Each pig must
remain alive, alone, and vertically bounded through age 240. The open pig must
move at least 0.5 and at most 12 blocks horizontally from its seed position;
the caged pig must remain within 0.25 blocks. These bounds establish only
seeded passive movement versus collision confinement.

This milestone does not claim breeding, panic, drops, natural spawning,
player interaction, packets, persistence, or general behavior for other RNG
seeds. Its frozen trace signature is
`17747e296bd5c3d985ae22b817598da213acc55c7d078016447b2f0869ed1d28`.

## Frozen semantic signal

`oracle=MATCH,fixture=m503-sw-pig-wander,ticks=240,controlled=true`
