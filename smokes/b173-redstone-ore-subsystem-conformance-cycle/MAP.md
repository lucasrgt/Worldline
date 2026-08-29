<!-- worldline-map-schema=1 -->
<!-- boundary=b173-redstone-ore-subsystem-conformance-cycle -->
<!-- nonclaims=client-particles,native-render,dust-rng-distribution,fortune,silk-touch,experience,wire-power -->
<!-- frozen-trace=f3520bbd8dfa2735748b7a21b8ed9c2d6015710042202f4e0091eb154621deb9 -->

# Beta 1.7.3 redstone ore subsystem conformance

`RedstoneOreSubsystemFixture` treats inactive ore `73` and glowing ore `74` as one state
machine. Both registry entries resolve to the same native block implementation. A native click on
`73:0` causally materializes `74:0`; the glowing state's random-tick callback returns `73:0`.

Breaking `74:0` removes it and emits four or five redstone dust items `331:0`. Native chunk
serialization preserves both `73:0` and `74:0`. Both states have full collision and opacity 255;
inactive emits zero light and glowing emits nine.

Stone and lever neighbor notifications preserve both states. Registry presence is observed here
for fixture integrity but is already covered by the universal registry census claim, so this
milestone promotes only the thirteen previously unknown claims.

The map does not claim client particles, native rendering, the statistical dust distribution,
Fortune, silk touch, experience, or downstream wire power.

Frozen aggregate signal:
`family=redstone-ore-subsystem,subjects=2,claims=13,registry=73+74,domains=73:0+74:0,materialization=click-73>74,drop=331x4..5,persistence=chunk-nbt-both,collision=full+full,light=0+9,ticks=FT+fade,neighbors=stable,oracle=MATCH`.

Qualified semantic signature: `f3520bbd8dfa2735748b7a21b8ed9c2d6015710042202f4e0091eb154621deb9`.
