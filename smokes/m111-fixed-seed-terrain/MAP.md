<!-- worldline-map-schema=1 -->
<!-- boundary=m111-fixed-seed-terrain -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=b885d60be98dfb11c60f51a928c0fa9bdda225520187692098587a72e253fa98 -->

# M111 behavior map

M111 loads absolute chunk `(0,0)` from two newly generated official Beta 1.7.3
worlds with seed `17320110707`. View distance seven makes the fixed chunk
available despite the version's variable player-spawn selection. M112 later
hardened the fixture by writing a minimal player NBT at chunk `(0,0)` before
login, without changing M111 terrain evidence.

Packet50 establishes the chunk lifecycle and Packet51 supplies the complete
`16 x 128 x 16` payload. The decoder reports a diagnostic hash of all 32,768
legacy block IDs in X/Z/Y order. The semantic digest freezes the block count,
non-air count, and highest non-air block's Y/ID/metadata for all 256 columns.
Beta 1.7.3 population order can swap a few buried gravel and ore cells without
changing that surface contract.

Frozen trace:

```text
v2|server=official-b1.7.3|seed=17320110707|target=absolute-chunk|origin=0,0|blocks=32768|nonair=16342|surface=bb5fa0b1c2f242c7952ec1e58d269d66705fa308fcdcc3b25a30c8b309ea74db|full-id-plane=diagnostic|decode=packet50+packet51-xzy|disconnect=clean
```

SHA-256: `b885d60be98dfb11c60f51a928c0fa9bdda225520187692098587a72e253fa98`.

The player-spawn pose, complete block-ID plane, and complete metadata plane are
diagnostic and deliberately excluded. M111 does not freeze lighting, biomes,
entities, buried decoration ordering, structures outside this chunk, alternate
seeds, generators, dimensions, or persistence.
