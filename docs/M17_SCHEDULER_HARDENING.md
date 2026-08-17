# M17 Scheduler Hardening

M17 broadens M16 from one fixed camera/save to stationary empty, stationary
dense, and moving dense workloads. The candidate retains the explicit
accepted/deferred boundary and drains background work, but the matrix rejects
promotion.

## What generalized

Across all three first-200-frame windows, adaptive makes exactly one caller
completion per frame, accepts bounded real work, reports no stalled outcome,
and eventually drains the global and visible queues. Its visible-first policy
beats vanilla's normalized readiness frontier in both stationary workloads.
The old Aero governor rejects work continuously and retains bounded-run backlog,
confirming the retry-loop failure diagnosed in M13.

## What did not generalize

The moving camera's readiness frontier trails vanilla during the measured
window before eventually draining. Timing is mixed by scenario and therefore
remains observational. The 12 ms envelope can stop only between rebuilds; one
large rebuild can overshoot it substantially.

The frozen-frame oracle removes non-player entities, fixes time, weather,
camera, interpolation, and HUD, then requires 200 globally drained stable
frames. Nevertheless, every scenario exceeds M16's strict full-frame
64-pixel/delta-2 threshold. M17 treats this as detected divergence, not as a
reason to broaden tolerance.

## Shipping boundary

The packaged evaluation profile is default-off and explicitly marked
`lab-only-no-go`. No source is changed in the pinned Aero checkout, no public
Worldline API is added, and no claim is made that the historical random spike
is eliminated. Promotion requires a finer preemptible work unit and a visual
oracle that either proves equivalence or attributes the divergence precisely.
