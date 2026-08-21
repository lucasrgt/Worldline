# M38 Explicit Movement Fallback

Two fresh sessions execute two caller-supplied primary/fallback pairs. The
first primary is a safe `+0.125 X`; its fallback must be skipped. The second
primary targets a decoded solid terrain cube and must be corrected; its single
`+0.125 X` fallback then executes from the authoritative pose.

The exact outcomes are `UNCHALLENGED`, `CORRECTED`, `UNCHALLENGED`. There is no
retry of the blocked primary and no discovered path. Cache remains coherent,
and official player NBT must equal the explicit fallback's final pose.
