# M40 Observer-Directed Route Control

Two fresh sessions begin with a corrected primary, execute its supplied
fallback, and synchronously return `STOP` from that fallback event. The exact
events must be `0:0:PRIMARY`, `0:1:FALLBACK`; a second alternative must never
be sent.

The controller runs on the caller thread and receives the same immutable
outcome objects returned by the route. It introduces no executor, queue,
automatic retry, path discovery, or adapter behavior. Cache remains coherent
and official player NBT persists the accepted fallback pose.

Frozen expected signature SHA-256: `6a3285b118eccd8b3f1e95ba51e7f6de46933c168b9f56f2623b11d8d266da7b`
