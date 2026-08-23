<!-- worldline-map-schema=1 -->
<!-- boundary=m417-remaining-tnt-place -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=153e7f2258e4d355e0e2c070a630aebe6dfa4262d98a3e4aa3e99b8f99e0205d -->

# M417 behavior map

Packet15 places two TNT items `46` on a raised stone column, two cells apart
on the east axis so flint fire cannot prime both. Packet15 of flint-and-steel
item `259` primes only the first `46:0` cell into a primed entity. Packet23
type `50` is the primed-TNT object on the existing tracker. After the bounded
fuse, protocol-14 Packet60 strength `4` primes the second TNT cell. A second
Packet23 type `50` and a second Packet60 strength `4` complete the chain.
Both TNT cells remain air after a clean save plus fresh login.

This is distinct from M219 unprimed place, M137 one Packet60 detonate, and
M381 one-TNT prime. Exact blast rays and destroyed-cell count are not hashed.
Nether-bed Packet60 strength `5` is out of scope.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+tnt46+tnt46+flint259|cause=packet15-item46+packet15-item46+packet15-item259-prime-one|fuse=100ticks|wire=packet60-strength4+packet60-strength4+packet23-type50-chain|oracle=live-two-tnt-chain+crater-air+fresh-login|column=15,support=4:69:4:1:0,tnt1=4:70:4:46:0->0:0,tnt2=6:70:4:46:0->0:0,flint=259,packet23=50+50,packet60=strength4,chain=true,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`153e7f2258e4d355e0e2c070a630aebe6dfa4262d98a3e4aa3e99b8f99e0205d`.
