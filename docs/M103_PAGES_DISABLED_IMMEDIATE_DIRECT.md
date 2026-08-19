# M103 pages-disabled immediate-direct control

Status: GO in Worldline v1.91.0.

M103 runs the exact sixteen-entity M74/M78 scene with literal
`aero.becell.pages=false`, cache maximum one, rebuild sentinel negative one,
and TTL100000. The disabled gate makes each `queueAtRest` entry draw
immediately rather than joining a page. Runtime validation proves every
literal before capture; common/server code remains Aero-free.

Every retained record must show sixteen renderer/queue entries, sixteen
smoke-owned calls to the exact immediate `drawDirect` overload, and two empty
flush calls. Public cell counters remain queued0/pageCalls0/direct0/rebuild0,
cache0, and evicted0 because the empty flush resets them. The aligned M74
census independently proves sixteen actual at-rest renders and list calls plus
all sixteen synchronized identities.

This is a structural control, not a timing comparison with M101/M102. It is
bounded to the exact property value, overload, fixture, and pinned revision.

Nonclaims: additive cost, relative performance, generic disabled-path
behavior, other render entrypoints or fixtures, causality,
regression/improvement, inference, pixels, cross-machine generality, combat,
or historical lag reproduction.
