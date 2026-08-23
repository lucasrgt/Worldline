<!-- worldline-map-schema=1 -->
<!-- boundary=state-world-differential -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=272e63d1ae30e3865b17feceb300a7b502c6a49dc8d151edc108412e32391034 -->

# M521-SW behavior map

The actor starts with stone `1:1:0` in hotbar slot 0 and an empty slot 1. An
independent observer first receives the actor's populated carried item. Packet16
then selects slot 1, and the observer must receive an explicit Packet5 equipment
update encoded as item `-1`, damage `0`. The actor's authoritative inventory
view independently identifies the selected slot as empty.

The negative returns to populated slot 0 and requires Packet5 stone `1:0`. The
mutation sends raw Packet16 slot 9, outside the official `0..8` bound. After a
bounded wait, both the actor selection boundary and the observer's last
authoritative equipment state must still be populated stone.

Official `NetServerHandler.handleBlockItemSwitch` accepts only values from zero
through `InventoryPlayer.func_25054_e()` and logs invalid selections without
changing `currentItem`. Packet5 empty equipment is distinct from Packet20 spawn,
whose empty carried-item encoding is zero.

This milestone does not claim window clicks, drops, transfers, persistence,
respawn selection, peer animation, or malformed packet lengths. Its frozen
semantic signature is
`272e63d1ae30e3865b17feceb300a7b502c6a49dc8d151edc108412e32391034`.

## Frozen semantic signal

`slot1=empty,packet5=-1:0,slot0=1:0,slot9=rejected,selection=slot0`
