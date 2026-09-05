# M793-AERO-PRODUCTION-QUALIFICATION Aero production qualification matrix

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Qualify exact multi-pose reuse and the default renderer against the immutable previous implementation across twelve production cases.

## Qualification cycle

Every client renders into a private RGBA8/depth24 framebuffer with the original depth precision; the default window back buffer is not the capture oracle. Empty alpha or constant frames fail. Eight fresh StationAPI clients run baseline-current-current-baseline version groups, each with off-on and on-off arms. The immutable baseline is 88adc8e6e219b2381766303dbaa4c26ee5c2199d. Cases cover first render, 1/20/64/128/256 exact phases, a 400-tick transition, custom playback, nested skeleton, model identity replacement, block and lighting changes, and chunk-boundary traversal with teleport reveal. Four terrain-only views wait for zero dirty in-frustum chunks and eight identical complete-frame samples spaced fifteen rendered frames apart. Model submission stays canceled. A separate diagnostic client attributes inclusive CPU time in pose resolution, cached local transforms, cached packing, unique transform-and-pack, and Tessellator driver calls; these injected hooks are absent from all eight qualification clients. Each case runs 180 frames per arm and records 120 after discarding 60, except first-render profiles retain all 180. Cold comparisons use only the first arm of fresh JVMs against the corresponding baseline version and order. Three complete RGBA captures per case and arm must match exactly, including current-off versus historical-off. Each version/order stratum requires mean render ratio at most 1.03, p99 at most 1.05, allocation at most 1.02 and non-increasing 50ms hitches. The 20- and 64-phase candidate cases additionally require at least ten percent mean render reduction in both orders. Saturation, transitions, custom playback and nested skeletons must not produce candidate matches. Changed lighting must rebuild real Cell Pages. Staging remains bounded at 16 MiB. All original Worldline Profiler artifacts and framebuffer bytes are retained. Timing excludes display, readback and fixture preparation; no all-platform or end-to-end latency claim is made.

Expected signal: `sdk=discovered,sessions=8-counterbalanced,cases=12,pixels=exact,default=guarded,poses=bounded,tail=guarded,allocation=guarded`.

Frozen semantic SHA-256: `ad89d03736e30f9f8a56bb95be7910f3a12fe6a2de97a908aae443839caedd40`.
