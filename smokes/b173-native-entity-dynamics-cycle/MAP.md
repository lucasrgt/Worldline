# Native entity dynamics semantic map

Public behavior: `native-entity-dynamics` (`WorldlineEnvironmentBehaviors.NATIVE_ENTITY_DYNAMICS`).
Public acceptance: `NativeEntityDynamicsFixture#verify`.

| Boundary | Native actions | Observations and control |
| --- | --- | --- |
| Motion | `Entity.moveEntity(5,0,0)` | Exact five-block free displacement versus a native wall collision and horizontal-motion cancellation |
| Hanging motion | `EntityPainting.moveEntity` | Nonzero movement breaks the painting and produces one native item entity; zero movement preserves it |
| Immersion | Virtual `Entity.onUpdate()` | Eye-material query differs between dry and wet worlds; after the native tick, short chicken/snowball flags remain false while other immersion subjects become wet |
| Support | Virtual `Entity.onUpdate()` | TNT downward motion arrests on support; falling sand settles as block 12 only on support |
| Rail condition | Virtual `Entity.onUpdate()` | Moving minecart is compared on a rail strip and a bare floor |
| Timed recovery | Two virtual native updates | Under a roof excluding daylight damage, living hurt counters and vehicle hit counters decay from two to zero while inactive controls remain zero |
| Expiry | One virtual native update | Item age 6000 and grounded projectile age 1200 remove the entity; one-earlier controls survive |
| Periodic support check | One virtual native update | Unsupported painting checks at native counter 100; counter 99 delays removal |

All setup and native dispatch use the existing named/server namespaces in the hash-pinned Tiny file.
The untouched official dedicated-server JAR is authoritative. No decompiled source is treated as
the oracle or distributed. Class and input provenance are qualification guards. Canonical evidence
contains only subject/layer/rule identity and measured paired native outcomes.

The abstract registry type 48 is excluded from the concrete subject plan. Existing independently
qualified abstract applicability and stronger gameplay claims are preserved. Census promotion binds the successful four-process capture through the public fixture.

Frozen four-process signature:
`ad1d0d856769c15c32bdaf03705659dccf6eaf28effa872e587860a3f491e64a`.

Required semantic signal: `family=native-entity-dynamics,subjects=23,cases=66,processes=4,oracle=mapped-official-native-state`.
Required trace: `v1|server=official-b1.7.3|family=native-entity-dynamics|subjects=23|cases=66|oracle=mapped-official-native-state`.
