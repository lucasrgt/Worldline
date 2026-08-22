# M546 piston QC set

M546 opens the official BlockPistonBase quasi-connectivity boundary.
A regular piston `33` extends when the block ABOVE the piston is
powered, while the piston cell itself is not directly powered: no
adjacent dust, torch, or lever on the piston.

One headless session builds a west-facing piston `33:4` on the raised
stone column, places stone on top of the piston, and attaches lever
`69` to that above-block. Lever Packet15 QC-extends `33:4 -> 33:12`
with head `34:4`, then unpowers the above-block and retracts. Those
final cells remain after a clean save plus fresh login.

Frozen semantic SHA-256:
`10f2cb5507e55026ceafd72d6ab2c74a3ab228aad1924965b9844d149117d989`.

This is distinct from M367 (lever on the piston support), shipping
M142-M147 1:1 piston cycles, and M427 remaining place-facings. It does
not claim sticky QC, BUD-without-update, two-block chains, push limits,
obsidian rejection, or a generic piston model.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.
