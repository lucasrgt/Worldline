# M521-SW-HOTBAR-EMPTY-SELECTION Sw hotbar empty selection

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

This milestone freezes the behavior identified by its expected signal and semantic signature.

## Qualification cycle

M521 verifies the official multiplayer hotbar boundary when a player selects
an empty slot. The observer must receive Packet5 with item `-1`, the previous
stack must remain intact, an invalid slot must be rejected, and selection must
recover to slot zero.

The expected signature is
`272e63d1ae30e3865b17feceb300a7b502c6a49dc8d151edc108412e32391034`.
Qualify it with `java tools/harness/Gate.java --milestone
m521-sw-hotbar-empty-selection`.

Expected signal: `slot1=empty,packet5=-1:0,slot0=1:0,slot9=rejected,selection=slot0`.

Frozen semantic SHA-256: `272e63d1ae30e3865b17feceb300a7b502c6a49dc8d151edc108412e32391034`.
