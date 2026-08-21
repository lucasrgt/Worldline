# M157 behavior map

A selected bow with an inventory arrow fires through Packet15 air-use
(direction 255). EntityTracker broadcasts Packet23 type 60 to every nearby
player, including the shooter. Thrower extras exist only when the shooter
entity id is greater than zero.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone-platform|cause=packet15-air-bow261+arrow262|wire=packet23-type60|oracle=two-peer-identical-arrow-object-spawn|column=17,bow=261,arrow=262,wire=packet23-type60,thrower=actor,shared-id=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`abf0244450acb0b727df9080a6ca53849fcd0ca4ce62de83a13b815d04c8f917`.
