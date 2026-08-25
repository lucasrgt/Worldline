<!-- worldline-map-schema=1 -->
<!-- boundary=tnt-chain -->
<!-- nonclaims=exact-fuse,crater-shape,player-damage,redstone,minecart,longer-chains -->
<!-- frozen-trace=64d0bfa01449b7517f3e1303d1552e807706c1948238c138be08d88619c811ba -->

# M637 TNT chain reaction behavior map

The public boundary is `worldline.testkit.TntChainFixture#observe`. The fixture owns two adjacent TNT `46` cells. The sole direct action primes the first with flint and steel `259`. Ordered observations retain two distinct Packet23 type-50 objects, bind the second object to the adjacent charge, retain two strength-four Packet60 explosions, and require air at both cells. The persisted observation repeats both air states after save and fresh login.

Equatable evidence removes entity IDs, fuse duration, explosion-propelled object motion, precise final explosion centers, and crater membership. The proof uses two fresh official Beta 1.7.3 dedicated-server replicas and never primes the second charge directly.

Frozen signal: `charges=2,adjacent=true,direct=packet23:50,chain=packet23:50,explosions=2xstrength4,both-air=true,persisted=true,replicas=2,disconnect=clean`.
