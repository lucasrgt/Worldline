# M111 behavior map

M111 loads absolute chunk `(0,0)` from two newly generated official Beta 1.7.3
worlds with seed `17320110707`. View distance seven makes the fixed chunk
available despite the version's variable player-spawn selection. M112 later
hardened the fixture by writing a minimal player NBT at chunk `(0,0)` before
login, without changing M111 terrain evidence.

Packet50 establishes the chunk lifecycle and Packet51 supplies the complete
`16 x 128 x 16` payload. The decoder hashes all 32,768 legacy block IDs in
X/Z/Y order. A second digest freezes the highest non-air block's
Y/ID/metadata for all 256 columns.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|target=absolute-chunk|origin=0,0|blocks=32768|nonair=16342|terrain=a51059341212e1be0b41cea24881a07e962ffa39a4aa4dc874a76fc61e4326bf|surface=bb5fa0b1c2f242c7952ec1e58d269d66705fa308fcdcc3b25a30c8b309ea74db|decode=packet50+packet51-xzy|disconnect=clean
```

SHA-256: `1242a03c15a6e0c36adbefb6ca2b89b166ab1b57f5fb20cf6d3f402a0bec50b1`.

The player-spawn pose and complete metadata plane are diagnostic and
deliberately excluded. M111 does not freeze lighting, biomes, entities,
structures outside this chunk, decoration timing, alternate seeds, generators,
dimensions, or persistence.
