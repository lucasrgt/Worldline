# SEM-M13 Complete Mapping Batch

SEM-M13 selects all 6,475 maintained Beta 1.7.3 graph identities with at least
two independent mapping sources. Its report sets `complete=true` only when the
cumulative selection equals the complete qualified graph.

Batch membership and report order depend only on canonical graph identities.
Smoke source, descriptor, and documentation text are deliberately absent from
the mapping fingerprint, so later behavioral milestones cannot rewrite a
sealed mapping proof.

Eleven old Nostalgia-only entries remain explicitly reported as excluded.
They have no Calamus official identity, no RetroMCP resolution, and are absent
from the current Feather artifact. They are therefore retracted from the
maintained graph rather than converted into invented mappings. The exclusion
list is part of every batch report digest and cannot disappear silently.

This boundary qualifies mapping identities. It does not claim that conflicting
external aliases are semantically equivalent, and it does not name JVM
constructors, class initializers, library members, or other official-bytecode
gaps that mapping formats intentionally leave unnamed. Those remain visible in
the SEM-M6/SEM-M7 audit surfaces. The exact policy is
`mappings/b1.7.3/sem-m13.properties`.

The runtime profile acquires the pinned public artifacts, verifies the official
client and server inputs, reconstructs all three reports, and applies the M11,
M12, and M13 gates. Static tests separately prove deterministic selection,
explicit orphan handling, and fail-closed policy drift.

The project-wide definition and the diagnostic queue non-claim are fixed in
[the mapping constitution](MAPPING_CONSTITUTION.md).
