<!-- worldline-map-schema=1 -->
<!-- boundary=b173-rail-network-subsystem-conformance-cycle -->
<!-- nonclaims=registry-presence,gameplay-placement,break-transition,drop-matrix,save-reload,native-render,minecart-motion,minecart-collision-transfer,downstream-powered-propagation,powered-rail-neighbor-response -->
<!-- frozen-trace=5e3d3164fd4a0ed33c6d628f7ac267dc19e3a35740acce3054607ca8de10666c -->

# Beta 1.7.3 rail network

This package treats powered rail `27`, detector rail `28`, and normal rail `66` as one rail-network
subsystem instead of unrelated block counters. It proves their native metadata domains, flat and
ascending selection envelopes, empty collision boxes, zero light-table entries, tick policies, and
support dependency. The detector additionally proves a complete `0 -> 8 -> 0` minecart pulse.

Normal rail accepts routing states `0` through `9`. Powered and detector rails accept shapes `0`
through `5` with an independent powered bit, yielding states `0..5` and `8..13`. The native bounds
routine does not mask that bit: shape `2` is ten-sixteenths high while powered state `10` returns to
the flat two-sixteenths envelope. This historical behavior is deliberately preserved as evidence.

The detector proof first verifies that exactly one minecart intersects its native query volume,
then invokes the same private evaluator on mapped and untouched official classes. Removing the cart
and running the native 20-tick update clears the powered bit. Removing the supporting block under
each rail transitions it to air and creates exactly one item entity.

Registry, gameplay placement, ordinary break/drop matrices, save/reload, rendering, minecart
movement and collision transfer, downstream power propagation, and the already-proved powered-rail
neighbor response remain separate evidence families.

Frozen signal:
`family=rail-network,subjects=27+28+66,claims=14,states=rail-routing+powered-bit+detector-pulse,bounds=flat+slope+powered-slope-quirk,collision=none,light=0/0,ticks=stable+20,neighbors=support-drop,oracle=MATCH`.
