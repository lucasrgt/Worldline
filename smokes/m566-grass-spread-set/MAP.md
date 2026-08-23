<!-- worldline-map-schema=1 -->
<!-- boundary=m566-grass-spread-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=b80a81abefd273cf68a6495d0a825f6556e85176324d77bc1702832ffc448174 -->

# M566 behavior map

Packet15 of grass item `2` and dirt item `3` builds a small raised-stone
pad: an 8-cell grass ring, four open dirt samples, and one dirt cell
covered by stone `1`. Official random ticks then write Packet53 dirt
`3` to grass `2` on at least one lit sample. The covered/dark dirt cell
stays dirt `3`. Exact wait length and which lit sample converts are not
hashed.

This map does not claim M238 grass place or M223 dirt place as the
conversion, farmland, hoe use, or mycelium.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+grass2-ring8+lit-dirt3+covered-dirt3|cause=packet15-item2+item3+random-ticks|wire=packet53-dirt3-to-grass2+covered-dirt3|oracle=lit-dirt-spread+dark-dirt-stay+fresh-login|column=17,support=4:71:4:1:0,grass-ring=8,source=2:0,lit=4:72:4+6:72:4+2:72:4+4:72:2,covered=4:72:6:3:0,cover=4:73:6:1:0,spread=3->2,covered-stay=true,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`b80a81abefd273cf68a6495d0a825f6556e85176324d77bc1702832ffc448174`.
