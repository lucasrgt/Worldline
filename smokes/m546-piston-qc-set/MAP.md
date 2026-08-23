<!-- worldline-map-schema=1 -->
<!-- boundary=m546-piston-qc-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=10f2cb5507e55026ceafd72d6ab2c74a3ab228aad1924965b9844d149117d989 -->

# M546 behavior map

The cloned M142 west-facing piston occupies one raised stone column.
Normal piston `33:4` sits on the support at `(4,65,4)` with air in
front. Stone `1:0` occupies the cell ABOVE the piston at `(4,66,4)`.
Side lever `69:1` is attached to that above-block's east face at
`(5,66,4)`, so it is not adjacent to the piston cell and no dust,
torch, or lever touches the piston.

Empty-hand Packet15 powers the above-block. Official BlockPistonBase
quasi-connectivity extends piston `33` (`33:4 -> 33:12`, head `34:4`)
while `direct-power=false`. A second Packet15 unpowers the above-block
and retracts (`33:12 -> 33:4`, head air). Fresh login Packet51 keeps
the retracted arm.

This map is distinct from M367 lever-on-piston motion, shipping
M142-M147 1:1 piston cycles, and M427 remaining place-facings.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=piston33-west+above-stone+lever69-above-east|settle=200+10ticks|cause=packet15-lever-on-above-block+deactivate|effect=official-piston33-qc-extend+head34+qc-retract|observation=fresh-login-packet51|column=10,qc-extend=33:4->12,qc-retract=33:12->4,piston=4:65:4:33:4->12->4,head=3:65:4:0:0->34:4->0:0,above=4:66:4:1:0,lever=5:66:4:69:1->9->1,direct-power=false,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`10f2cb5507e55026ceafd72d6ab2c74a3ab228aad1924965b9844d149117d989`.
