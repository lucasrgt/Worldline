# M39 Synchronous Route Observation

M39 adds `MovementRouteObserver` and immutable `MovementRouteEvent` values.
Every resolved M38 primary or fallback emits one event immediately, on the
caller thread, before the next network movement begins. Events identify their
alternative index, global outcome index, and `PRIMARY` or `FALLBACK` kind. The
event retains the same `MovementOutcome` object later exposed by the route.

The official smoke observes the M38 sequence on two fresh servers and requires
exactly `0:0:PRIMARY`, `1:1:PRIMARY`, `1:2:FALLBACK`. It rejects thread changes,
delayed indexes, or copied outcomes. The cache remains loaded and the final
fallback pose persists after disconnect and save.

## Non-claims

M39 does not add asynchronous callbacks, event queues, cross-thread delivery,
observer-directed cancellation, path discovery, or server tick control. An
observer exception propagates synchronously and stops later orchestration.
