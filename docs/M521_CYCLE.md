# M521 qualification cycle

M521 verifies the official multiplayer hotbar boundary when a player selects
an empty slot. The observer must receive Packet5 with item `-1`, the previous
stack must remain intact, an invalid slot must be rejected, and selection must
recover to slot zero.

The expected signature is
`272e63d1ae30e3865b17feceb300a7b502c6a49dc8d151edc108412e32391034`.
Qualify it with `java tools/harness/Gate.java --milestone
m521-sw-hotbar-empty-selection`.
