# M557 one-tick pulse set

M557 opens the official 1-tick piston-pulse boundary. A west-facing
1-tick repeater `93:3` sits between a floor lever and piston `33`. One
short Packet15 on/off pulse through that diode extends the piston then
immediately retracts and leaves the payload in the pushed cell.

Frozen semantic SHA-256:
`cd7816b4b28602a9d7bb4cb6e65bbfc8918216b84e075b8912af314905ec7c05`.

This is distinct from M367's two Packet15 full lever hold (`33:4 -> 12`
then `33:12 -> 4` with 10-tick settle on the piston support) and from
M144 sticky pull (`34:12 -> 1:0` with destination air). It does not
claim sticky 1-tick droppers, quasi-connectivity, BUD, T flip-flops, or
a generic redstone model.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.
