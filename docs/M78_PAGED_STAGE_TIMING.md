# M78 paged stage timing

Status: GO in Worldline v1.66.0.

M78 qualifies Aero's real steady-state cell-page path over the exact M74
sixteen-entity scene. A client-only mixin makes the remote plain block entity
implement `Aero_CellRenderableBE`; the byte-identical common/server class stays
free of Aero, Minecraft-client, and LWJGL dependencies.

The fixed plan crosses two Y cells and two Z cells, producing exactly four
cached pages. Every retained record proves sixteen renderer identities,
sixteen real enqueues, two flush method calls, four cached pages, four page
display-list calls, zero direct fallbacks, and zero page rebuilds. M74 records
state `16/16`, mask `0xffff`, and visible chunks while its per-BE Aero render
and list counters remain `0/0`, as expected for cached page replay.

M78 reuses the aligned primitive-timer design: full renderer, nested enqueue,
and aggregate flush spans are copied into a fixed sidecar at the same index as
each M74 census record. No retained per-sample allocation or I/O occurs; both
artifacts are written and reparsed only after the bracket seals.

The two fresh same-plan/nonce replicas observed renderer medians of `4200` and
`3600 ns`, enqueue medians of `1800` and `1600 ns`, and populated-flush medians
of `27800` and `25700 ns`. Every flush aggregate was positive. These values are
descriptive spans under M78 instrumentation.

Nonclaims: page compilation/rebuild cost, cold-cache behavior, generic Aero BE
compatibility, uninstrumented or additive cost, causal attribution, regression
or improvement, inference, pixels, cross-machine generality, combat relation,
or historical lag reproduction.
