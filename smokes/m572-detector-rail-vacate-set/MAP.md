<!-- worldline-map-schema=1 -->
<!-- boundary=m572-detector-rail-vacate-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=fddc5e78e37e693e02add3085bf1e0c53f9d464a11c3784f5b7814b28274f5ba -->

# M572 behavior map

The cloned raised stone column keeps detector rail `28` on the original
support at `(4,72,4)`. Stone one north and one up holds regular rail `66`
at `(4,73,3)`. Landing rail `66` occupies the south cells at the detector
Y. Official rail connection slopes the detector north (`28:4`) toward the
higher rail, so gravity can roll a cart south onto the landing.

Packet15 of minecart item `328` occupies the sloped detector and writes
powered bit 8 (`28:4 -> 28:12`). After the cart leaves, the scheduled
detector tick clears bit 8 (`28:12 -> 28:4`). Fresh login Packet51 keeps
the vacated unpowered slope.

This map is distinct from unpowered detector place `28:0` (M185), occupied
detector persistence `28:0->8` (M402), and powered-rail launch onto a
detector that stays occupied (M377). It does not claim powered rail `27`,
furnace carts, riding, derail, or redstone wire.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+sloped-detector28+landing-rail66+minecart328|cause=packet15-item28+packet15-item66+packet15-minecart328|wire=packet23-type10+thrower0+packet53-detector28-occupy-then-vacate|oracle=occupy-then-vacate-detector+fresh-login|column=17,support=4:71:4:1:0,high=4:72:3:1:0,detector=4:72:4:28:4->12->4,landing=4:72:5:66:0,cart=type10+thrower0+fixed144:2331:144,occupy=28:12,vacate=28:4,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`fddc5e78e37e693e02add3085bf1e0c53f9d464a11c3784f5b7814b28274f5ba`.
