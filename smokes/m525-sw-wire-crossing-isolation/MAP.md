# M525-SW behavior map

The lower dust component runs east-west at Y65. The upper component runs
north-south at Y67 on a stone bridge, crossing the lower component in X/Z
projection with a solid block between them. Vanilla dust metadata is sampled
after each notification/tick phase.

First, the lower source powers only the lower component. Removing its middle
connector depowers the lower tail without affecting the upper component. The
mutation removes the lower source and powers the upper source, reversing which
component carries `15,14,13` while the other stays zero.

This milestone does not claim repeaters, quasi-connectivity, burnout, buttons,
pistons, persistence, or arbitrary circuit isolation.

Frozen expected signature SHA-256: bbd05e6a5e18bbeeda9ff5bc0b8ad3fcca475ac38719e0ce2d98047f8e91f5b5
