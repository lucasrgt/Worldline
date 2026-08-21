# M357 behavior map

Seven accepted window-0 Packet102 clicks craft glowstone block `89` from four
glowstone dust `348` in the personal 2x2 grid. This is the vanilla Beta 1.7.3
recipe; the dedicated server has no reverse uncraft of `89` back to dust.

- left-click hotbar slot 36 to pick up dust `348x4:0`
- right-place one dust into matrix slots 1, 2, 3, and 4
- take result `89x1:0` from slot 0 and store it in slot 36

That stack survives a clean save plus fresh login. This map does not claim
M191 glowstone placement, light-plane hashing, or glowstone-dust drops.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=personal-2x2-dust348x4|window0=2x2-dust-to-glowstone89|cause=packet102-window0-left+button1-right-place|wire=packet106-accepted|oracle=result89x1+fresh-login|dust=348x4:0,result=89x1:0,taken=true,stored=36:89x1:0,grid=2x2,actions=7,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`af0a81cf89ec64afd6056fb4755ef7ed9350bac34875caa333cc150d99d7955c`.
