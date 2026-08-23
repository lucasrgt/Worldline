# M577 wheat light halt behavior map

Packet15 of wooden hoe `290` and seeds `295` builds a small raised-stone
farmland pad: three open wheat `59:0` samples and one wheat cell covered
by stone `1`. Official random ticks then write Packet53 crop-age on at
least one lit sample. The covered/dark wheat cell stays `59:0`. Exact wait
length and which lit sample ages are not hashed.

This map does not claim M179 wheat place, bonemeal, harvest, hoe
durability, or a generic plant-growth model.

Frozen signal: `column=17,support=4:71:4:1:0,water=5:72:3:9:0,hoe=290,seeds=295,wheat=59,lit=6:73:4+2:73:4+4:73:2,covered=4:73:6:59:0,cover=4:74:6:1:0,lit-age>=1,dark-stay=true,persisted=true,clients=2,disconnect=clean`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+hydrated-farmland60+lit-wheat59+covered-wheat59|cause=packet15-hoe290+seeds295+random-ticks|wire=packet53-crops59-age+covered-59:0|oracle=lit-wheat-age+dark-wheat-halt+fresh-login|column=17,support=4:71:4:1:0,water=5:72:3:9:0,hoe=290,seeds=295,wheat=59,lit=6:73:4+2:73:4+4:73:2,covered=4:73:6:59:0,cover=4:74:6:1:0,lit-age>=1,dark-stay=true,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`4afa82e0dca6bebe303f9a381e5dbd2b69bd22f7d2f13a32082b6c262cadb425`.
