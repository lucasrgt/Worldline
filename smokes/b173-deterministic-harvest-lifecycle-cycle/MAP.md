<!-- worldline-map-schema=1 -->
<!-- boundary=b173-deterministic-harvest-lifecycle-cycle -->
<!-- nonclaims=random-drop-count,silk-touch,fortune,consumption-state,native-render -->
<!-- frozen-trace=e998fe6b9558213a92917c69a679484762377b6c2d777f22f3c8dfe600ebb242 -->

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

Both official replicas produced identical row artifacts: grass `49373999ac34a59ecc8dfb0f266071cf6e70441d983619de1c207731b0f6eceb`, cobweb `0aac8bec5249faf02e5f3492754d884e00ce286fdfb62497475ad36f08d2493b`, bookshelf `825233e4b85185ff365e956613d900ad54e71828c11f16d86059c7e49f61e432`, and cake `a5700a9592275becd95f395a7e9252cf0532753d5bcea23ea44bd6b74f85c251`.

Frozen signal:
`provider=b1.7.3-server-lifecycle,family=deterministic-harvest,rows=4,passed=4,layers=U-U-U-A+U-U-U-A+U-U-U-A+U-U-U-S,reload=FRESH_LOGINx8,evidence=42942ddbf391ca83fc928568a8e6847270225b9365642fec9b35607e921e0bbb,isolation=4-fresh-worlds`.
