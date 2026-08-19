# M134 Nether portal roundtrip

Status: GO in Worldline v1.122.0.

M134 composes both portal directions on one authenticated protocol-14 session.
After the qualified outbound trip, the client derives the Nether portal plane
from six decoded block-90 cells, moves outside it for a bounded cooldown, and
re-enters. The official server emits Packet9 `-1→0`, corrects the pose and sends
the Overworld chunks again.

Both transitions invalidate the old dimension's remote cache. The return view
contains six active portal cells and fourteen obsidian frame cells, and clean
persistence records dimension `0`. Depending on the generated Nether portal's
position, vanilla may reuse the source or create another Overworld portal.

M134 does not claim the minimum cooldown, portal reuse, exact search coordinates,
arbitrary coordinate scaling, concurrent travelers, entity transport, portal
destruction, death/respawn, repeated unbounded journeys, or client rendering.
