# M115 qualification cycle

`LeverActivationCycle` verifies the official server artifact, compiles the
new neutral activation interface, protocol-14 adapter and smoke, and runs two
fresh official worlds. Each uses one actor and one fresh reader session.

The actor constructs the same ten-stone support column, places the side lever,
fixes orientation, and waits 200 ticks. The runner requires Packet53-derived
off/on states, the full-chunk one-state delta, fresh Packet51 state, traces and
signatures to match across both worlds before checking frozen evidence.
Diagnostic mode cannot qualify.

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`497b5d743a5693c925d69d71c02528cf2d16a63ad5c477980b916a0d2b45ae34`.
