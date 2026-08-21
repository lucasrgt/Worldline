# M524-SW Packet14 dig-status boundaries

M524-SW freezes the official Beta 1.7.3 Packet14 start/finish boundary without
importing a cancellation status from a later protocol. Status `0` starts the
gold-shovel break and status `2` finishes it. Status `1` alone is ignored, and
status `2` without a matching start leaves its dirt cell unchanged.

The positive air cell and both unchanged dirt controls survive a clean save
and fresh server restart. The public Worldline session remains limited to
`beginBreak` and `finishBreak`; raw status emission exists only in smoke code.

M524-SW does not claim durability, privileges, instant breaking, client-side
animation, or a vanilla cancellation packet.

Frozen semantic SHA-256:
`bb181bdd9db372111cec4f232aad960bc58be6fc343dc384dc4ccb89dd1f32ea`.
