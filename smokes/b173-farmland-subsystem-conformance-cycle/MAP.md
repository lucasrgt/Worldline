<!-- worldline-map-schema=1 -->
<!-- boundary=b173-farmland-subsystem-conformance-cycle -->
<!-- nonclaims=horizontal-hydration-range,vertical-water-rule,rain-hydration,trampling,crop-retention,tick-policy,native-render -->
<!-- frozen-trace=7e3ab53109572ad7404382a8ecc0492abb956eb664e0f807e6e5abb731b860ee -->

# Beta 1.7.3 farmland subsystem conformance

`FarmlandSubsystemFixture` maps block `60` as native stateful soil. A supplied `60:0` item
follows the ItemBlock placement route, creates one `60:0` cell, and consumes the one-item stack.

Player-relative break strength is finite. The native server harvest sequence removes farmland and
produces exactly one dirt `3:0` item entity. Adjacent water plus a deterministic native update
hydrates metadata 0 to 7. After native chunk serialization preserves `60:7`, removing that water
and applying seven deterministic updates traverses every moisture value back to `60:0`.

Farmland exposes a full-cube collision box even though its maintained visual bounds end at 15/16
height. It is nonopaque, does not render as a normal cube, and its light tables report opacity 255
with emission 0. Ordinary neighbor notifications with air above preserve `60:0`; placing a solid
stone cover causes the native neighbor callback to convert it to dirt `3:0`.

The Functional Census profile is enriched to
`farmland,random-tick,special-collision,stateful-metadata,tick-driven`. Tick-policy remains owned
by `m608-farmland-dry-set`, while native-render remains owned by
`m703-native-3d-inventory-render`; this milestone promotes only eight previously unknown claims.
It does not claim horizontal hydration range, vertical water rules, rain hydration, trampling, crop
retention, tick scheduling distribution, or native rendering.

Frozen aggregate signal:
`family=farmland-subsystem,subject=60,claims=8,domain=60:0..7,item-placement=60x1>0,break=finite+removed,drop=3x1,persistence=chunk-nbt,collision=full-vs-15/16,light=255:0,ticks=random-T+hydrate+dry,neighbors=air-stable+solid-cover-dirt,oracle=MATCH`.

Qualified semantic signature: `7e3ab53109572ad7404382a8ecc0492abb956eb664e0f807e6e5abb731b860ee`.
