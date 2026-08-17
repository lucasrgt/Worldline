# M40 Observer-Directed Route Control

M40 adds `MovementRouteController` and `MovementRouteDirective` as a separate
control boundary above M39 observation. `moveRouteWithFallbackUntil` invokes
the controller synchronously after each resolved immutable event. The returned
directive is applied before any fallback or later alternative can be sent.

The live oracle deliberately produces a corrected primary, continues into the
single supplied fallback, then returns `STOP` from the fallback event. The
result contains exactly those two identity-bound outcomes, the second supplied
alternative remains absent, cache stays coherent, and official player NBT
persists the fallback pose.

The existing `MovementRouteObserver` remains unchanged. M40 adds no thread,
event queue, automatic retry, path discovery, goal inference, server tick
control, complete physics simulation, or adapter behavior.
