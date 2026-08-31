<!-- worldline-map-schema=1 -->
<!-- boundary=external-aero-runtime-qualification -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=27a0cd70f22302ef30827b597211def5f440ab8ffe985216da3b4f730539f888 -->

# M777-AERO-ADAPTIVE-PREWARM-REPEATABILITY behavior map

The boundary is a pinned AeroModelLib StationAPI client observed through
Worldline's neutral profiler gate. It does not claim results for other GPU
drivers, other scenes, or unqualified property combinations.

Fixture: one restored world with 120 block entities arranged into four
view-separated panels, fifteen loaded model identities, and loader-only decoys.

Actions: four fresh counterbalanced sessions run `cold`, `blind`, `adaptive`,
and `pressured` clients. Each client performs abrupt first-sight turns, stable
panel views, a near-panel teleport, and a spin. The pressured arm queues one
speculative model in frame one and explicitly promotes it in frame two, proving
pressure pauses speculation without starving urgent work. The adaptive arm
adds exactly four hidden observations for the fixture's real MegaCrusher
identity so hotness admission must either drain before visibility or record a
synchronous first-use miss without changing the useful display-list set.

Observations: the identical first 600 frames from every client supply frame
wall time, render-thread CPU time and allocation, per-frame at-rest render and
display-list-call work, display-list
allocation/lifetime, prewarm queue/drain, adaptive admission, render-pressure
skips, synchronous first-use misses, OBJ-cache population, and paired
first-sight hitch rate above 50 ms. The four-pair aggregate owns the 5,000 ppm
hitch bound; aggregate metrics and three-of-four per-metric sessions own the
FPS, p99, and allocation bounds.

Claim: `scene=panels120x15+decoys,sessions=4,window=fixed600,arms=cold+blind+adaptive+pressured,journey=turn+hold+teleport+spin,adaptive=hidden4-MegaCrusher+hotness4,pressure=probe+urgent,miss=sync,hitch=safe,metrics=aggregate+3of4,decoys=4of4,attribution=cpu+render-work`.

Frozen trace: `v2|scene=four-panels-120-15-models-plus-loader-decoys|sessions=4|orders=cold-blind-adaptive-pressured+blind-pressured-cold-adaptive+adaptive-cold-pressured-blind+pressured-adaptive-blind-cold|journey=first-sight60+north60+east120+south120+west120+spin120|admission=hidden-probe4-MegaCrusher+loader-decoys|lists=cold-vs-blind1-vs-adaptive1|pressure=explicit-speculative-probe+0.1ms+urgent-promotion|capture=wall+cpu+allocation+per-frame-render-work+display-lists+prewarm+admission+pressure+first-use-miss+obj-cache|gates=fixed600+aggregate+hitch5000ppm+fps3pct+p995pct+alloc5pct+3of4+decoy4of4+drain`.

SHA-256: `27a0cd70f22302ef30827b597211def5f440ab8ffe985216da3b4f730539f888`.
