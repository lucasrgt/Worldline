<!-- worldline-map-schema=1 -->
<!-- boundary=b173-deterministic-harvest-lifecycle-cycle -->
<!-- nonclaims=random-drop-count,silk-touch,fortune,consumption-state,native-render -->
<!-- frozen-trace=0000000000000000000000000000000000000000000000000000000000000000 -->

# Beta 1.7.3 deterministic harvest lifecycles

Four public TestKit rows execute complete lifecycles for grass, cobweb, bookshelf, and cake. This
family targets historical deterministic harvest laws where the placed block does not simply drop
itself: dirt from grass, string from sword-broken cobweb, and the Beta-era empty drops for bookshelf
and cake. Cake additionally proves that the caller-declared placement item can differ from the
resulting block ID.

Each isolated case proves placement consumption, exact placed state, fresh-login persistence,
break to air, exact zero-or-one drop matrix, exact tool state, and removed-state persistence after
a second fresh login. It executes sixteen case-claims but advances only the eight break/drop claims
that remain unknown for these four subjects.

This map does not claim randomized drops, silk touch, fortune, cake consumption metadata, cobweb
movement slowdown, grass spread, fire behavior, particles, or native rendering.

Discovery signal:
`provider=b1.7.3-server-lifecycle,family=deterministic-harvest,rows=4,passed=4,layers=U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-S,reload=FRESH_LOGINx8,evidence=0000000000000000000000000000000000000000000000000000000000000000,isolation=4-fresh-worlds`.
