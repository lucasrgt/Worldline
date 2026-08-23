<!-- worldline-map-schema=1 -->
<!-- boundary=movement-route -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=850b6e29ed5e8aab12e48625ebde6b8ce1902b581d9e07f55c8488f2d7bfd947 -->

# M38 Explicit Movement Fallback

Two fresh sessions execute two caller-supplied primary/fallback pairs. The
first primary is a safe `+0.125 X`; its fallback must be skipped. The second
primary targets a decoded solid terrain cube and must be corrected; its single
`+0.125 X` fallback then executes from the authoritative pose.

The exact outcomes are `UNCHALLENGED`, `CORRECTED`, `UNCHALLENGED`. There is no
retry of the blocked primary and no discovered path. Cache remains coherent,
and official player NBT must equal the explicit fallback's final pose.

Frozen expected signature SHA-256: `850b6e29ed5e8aab12e48625ebde6b8ce1902b581d9e07f55c8488f2d7bfd947`
