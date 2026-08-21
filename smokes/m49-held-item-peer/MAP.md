# M49 Held Item Peer Observation

Two neutral protocol clients join each fresh official Beta 1.7.3 server. The
actor acquires distinct stone and dirt stacks in hotbar slots 0 and 1 through
the qualified M48 inventory path. It sends Packet16 to select slot 1. The
independent observer correlates the actor's Packet20 named spawn and receives a
Packet5 equipment update containing dirt.

The observed item is server-authoritative: the observer never reads the
actor's local inventory, server memory, or player NBT. NBT is inspected only
after both clients disconnect and the server saves, as independent persistence
evidence.

This cycle does not claim click-window mutation, equipment counts, selection
acknowledgement to the initiating client, arbitrary containers, or tick control.
