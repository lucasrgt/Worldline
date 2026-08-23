# M466 behavior map

The fixture docks the seed-water column in chunk `0,0`, builds a `7×7`
stone floor, hollows a dry open `5×5` pen at `y<63`, and places one default
spawner `52:0`. After a clean save the region NBT `EntityId` is rewritten
from `Pig` to `Squid`. Packet24 type `94` must spawn on land within official
attack range. A Packet7 diamond-sword `276` strike kills that dry squid;
death while out of water is Packet38 status `3` plus Packet29. The fixture
does not claim that land exposure itself caused damage.
Already-received movement is followed without requiring a movement packet.
Ink sac `351:0` is unclaimed.

This map does not re-qualify M408 squid-in-water ink. It does not claim
other water mobs, XP, or cooked drops.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=seed-water-dock+open-dry-y<63-squid-spawner52|cause=nbt-entityid-squid+land-sword276|wire=packet24-type94+packet38-status3+packet29|oracle=squid-type94-land-death-not-m408-ink|column=9,surface=4:63:4:1:0,floor=7x7,pen=5x5-open-dry-y62,spawner=4:62:4:52:0,entityid=Squid,land=true,mob=type94,death=land+packet7-sword276+packet38-status3+packet29,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`3c3b628471c4ee01b5da67ea523767d75fcc305a6747025b28720ca05ecab8a6`.
