# M584 sheep-eat-grass retraction

M584 was reserved as `m584-sheep-eat-grass-set`, but its branch and worktree
never contained an implementation. The proposed behavior is not present in the
official Beta 1.7.3 dedicated server whose SHA-256 is
`033a127e4a25a60b038f15369c89305a3d53752242a1cff11ae964954e79ba4d`.

The official server class `ce` is `EntitySheep`: its constructor selects
`/mob/sheep.png`, its NBT methods read and write `Sheared` and `Color`,
and its data watcher stores those values at index 16. A complete bytecode method
inventory exposes initialization, damage, drops, player interaction, NBT,
sounds, color access, sheared access, and random spawn color. It contains no
living-update override, grass search, grass block mutation, or wool-regrowth
path. The inherited Beta 1.7.3 animal AI therefore cannot provide the proposed
event.

M506 remains the accepted sheep lifecycle boundary for shearing metadata,
persistence, repeated-shear rejection, and controlled NBT unshearing. M584
would claim a later-version mechanic as Beta 1.7.3 behavior, so the
`sheep-eat-grass` token is formally retracted. The empty Grok reservation is
preserved until lifecycle forensics classifies and archives it as a husk.
