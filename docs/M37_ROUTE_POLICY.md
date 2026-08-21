# M37 Route Correction Policy

M37 adds `RouteCorrectionPolicy` to the M36 route composition. `CONTINUE`
preserves the existing behavior. `STOP_ON_CORRECTION` records the corrected
outcome and immediately returns without retrying that movement or sending any
later step.

The official smoke supplies three steps to two fresh servers: a persistable
small move, a move into a decoded solid terrain cube, and another small move.
Stop policy must return exactly two outcomes with one correction. After clean
disconnect and save, player NBT must equal the first step's pose, proving the
third step was not applied. The original cached chunk must remain loaded.

## Non-claims

M37 does not choose an alternate path, retry an obstacle, infer correction
causes, execute concurrently, or control server ticks. Policy is explicit and
bounded by the same 64-step M36 ceiling.
