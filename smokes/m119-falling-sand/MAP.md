<!-- worldline-map-schema=1 -->
<!-- boundary=m119-falling-sand -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=ac00ec1900fdfc0489c6e7d4e9621c916411505d522df3c1fc9f3c53a78eb656 -->

# M119 behavior map

The official fixture stabilizes stone `1:0` at `(4,64,4)` with sand `12:0`
directly above it. Packet14 removes the stone and Packet53 must expose air at
the lower coordinate before settlement.

The server's falling-sand behavior then places `12:0` in the lower cell and
leaves `0:0` in the former upper cell. After clean disconnect/save, a fresh
Packet51 must expose both states. The ordered full-chunk delta admits exactly
those two cells.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=stone-column+supported-sand12|settle=40+40ticks|cause=packet14-remove-support|confirmation=packet53-air|effect=official-falling-sand-settle|observation=live-packet53+fresh-login-packet51|column=10,lower=4:64:4:1:0->12:0,upper=4:65:4:12:0->0:0,states=2:f2249e6e8b5904961f450ca0dfa697956efd8acadf85263d4dfe05b08344ca6a|disconnect=clean
```

SHA-256: `ac00ec1900fdfc0489c6e7d4e9621c916411505d522df3c1fc9f3c53a78eb656`.

Packet14 is request evidence; Packet53 air is the support-removal boundary.
The settled live state and reload Packet51 are the gravity outcome oracles.
