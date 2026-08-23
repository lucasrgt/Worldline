# M524-SW behavior map

The official Beta 1.7.3 client emits Packet14 status `0` when block breaking
starts and status `2` when it finishes. The server handles those statuses,
status `3` block resynchronization, and status `4` held-item drop. It ignores
status `1`; the client has no network cancellation status in this version.

The smoke places three dirt `3:0` cells over a raised stone fixture. A gold
shovel `284` and status `0` followed by status `2` remove the positive cell.
Status `1` alone leaves the negative cell invariant. Status `2` without a
matching start leaves the mutation cell invariant. A fresh server restart
proves the resulting air/dirt/dirt states persisted.

This map does not claim a cancellation packet, arbitrary hardness timing,
durability, privileges, instant breaking, or client animation.

Frozen semantic SHA-256:
`bb181bdd9db372111cec4f232aad960bc58be6fc343dc384dc4ccb89dd1f32ea`.

## Frozen semantic signal

`column=17,support=4:71:4,positive=4:72:4:3:0->0:0,status=0+2,ignored=3:72:4:3:0->3:0,status=1,orphan=5:72:4:3:0->3:0,status=2-only,tool=284,persisted=true,clients=2,disconnect=clean`
