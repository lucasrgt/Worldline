# M548 piston BUD set

M548 opens the official classic piston-BUD pulse boundary. It clones the
M367 west-facing piston-`33` arm, then proves one neighbor-place extension
pulse with no continuous power.

One headless session builds piston `33:4` and a stone payload. There is no
lever. Packet15 of torch item `76` on that payload is the neighbor update.
The official server emits moving piston `36:4`, then retracts to `33:4`
with the payload left behind and the torch gone. The frozen signal includes
`bud-pulse` and `power=none`. Those final cells remain after a clean save
plus fresh login.

Frozen semantic SHA-256:
`64edc418a23140583ce5015dead697010582f99862cc5e19d6e8e7e53f02bcff`.

This is distinct from M367 lever-power and from M546 QC-with-power-above
remaining on. It does not claim sticky BUD, dispenser QC, or a generic
redstone evaluator.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.
