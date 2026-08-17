# M16 Adaptive Visible-First Chunk Scheduling

M16 turns M15's safe deferral boundary into a bounded scheduling candidate. It
answers whether the initial chunk backlog can be processed without the vanilla
same-frame retry loop, without the visible-readiness regression of a fixed
two-rebuild batch, and without changing the final rendered frame.

## Adaptive envelope

The sole `GameRenderer.renderFrame` call site still invokes the adapter-owned
`Aero_ChunkWorkContract` once per rendered frame. Visible dirty debt selects an
accepted-work limit of 2, 4, 6, or 8 rebuilds. When visible debt is zero, the
maximum band drains background work. Selection retains vanilla's
`DirtyChunkSorter`; while visible debt exists, only in-frustum builders are
eligible.

The rebuild loop also stops after 12 ms once it has accepted at least one item.
Its result remains `ACCEPTED_DEFERRED`, mapped to end the current caller loop and
resume on the next frame. The time budget is an envelope, not a hard real-time
deadline: one rebuild may itself exceed it.

Across the first 300 frames of the release-gate run, baseline made 1,206 calls.
Adaptive made exactly 300 calls, accepted 1,668 rebuilds, selected visible work
for every accepted item, and reported no stalled outcome. Its minimum visible
dirty count was 644 versus baseline's 856, while maximum visible-ready count was
1,066 versus 840.

## Fixed-state framebuffer oracle

Tick 20 freezes game ticks, player position, yaw, pitch, velocity, interpolation,
HUD, and view bobbing. The oracle waits until every chunk builder is clean and
the visible set is built, then requires 20 stable cull frames before reading the
complete 854 by 480 RGBA framebuffer.

A same-seed world was not sufficient: chunk population order produced small
vegetation differences between independent saves. The runner therefore creates
one canonical fully generated save, copies it, and restores those exact bytes
before both measured processes. Generated saves, images, and logs remain under
ignored roots.

Both queues reached zero before comparison. The release-gate run matched all
409,920 pixels exactly; exploratory repetitions exposed up to 12 low-bit edge
pixels within the frozen limit of 64 pixels and delta 2. Baseline required 1,924
frozen render frames and adaptive 1,887. Its observed worst frame fell from
735.2 ms to 218.6 ms; p95 and absolute timing remain reported observations
rather than frozen cross-machine thresholds.

## Decision and non-claims

The visible-first adaptive envelope is a qualified adapter candidate. It is not
merged into Aero, exposed as a Worldline product API, or claimed optimal. The
proof covers the pinned Aero revision, fixed seed/save, camera, view settings,
tick, configured 8-work/12-ms maximum, and declared strict pixel tolerance.

M16 does not prove elimination of every historical random spike or generalize
performance across hardware, saves, camera paths, view distances, or mods. M17
should broaden that matrix and prepare an opt-in upstream integration without
weakening the explicit next-frame contract or framebuffer oracle.
