# M463 sword hurt set

M463 opens the official compound mob-hurt family. Two saved mob spawners
are retargeted from `Pig` to `Zombie` and `Skeleton`. After midnight
(`time set 14000`) the headless protocol-14 client observes Packet24
type `54` and type `51`, then Packet7-attacks each identity with diamond
sword `276` selected. Packet38 status 2 HURT is required on both
identities. `peekDeath` stays null after the first hit. The session
stops after hurt.

This is distinct from M353's pig hits-to-kill sword ladder, which does
not cover the hostile hurt family. It does not claim M388 zombie feather
`288` / skeleton arrow death drops, M444 remaining death drops, or M391
creeper Packet60 strength `3`.

Frozen semantic SHA-256:
`34f99909ebaad48c9c513f7aef51ee8586e82fb1b0db74e616104c22b7bb738c`.

This milestone does not claim death, drops, knockback vectors, XP, or
other hostile types. Headless `B173WireClient` only. No GUI. No Aero.
