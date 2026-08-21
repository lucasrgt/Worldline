# M170 redstone repeater pulse

M170 opens the official diode boundary. Packet15 of repeater item 356 on a
raised west-facing stone line places unpowered block `93:3` with look yaw
`90`. Packet53 confirms that facing metadata. A floor lever on the east
input cell is toggled with empty-hand Packet15.

After the default 1-tick delay, a fresh Packet51 login contains powered
block `94:3` while the lever is still on. Turning the lever off returns
`93:3`. A second save plus login retains unpowered `93:3`.

This milestone does not claim 2/3/4-tick delay, repeater locking,
comparators (they do not exist in Beta 1.7.3), or quasi-connectivity.
