# M443 remaining bucket rest set

M443 opens the official remaining empty-bucket pickup set. Empty bucket
`325` is the reverse of place: it scoops water `8/9` and lava `10/11`
together in one session. Shipping M344 already froze still-source place
plus pickup for water `326`/`9` and lava `327`/`11`, so this milestone
freezes the remaining source-versus-flowing pickup contrast.

Still water `9:0` is picked up to air and water bucket `326`. Adjacent
flowing water `9:1` (water family `8/9`) is rejected and the bucket stays
`325`. Still lava `11:0` is picked up to air and lava bucket `327`.
Adjacent flowing lava `11:2` (lava family `10/11`) is rejected and the
bucket stays `325`. Both filled buckets and both empty source cells
survive a clean save plus fresh login.

This is distinct from shipping M168 and M181 (source pickup only), M254
and M255 (place only), M344 (still-source place plus pickup), and M392
(horizontal flow with no bucket). It does not claim infinite sources,
obsidian, cobble, milk, or a Worldline fluid simulator. Headless
`B173WireClient` only. No GUI. No Aero.

The frozen semantic SHA-256 is
`b556b71fd57896aa06fbb39f5088d8f96e6c8a64076014c7d7391b961c669eb7`.
