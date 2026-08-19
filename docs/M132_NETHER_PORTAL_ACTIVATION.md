# M132 Nether portal activation

Status: GO in Worldline v1.120.0.

M132 constructs a complete portal frame through ordinary held-block Packet15
interactions on the official Beta 1.7.3 server. Before ignition, all fourteen
frame blocks are obsidian and all six interior cells are air. A second Packet15
interaction with flint and steel invokes vanilla portal recognition and fills
the interior with block `90:0`.

The live client observes the activation and a fresh client independently proves
the persisted frame and portal. The frozen delta is restricted to the six
causal interior cells because unrelated scheduled world updates are not portal
effects.

M132 does not claim player traversal, Packet9 emission, coordinate scaling,
Nether portal generation, return travel, arbitrary frame sizes or orientations,
portal destruction, entity transport, or client rendering.
