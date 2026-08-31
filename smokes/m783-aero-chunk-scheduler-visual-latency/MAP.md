<!-- worldline-map-schema=1 -->
<!-- boundary=external-aero-runtime-qualification -->
<!-- nonclaims=no-off-thread-compilation,no-unbounded-world-generalization,no-promotion-without-all-gates -->
<!-- frozen-trace=666536f4e861d8383318106fd1ab6f48698e38cdf79c249ae4298e3b5edb54ab -->

# M783 Aero chunk scheduler visual-latency behavior map

## Fixture

- AeroModelLib revision `82119d67d7ae88e527f1397cd6a0def31f1697ef`
  renders a copied 576-machine world in every treatment.
- Four counterbalanced pairs run `pages` and `prebake` in eight fresh real
  clients. Both arms keep page caching enabled; only `prebake` activates the
  camera-aware scheduler with budget one, radius three, age 120, and debt 30.
- Each 2,400-frame route repeats walking, turning, teleporting, mutation, and
  settling four times and reloads the same world in-process at frame 1,200.

## Actions and observations

1. Every eight frames, the fixture dirties two visible chunks, one look-ahead
   chunk, and one background chunk.
2. A direct census records the frame at which each visible chunk is dirtied
   and the frame in which its real `ChunkBuilder.rebuild()` completes.
3. Each arm also records full frame intervals, current-thread allocation,
   actual rebuild count and duration, scheduler accounting, maximum and final
   backlog, world-reset count, and reload count.
4. Every artifact requires 576 machines, one reload, at least three world
   resets, real dirty backlog, real rebuilds, latency samples, zero pending
   visible chunks, and zero final backlog.
5. The neutral paired hitch-rate gate uses a 50 ms threshold and a 5,000 ppm
   no-regression margin. Aggregate promotion additionally requires at least
   97% baseline FPS, no more than 105% baseline p99 or allocation, maximum
   visible latency eight frames, and visible p99 four frames.
6. A valid cycle always classifies the candidate as `promote` or
   `keep-disabled`; a performance loss is evidence, not a broken test.

## Boundary

This milestone qualifies one pinned real-client workload and its world
transition. It does not claim off-thread GL compilation, universal results for
unbounded worlds, or permission to promote when any safety, throughput, memory,
or visible-latency gate fails.

Frozen trace: `v1|scene=restored-576|pairs=4|orders=pages-prebake+prebake-pages+prebake-pages+pages-prebake|window=2400|reload=1200|route=4x-walk+turn+teleport+mutation+settle|dirty=visible2+lookahead1+background1-per-eight|pages=on|prebake=off-vs-budget1-camera3-age120-debt30|capture=wall+allocation+chunk+visible-latency+backlog+world-resets|gates=hitch5000ppm+fps3pct+p995pct+alloc5pct+visible-max8+p99-4|decision=promote-or-keep-disabled`.

Expected signal: `scene=restored-576,pairs=4,jvms=8-fresh,window=2400,reload=midpoint,route=walk+turn+teleport+mutation+settle,pages=on,prebake=off-vs-budget1,visual-latency=measured,backlog=drained,world-reset=observed,hitch=classified,metrics=classified,decision=promote-or-keep-disabled`.
