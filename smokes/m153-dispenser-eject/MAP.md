<!-- worldline-map-schema=1 -->
<!-- boundary=m153-dispenser-eject -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=e9ac098cef87b90c28a3fdc264de812fb712489a228157a47e92fa23c958d3ec -->

# M153 behavior map

A side lever powers one official dispenser that ejects a single cobblestone.
Packet100 opens the Trap window, Packet102 loads slot 0, and Packet21 carries
the ejected stack. The live dispenser inventory is empty after the eject.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-dispenser23-west+side-lever69|cause=packet15-lever-activate|wire=packet100-trap+packet102-load+packet21-cobble4|oracle=official-dispenser-eject|column=17,disp=4:72:4:23:4,lever=5:71:4:1->9,load=4x1,drop=packet21-4x1,remain=empty,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`e9ac098cef87b90c28a3fdc264de812fb712489a228157a47e92fa23c958d3ec`.
