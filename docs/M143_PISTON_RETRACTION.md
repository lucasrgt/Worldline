# M143 piston retraction

M143 closes the normal-piston cycle established by M142. It adds no public API
and leaves all piston mechanics server-authoritative.

The fixture is first built and extended through the exact M142 protocol path.
After clean save, a fresh client proves lever `69:9`, base `33:12`, head `34:4`
and displaced stone `1:0`. That independent extended snapshot is the M143
precondition, not an adapter prediction.

One empty-hand Packet15 then deactivates the lever. The official piston event
returns the lever to `69:1` and base to `33:4`, removes the head to air, and
leaves the non-sticky piston's displaced stone at its destination. A second
clean save and another fresh Packet51 prove the same final states.

Exactly three cells change in the raised fixture: lever, base and head. The
stone destination is explicitly invariant. As in M142, generated water below
the tower is outside the causal digest.

This milestone does not claim sticky pulling, repeated clocks, multi-block
motion, transient block `36`, collision, timing, sound, cross-chunk behavior,
quasi-connectivity, or a generic piston model.
