<!-- worldline-map-schema=1 -->
<!-- boundary=b173-dirt-flora-lifecycle-cycle -->
<!-- nonclaims=random-tick-growth,bonemeal-tree-growth,light-survival,neighbor-removal,native-render -->
<!-- frozen-trace=4a17dfbc0a8cddf94aab012f6ab700de105bd37373778cd3326f7861622a9ca7 -->

# Beta 1.7.3 dirt-supported flora lifecycles

Five public TestKit cases exercise dandelion, rose, and the oak, spruce, and birch sapling variants
on gameplay-provisioned dirt. Each case proves the substrate precondition, exact placement-item
damage, exact placed metadata, placement consumption, persistence across a fresh login, break to
air, exact item drop and damage, unchanged stick state, and removed-state persistence after a
second fresh login.

The three sapling variants are three parametrized cases of one subject and therefore close one
set of four Functional Census claims, not twelve separate atoms. Together with the two flower
subjects, this package executes 20 case-claims but advances 12 distinct census claims as one
dirt-supported flora mini-subsystem.

This map does not claim random-tick or bonemeal growth, light-dependent survival, support-removal
neighbor reactions, species-specific tree generation, particles, or native rendering. Mushrooms
remain outside because their valid lifecycle additionally requires a controlled light canopy.

Frozen signal:
`provider=b1.7.3-server-lifecycle,family=dirt-flora,rows=5,passed=5,layers=U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-A,reload=FRESH_LOGINx10,evidence=2dace62920f2ad243999aabfaafbaade52d250fe31c40bdbe68186119766e875,isolation=5-fresh-worlds`.
