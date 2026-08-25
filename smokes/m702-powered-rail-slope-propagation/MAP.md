<!-- worldline-map-schema=1 -->
<!-- boundary=m702-powered-rail-slope-propagation -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=fce5958ba41931d5f80c5281e83f1f4c1939b0ec88ee47577d057f2d5fc59e91 -->

# M702 behavior map

The cloned raised stone column carries a north-south powered-rail `27` run
across one slope boundary. Stone one north and one up supports the higher
flat rail at `(4,73,3)`; the sloped cell sits on the original support at
`(4,72,4)` and two lower flat rails occupy `(4,72,5)` and `(4,72,6)`.
Official rail connection slopes the base cell north (`27:4`) toward the
higher rail, so the run crosses the ascending boundary uphill and the same
boundary downhill in one connected graph.

Packet15 of torch item `76` east of the slope base writes the powered bit on
all four rails: the slope cell (`27:4 -> 27:12`), the higher flat rail across
the ascending boundary, and both lower rails downhill. A bounded live hold
keeps every rail powered. Packet14 breaks the torch; propagation withdraws and
each rail restores its exact idle shape. Fresh login keeps the restored cells.

This map does not re-qualify flat-line power states (M309), powered launch
(M377), unpowered braking (M595), detector occupancy on a slope (M572), or
slope placement geometry alone (M432). It claims no minecart motion, riding,
derail, furnace carts, or redstone wire. Headless `B173WireClient`
protocol-14 only. No GUI. No Aero.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+slope27+top27+low27+far27+torch76|cause=packet15-item27x4+packet15-torch76+break-torch76|wire=packet53-rail27-slope-power-crossing+torch76:5->0|oracle=slope-boundary-power-both-directions+fresh-login|column=17,support=4:71:4:1:0,high=4:72:3:1:0,slope=4:72:4:27:4->12->4,top=4:73:3:27:0->8->0,low=4:72:5:27:0->8->0,far=4:72:6:27:0->8->0,torch=5:72:4:76:5->0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`fce5958ba41931d5f80c5281e83f1f4c1939b0ec88ee47577d057f2d5fc59e91`.
