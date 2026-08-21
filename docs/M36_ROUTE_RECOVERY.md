# M36 Route Recovery

M36 adds immutable `MovementStep` and `MovementRouteResult` values and a
`RecoveringMovementMultiplayerSession` composition. A route contains at most
64 non-zero relative steps. Each step uses the M35 bounded observation, so a
later step naturally begins from the latest resulting pose rather than from a
predicted path.

The official smoke executes three steps on each of two fresh servers: the
persistable `+0.125 X` move, a move into a cache-selected solid terrain cube,
and another `+0.125 X` move. The middle step must receive Packet13 correction
to the first pose; the final step must remain unchallenged from that rollback.
The route requires exactly one correction, cache retention, and persisted final
coordinates after disconnect and save.

## Non-claims

M36 does not choose routes, retry obstacles, model collision shapes, infer why
a correction occurred, run asynchronously, or control server ticks. It only
defines bounded ordered composition and authoritative recovery semantics.
