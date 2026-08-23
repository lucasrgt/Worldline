# M575-GRASS-DIE-COVER-SET grass die cover set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M575 qualifies official grass die-off from block 2 under an opaque stone cover as one SET. Packet15 only builds the raised-stone pad: an 8-cell grass ring, exposed grass samples, and one stone-covered grass cell. Random ticks then emit Packet53 2->3 on the covered grass. Adjacent exposed grass stays 2. The frozen signal names die=2->3 and exposed-stay=true. Converted dirt survives a clean save plus fresh login. This family is the inverse of grass-spread and is distinct from shipping M238 grass place and M223 dirt place. It does not claim farmland, hoe conversion, mycelium, or a generic spreading-block model. Headless B173WireClient protocol-14 only. No GUI. No Aero. Exact wait length is not hashed.

## Qualification cycle

DataDrivenCycle rebuilds the raised stone grass pad in two fresh official server JVMs. Each run Packet15-places an 8-cell grass 2 ring beside exposed grass 2 samples and one stone-covered grass 2, then waits a bounded random-tick window until Packet53 2->3 appears on the covered cell while exposed grass stays grass. One official EOF is retried after a 5 second sleep. The frozen signal must name die=2->3 and exposed-stay=true and must not claim M238/M223 place oracles. Canonical evidence uses two official server JVMs and four client sessions. Headless protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,support=4:71:4:1:0,grass-ring=8,source=2:0,exposed=4:72:4+6:72:4+2:72:4+4:72:2,covered=4:72:6:3:0,cover=4:73:6:1:0,die=2->3,exposed-stay=true,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `eba768ee294d89efacd60974254f64334d6ac35bdd21603215f427be90ac5735`.
