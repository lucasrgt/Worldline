# M127 qualification cycle

`CrossChunkRedstoneRecoveryCycle` repeats fixture construction, powering,
fresh-client precondition, deactivation and fresh-client recovery in two new
official-server worlds.

Both runs must reproduce the inverse one-state deltas, empty residuals, trace
and frozen SHA-256:
`269f3a7043dc7c483f160233c36890ef075faf03e36300801aa5779f06b05aa2`.

Canonical evidence uses two official server JVMs and six client sessions.
