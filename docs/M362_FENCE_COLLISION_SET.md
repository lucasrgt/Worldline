# M362 fence collision set

M362 opens the official fence-collision boundary. Packet15 of fence item
`85` places two adjacent blocks `85:0` on the raised-stone fixture. The
headless actor then Packet13-walks the same intended `+1 Z` step in air
and into that fence line.

The air step is unchallenged (`1000` milli-blocks). The fence step is
server-corrected back to the start (`0` milli-blocks), so the walk is
blocked versus air. Two official server JVMs must match. Both fence
cells survive a clean save plus fresh login.

Frozen semantic SHA-256:
`5784076d8eb5c6e86478f102566067459f9c73c231b5f92141b25d65c79ae290`.

This is distinct from M173 place-only and M329 craft-only. This
milestone does not claim jump-over or fence-gate. Fence gates do not
exist in Beta 1.7.3. Headless `B173WireClient` only. No GUI. No Aero.
