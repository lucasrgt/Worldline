# M427 remaining piston orient set

M427 qualifies the official remaining place-facings of piston `33` and
sticky piston `29` as one compound SET. Packet15 of those items on a
raised stone fixture writes down `0`, north `2`, south `3`, west `4`,
and east `5` for both ids in one session. The frozen signal names both
`33` and `29` plus those five remaining metas. All ten cells survive a
clean save plus fresh login.

This family is distinct from shipping M293/M294 single up-facing place
(`29:1` / `33:1`) and from M367 piston-motion (west `33:4` / `29:4`
extend, retract, and sticky pull). It does not claim powered heads,
multi-block pushes, quasi-connectivity, or a generic piston model.
Headless `B173WireClient` protocol-14 only. No GUI. No Aero.

Frozen semantic SHA-256:
`467d62056ad74b5561c6e6bf67533b1608d7fc66644062154b00b81109e8ad76`.
