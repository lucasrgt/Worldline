# M362 behavior map

Packet15 places two adjacent fence items `85` on a raised stone column.
The actor Packet13-walks the same intended `+1 Z` step in air, then after
those live `85:0` cells exist. Air is unchallenged (`1000` milli-blocks).
The fence walk is corrected back to the start (`0` milli-blocks). Both
fence cells survive a clean save plus fresh login.

This is not M173 fence placement. M173 only plants two `85:0` cells. This
is not M329 utility-block crafts. M329 only crafts fence, ladder, and
bookshelf. Jump-over and fence-gate are not claimed. Fence gates do not
exist in Beta 1.7.3.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+adjacent-fence85-path|cause=packet15-item85+packet13-walk|wire=packet13-air-vs-fence-collision|oracle=fence-walk-blocked-vs-air|column=17,support=4:71:4:1:0,north=4:71:3:1:0,west=4:72:4:85:0,east=5:72:4:85:0,ticks=10,air=1000,fence=0,air-disp=unchallenged,fence-disp=corrected,blocked=true,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`5784076d8eb5c6e86478f102566067459f9c73c231b5f92141b25d65c79ae290`.
