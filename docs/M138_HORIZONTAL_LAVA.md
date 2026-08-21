# M138 Horizontal lava

Status: GO in Worldline v1.126.0.

M138 adds the first scheduled lava transition. A raised stone trench contains
one still-lava block `11:0` behind a dirt gate. A fresh client removes the gate
with Packet14, observes exact air through Packet53, and waits for vanilla's
slower lava schedule. The adjacent target becomes `11:2` while the source
remains `11:0`.

The causal hash is deliberately scoped to the predeclared source and target
cells. Seventy official ticks also execute unrelated random ticks elsewhere in
the generated chunk; those changes are not attributed to lava. A third client
proves the two fixture states after save.

M138 does not claim bucket interaction, vertical flow, lava-water reactions,
fire ignition, entity damage, arbitrary metadata gradients, cross-chunk flow,
exact timing below the bounded window or unrelated chunk stability.
