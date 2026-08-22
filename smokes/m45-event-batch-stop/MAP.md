# M45 Event-Boundary Batch Stop

Two fresh sessions submit two correlated plans. The first plan contains two
safe alternatives. A caller-thread event controller returns `STOP` at
`route 0 / alternative 0 / outcome 0 / PRIMARY`.

The first route must return one outcome with `CONTROLLER_STOP`; its second
alternative and the second batch plan must remain absent. The batch also
reports `CONTROLLER_STOP`. M45 adds no rollback, asynchronous delivery,
parallelism, retry, registry, or adapter behavior.

Frozen expected signature SHA-256: `84d799547e96d434049f4879778606a592b3159626bf9df9b7e8225aeb9ca5d6`
