# M373 milk bucket set

M373 opens the official compound cow-fill and milk-drink set. Empty
bucket `325` used on a living type-`92` cow through Packet7 button 0
fills milk bucket `335`. Packet15 air-use then drinks that stack back to
empty bucket `325`. The frozen signal includes fill plus drink together
(`325:1:0->335:1:0->325:1:0`). Health stays `20 -> 20`. The empty bucket
survives a clean save plus fresh login.

This is distinct from shipping M267 (seeded milk drink only) and from
shipping M344 (water `326`/`9` and lava `327`/`11` place plus pickup).
It does not claim cake crafting, status effects, water buckets, or lava
buckets. Headless `B173WireClient` only. No GUI. No Aero.

The frozen semantic SHA-256 is
`0def850e0165e277e1055538ab58e3a7772dcf0239f16acbc88f430b10e9a77c`.
