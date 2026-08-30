<!-- worldline-map-schema=1 -->
<!-- boundary=external-aero-runtime-qualification -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=c4d85d0b85f35b98bad4d8ba8d5a26b217d5a11c8393015a3cf9de1e253d21b7 -->

# M776-AERO-PROFILER-GL-PREWARM-LOD-MATRIX behavior map

The boundary is a pinned AeroModelLib StationAPI client observed through
Worldline's neutral profiler gate. It does not claim results for other GPU
drivers, other scenes, or unqualified property combinations.

Fixture: one restored world with 120 block entities arranged into four
view-separated panels and fifteen distinct model identities.

Actions: four counterbalanced rounds run fresh `direct`, `cold`, `prewarm`,
and `lod` clients. Each client performs abrupt first-sight turns, stable panel
views, a near-panel teleport, and a spin.

Observations: frame wall time, render-thread allocation, at-rest render path,
animated instances, display-list allocation/lifetime, prewarm queue/drain, and
paired first-sight hitch rate above 50 ms.

Claim: `scene=four-panels-120,models=15,rounds=4,arms=direct+cold+prewarm+lod,journey=first-sight+stable-views+spin,display-lists=activated,prewarm=activated+drained,budget=1,lod=activated,hitch=no-regression,allocation=measured`.

Frozen trace: `v1|scene=four-panels-120-15-models|rounds=4|orders=direct-cold-prewarm-lod+cold-lod-direct-prewarm+prewarm-direct-lod-cold+lod-prewarm-cold-direct|journey=first-sight60+north60+east120+south120+west120+spin120|lists=off-vs-cold-vs-prewarm1|lod=off-vs-28|capture=wall+allocation+display-lists+prewarm+at-rest+animated|hitch=first-sight-50ms+5000ppm|claims=activation+drain+hitch-safety`.

SHA-256: `c4d85d0b85f35b98bad4d8ba8d5a26b217d5a11c8393015a3cf9de1e253d21b7`.
