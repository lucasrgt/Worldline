<!-- worldline-map-schema=1 -->
<!-- boundary=external-aero-runtime-qualification -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=a7473c0e9e1992edf4dc77c15ea1ab929c5876768b668b2be84a05ef559f674a -->

# M778-AERO-CULLING-VISUAL-INTEGRITY behavior map

The boundary is a pinned AeroModelLib StationAPI client observed through
Worldline's full-frame oracle. It does not claim arbitrary GPUs, scenes,
resolution independence, occlusion-query culling, or hidden-face removal.

Fixture: one restored world with 120 complex static MegaCrusher block entities,
four separated panels inside an opaque static bedrock enclosure, frozen world ticks, time
6000, clear weather, no ambient entities or client-clock clouds, hidden HUD, fixed tick delta, and
view distance one.

Actions: two fresh counterbalanced sessions prewarm the shared model and
toggle the broad cone `cull-off` and `cull-on` within the same frozen client
state while the unsafe strict block-entity layer remains disabled. Each arm
holds eight central 45-degree views and four near-panel 90-degree views for
twenty render frames before capturing the complete RGBA framebuffer.

Observations: the opposite-order sessions create two temporal phases for each
checkpoint. Matching phases with opposite culling states provide 24 exact
full-frame pixel pairs, SHA-256, changed-pixel count, maximum channel delta,
at-rest model renders, display-list calls, and effective view culls. Pixel changes are forbidden.
Culling must reduce at-rest render work in both sessions and to at most 95
percent in aggregate.

Claim: `scene=panels120-static-mega+static-enclosure+prewarmed,sessions=2-counterbalanced,cameras=center8x45+near4x90,world=frozen-no-clouds,culling=broad-cone-safe,pixels=exact24of24-phase-matched,work=reduced2of2`.

Frozen trace: `v9|scene=four-panels-120-static-mega+static-bedrock-enclosure+prewarmed|sessions=2|orders=within-client-off-on+on-off|pairing=cross-session-phase-matched|checkpoints=center8x45+near4x90|hold=20|world=frozen-tick+time6000+clear-weather+no-entities+no-clouds|contrast=frustum-off-vs-broad-on+be-view-off|other-culls=off|oracle=full-rgba-exact+draw-work-reduction|captures=12-per-arm-within-client`.

SHA-256: `a7473c0e9e1992edf4dc77c15ea1ab929c5876768b668b2be84a05ef559f674a`.
