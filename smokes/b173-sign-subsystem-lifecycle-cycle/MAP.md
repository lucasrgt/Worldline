<!-- worldline-map-schema=1 -->
<!-- boundary=b173-sign-subsystem-lifecycle-cycle -->
<!-- nonclaims=native-render,selection-box,sign-crafting,arbitrary-invalid-text,non-sign-tile-entities -->
<!-- frozen-trace=1a6ccdee8901506c433e50ef0b630fadb7f65ba0e3704686cbd19a85cb0e27a9 -->

# Beta 1.7.3 sign subsystem lifecycle

The public `SignSubsystemFixture` executes one coherent standing-sign and wall-sign subsystem.
An official player consumes sign item `323` to reach every standing-sign metadata value `0..15`,
directly breaks a placed sign, and observes the exact `323x1:0` drop. The same causal run places
the canonical `63:4` standing sign and `68:5` east-facing wall sign, writes both four-line texts
through protocol-14 Packet130, and recovers their exact states and texts after a clean save and
fresh login.

Two server-authoritative movement lanes cross the sign cells without correction. Both cells emit
block light `0`, retain sky light `15`, and hold their exact state for 240 bounded ticks. Breaking
the standing support and wall attachment support removes both signs through native neighbor
handling; final air survives a second clean save and login.

The map does not claim native client rendering, selection boxes, crafting, arbitrary invalid text,
or behavior shared by unrelated tile entities.

Frozen aggregate signal:
`family=sign-subsystem-lifecycle,claims=13,standing-domain=0..15,placed=63:4+68:5,inventory=20->19,break=63:0->0:0,drop=323x1:0,collision=UU,light=0:15x2,tick=240,reload=FRESH_LOGINx2,support=63:4+68:5->0:0+0:0,evidence=9e2f1dfce178bfdb3bb2e8bd2d38f0e0448288cedfa2c04601ce6455f8e817e8`.

Qualified semantic signature:
`1a6ccdee8901506c433e50ef0b630fadb7f65ba0e3704686cbd19a85cb0e27a9`.
