# M111 behavior map

M111 loads absolute chunk `(0,0)` from two newly generated official Beta 1.7.3
worlds with seed `17320110707`. View distance seven makes the fixed chunk
available despite the version's variable player-spawn selection. M112 later
hardened the fixture by writing a minimal player NBT at chunk `(0,0)` before
login, without changing M111 terrain evidence.

Packet50 establishes the chunk lifecycle and Packet51 supplies the complete
`16 x 128 x 16` payload. The decoder hashes a solid/other mask for all 32,768
cells in X/Z/Y order. Solid excludes air, water `8/9`, and lava `10/11`. A
second digest freezes the highest non-air block's Y/ID/metadata for all 256
columns. Raw block IDs and non-air count remain diagnostic because official
population order can select different overlapping underground decorators.

Frozen trace:

```text
v2|server=official-b1.7.3|seed=17320110707|target=absolute-chunk|origin=0,0|blocks=32768|solid=13992|solid-definition=not-air-water8+9-lava10+11|terrain=ffa29af83b49293c2b2a5a1791c55270bb11848d5b4f7532b77b5d45e506f946|surface=bb5fa0b1c2f242c7952ec1e58d269d66705fa308fcdcc3b25a30c8b309ea74db|raw-block-ids+nonair=diagnostic-not-frozen|decode=packet50+packet51-xzy|disconnect=clean
```

SHA-256: `1f477f68603f951d995e99d26d4dec79788d29a5ef1f29ecb30f5f490ccc0c2f`.

The player-spawn pose, raw block IDs, non-air count, and complete metadata
plane are diagnostic and deliberately excluded. M111 does not freeze lighting, biomes, entities,
structures outside this chunk, decoration timing, alternate seeds, generators,
dimensions, or persistence.
