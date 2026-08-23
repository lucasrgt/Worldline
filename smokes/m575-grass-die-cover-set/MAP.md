# M575 behavior map

Packet15 of grass item `2` builds a small raised-stone pad: an 8-cell
grass ring, four open grass samples, and one grass cell covered by
stone `1`. Official random ticks then write Packet53 grass `2` to dirt
`3` on the covered sample. Exposed grass cells stay grass `2`. Exact
wait length is not hashed.

This map does not claim M238 grass place or M223 dirt place as the
conversion, grass spread onto dirt, farmland, hoe use, or mycelium.

Frozen signal:
`column=17,support=4:71:4:1:0,grass-ring=8,source=2:0,exposed=4:72:4+6:72:4+2:72:4+4:72:2,covered=4:72:6:3:0,cover=4:73:6:1:0,die=2->3,exposed-stay=true,persisted=true,clients=2,disconnect=clean`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+grass2-ring8+exposed-grass2+covered-grass2|cause=packet15-item2+item1-cover+random-ticks|wire=packet53-grass2-to-dirt3+exposed-grass2|oracle=covered-grass-die+lit-grass-stay+fresh-login|column=17,support=4:71:4:1:0,grass-ring=8,source=2:0,exposed=4:72:4+6:72:4+2:72:4+4:72:2,covered=4:72:6:3:0,cover=4:73:6:1:0,die=2->3,exposed-stay=true,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`eba768ee294d89efacd60974254f64334d6ac35bdd21603215f427be90ac5735`.
