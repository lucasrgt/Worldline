# M364 behavior map

The M196 raised stone column hosts three official translucent cells in
one session. Packet15 places glass item `20` on a west pad, ice item
`79` on an east pad, and oak log item `17` on the north face so leaves
item `18` persist as `18:8` on that log. A clean save plus fresh login
decodes Packet51 sky-light and block-light nibbles at those three cells.

The three sky-light samples must be pairwise distinct. That compound is
not M112's untouched planes and not M196, M193, or M209 place-only.

This map does not re-qualify glass placement without light (M196), ice
placement without light (M193), or leaves placement without light
(M209). Headless `B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+glass20+oak17+leaves18+ice79|cause=packet15-item20+packet15-item18+packet15-item79|wire=packet53-glass20:0+packet53-leaves18:8+packet53-ice79:0|oracle=fresh-login-packet51-distinct-sky-light|column=17,support=4:71:4:1:0,west=3:71:4:1:0,glass=3:72:4:20:0:sky15:block0,east=5:71:4:1:0,ice=5:72:4:79:0:sky12:block0,log=4:71:3:17:0,leaves=4:72:3:18:8:sky14:block0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`2012aa0391268a287bc772ea5a40036b761ed9f72b90d91f8462512dbb0e3fab`.
