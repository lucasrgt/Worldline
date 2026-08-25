# M637-TNT-CHAIN TNT chain reaction

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M637 qualifies the Beta 1.7.3 TNT chain-reaction boundary. Packet15 with flint and steel primes the first of two adjacent TNT 46 blocks. Its strength-four Packet60 explosion must produce a distinct second Packet23 type-50 primed object and a second strength-four Packet60 explosion. Both charge cells persist as air. The claim excludes exact fuse durations, crater shape, player damage, redstone priming, minecarts, and chains longer than two charges.

## Qualification cycle

DataDrivenCycle executes two fresh official dedicated-server replicas at seed 17320110707. Each builds two adjacent raised TNT charges, directly primes only the first, then observes two ordered primed-object/explosion pairs and verifies both cells after a fresh login. TntChainFixture binds the second primed object to the adjacent charge and normalizes entity IDs, exact fuse timing, explosion-propelled motion, final second-explosion center, and crater blocks while rejecting nonadjacent charges, duplicate objects, wrong strength, or surviving TNT.

Expected signal: `charges=2,adjacent=true,direct=packet23:50,chain=packet23:50,explosions=2xstrength4,both-air=true,persisted=true,replicas=2,disconnect=clean`.

Frozen semantic SHA-256: `64d0bfa01449b7517f3e1303d1552e807706c1948238c138be08d88619c811ba`.
