<!-- worldline-map-schema=1 -->
<!-- boundary=aero-chunk-work-scheduler -->
<!-- nonclaims=no-default-promotion,no-off-thread-compilation -->
<!-- frozen-trace=79c7e74704af7c2e1145b58b5502c0bd6c1ff52071cb0ff8b9cece9270b87386 -->

# M773 Aero chunk-work scheduler behavior map

## Fixture

- A pinned AeroModelLib revision renders the restored solid 576-machine tower.
- Each arm selects at least eight real in-frustum and eight real hidden
  `ChunkBuilder` instances from the official client.
- Arms run in AB/BA/BA/AB order in fresh copied worlds.

## Actions and observations

1. One hidden builder is dirtied once while visible work is reintroduced.
2. Candidate frames may rebuild at most one builder.
3. The hidden target may not bypass visible work before debt eight and must be
   rebuilt shortly after becoming urgent.
4. Sixteen builders are repeatedly invalidated while the camera rotates.
5. Injection stops and the dirty backlog must drain completely.
6. Complete frame intervals and actual `ChunkBuilder.rebuild` durations feed
   the neutral hitch-rate and paired-maximum gates.

## Boundary

This milestone qualifies the opt-in scheduling candidate. It does not enable
the candidate by default, claim off-thread compilation, or promote a release.

Frozen trace: `v1|scene=restored-solid-576|pairs=4|priority=visible-before-debt|debt=8|budget=1-per-frame|stress=16-real-chunk-builders-camera-spin|drain=zero|hitch=50ms`.

Expected signal: `scene=solid-576,pairs=4,budget=1,visible-first,debt=8,spin-stress=16,drain=zero,hitch=no-regression,rebuild-max=majority-smaller`.
